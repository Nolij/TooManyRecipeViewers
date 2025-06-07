package dev.nolij.toomanyrecipeviewers.impl.jei.api.gui.builder;

import dev.emi.emi.mixin.accessor.OrderedTextTooltipComponentAccessor;
import dev.nolij.toomanyrecipeviewers.util.ComponentFormattedCharSink;
import dev.nolij.toomanyrecipeviewers.util.FormattedTextConsumer;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class TMRVTooltipBuilder implements ITooltipBuilder {
	
	private static Component getComponent(Object object) {
		return switch (object) {
			case Component component -> component;
			case FormattedCharSequence sequence -> ComponentFormattedCharSink.fromSequence(sequence);
			case OrderedTextTooltipComponentAccessor accessor -> ComponentFormattedCharSink.fromSequence(accessor.getText());
			default -> null;
		};
	}
	
	private final List<Object> lines = new ArrayList<>();
	
	public TMRVTooltipBuilder(List<ClientTooltipComponent> lines) {
		this.lines.addAll(lines);
	}
	
	@Override
	public void add(FormattedText formattedText) {
		lines.add(formattedText);
	}
	
	@Override
	public void addAll(Collection<? extends FormattedText> collection) {
		collection.forEach(this::add);
	}
	
	@Override
	public void add(TooltipComponent tooltipComponent) {
		lines.add(tooltipComponent);
	}
	
	@Override
	public void setIngredient(ITypedIngredient<?> typedIngredient) {
		
	}
	
	//? if >=21.1 {
	@Override
	public void clear() {
		lines.clear();
	}
	//?}
	
	@SuppressWarnings("removal")
	@Override
	public List<Component> toLegacyToComponents() {
		return lines.stream()
			.map(TMRVTooltipBuilder::getComponent)
			.filter(Objects::nonNull)
			.toList();
	}
	
	@SuppressWarnings("removal")
	@Override
	public void removeAll(List<Component> list) {
		final var toRemove = new HashSet<>(list);
		for (final var iterator = lines.iterator(); iterator.hasNext(); ) {
			final var line = iterator.next();
			final var component = getComponent(line);
			if (component != null && toRemove.contains(component)) {
				iterator.remove();
				toRemove.remove(component);
			}
		}
	}
	
	public List<ClientTooltipComponent> getClientTooltipComponents() {
		return lines.stream()
			.map(line -> switch (line) {
				case ClientTooltipComponent clientTooltipComponent -> clientTooltipComponent;
				case Component component -> ClientTooltipComponent.create(component.getVisualOrderText());
				case FormattedText formattedText -> 
					ClientTooltipComponent.create(
						Component.literal(
							FormattedTextConsumer.fromFormattedText(formattedText)
						).getVisualOrderText()
					);
				case TooltipComponent tooltipComponent -> ClientTooltipComponent.create(tooltipComponent);
				default -> null;
			})
			.filter(Objects::nonNull)
			.toList();
	}
	
}
