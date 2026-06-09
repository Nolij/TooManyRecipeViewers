package dev.nolij.toomanyrecipeviewers.impl.recipe;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTooltipComponents;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.nolij.toomanyrecipeviewers.plugin.Plugin;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

import java.util.List;

import static dev.nolij.toomanyrecipeviewers.TooManyRecipeViewersMod.shouldShowDebugInfo;

public class TMRVCategory<T> extends EmiRecipeCategory {
	
	private final IRecipeCategory<T> jeiCategory;
	private final Plugin plugin;
	
	public TMRVCategory(IRecipeCategory<T> jeiCategory, Plugin plugin) {
		super(jeiCategory.getRecipeType().getUid(), null);
		this.jeiCategory = jeiCategory;
		this.plugin = plugin;
		
		this.icon = (draw, x, y, delta) -> {
			final var context = EmiDrawContext.wrap(draw);
			if (shouldShowDebugInfo()) {
				context.fill(x - 1, y - 1, 18, 21, 0x7700FF00);
			}
			
			final var icon = this.jeiCategory.getIcon();
			if (icon != null) {
				icon.draw(context.raw(), x + (16 - icon.getWidth()) / 2, y + (16 - icon.getHeight()) / 2);
			} else {
				final var workstations = EmiApi.getRecipeManager().getWorkstations(this);
				if (!workstations.isEmpty()) {
					(workstations.getFirst()).render(context.raw(), x, y, delta, 1);
				} else {
					final var title = this.jeiCategory.getTitle().getString();
					context.drawCenteredTextWithShadow(EmiPort.literal(title.substring(0, Math.min(2, title.length()))), x + 8, y + 2);
				}
			}
		};
		this.simplified = this.icon;
	}
	
	@Override
	public Component getName() {
		return jeiCategory.getTitle();
	}
	
	@Override
	public List<ClientTooltipComponent> getTooltip() {
		final var result = super.getTooltip();
		
		if (shouldShowDebugInfo()) {
			result.add(EmiTooltipComponents.of(EmiPort.literal("JEI Plugin: " + plugin, ChatFormatting.GRAY)));
		}
		
		return result;
	}
	
}
