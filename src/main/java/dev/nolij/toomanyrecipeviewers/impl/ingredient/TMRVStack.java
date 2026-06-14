package dev.nolij.toomanyrecipeviewers.impl.ingredient;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.jemi.impl.JemiTooltipBuilder;
import dev.emi.emi.runtime.EmiDrawContext;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.ArrayList;
import java.util.List;

public class TMRVStack<T> extends EmiStack {
	
	public static <T> EmiStack create(IIngredientType<T> type, IIngredientHelper<T> helper, IIngredientRenderer<T> renderer, T ingredient) {
		if (!helper.isValidIngredient(ingredient))
			return ErrorEmiStack.INSTANCE;
		
		return new TMRVStack<>(type, helper, renderer, ingredient);
	}
	
	public final IIngredientType<T> type;
	public final IIngredientHelper<T> helper;
	public final IIngredientRenderer<T> renderer;
	public final T ingredient;
	
	public final Object key;
	
	private TMRVStack(IIngredientType<T> type, IIngredientHelper<T> helper, IIngredientRenderer<T> renderer, T ingredient) {
		this.type = type;
		this.helper = helper;
		this.renderer = renderer;
		this.ingredient = ingredient;
		
		if (type instanceof IIngredientTypeWithSubtypes<?, T> typeWithSubtypes) {
			this.key = typeWithSubtypes.getBase(ingredient);
		} else {
			//? if >=21.1 {
			this.key = helper.getUid(ingredient, UidContext.Ingredient);
			//?} else
			//this.key = helper.getUniqueId(ingredient, UidContext.Ingredient);
		}
	}
	
	@Override
	public EmiStack copy() {
		return new TMRVStack<>(type, helper, renderer, helper.copyIngredient(ingredient));
	}
	
	@Override
	public void render(GuiGraphics draw, int x, int y, float delta, int flags) {
		final var context = EmiDrawContext.wrap(draw);
		
		context.push();
		
		final var xRenderOffset = (16F - (float) renderer.getWidth()) * 0.5F;
		final var yRenderOffset = (16F - (float) renderer.getHeight()) * 0.5F;
		
		context.matrices().translate(x + xRenderOffset, y + yRenderOffset, 0F);
		
		renderer.render(context.raw(), ingredient);
		
		context.pop();
	}
	
	@Override
	public boolean isEmpty() {
		return false;
	}
	
	@Override
	public ItemStack getItemStack() {
		if (ingredient instanceof ItemStack itemStack) {
			return itemStack;
		}
		
		return super.getItemStack();
	}
	
	//? if >=21.1 {
	@Override
	public DataComponentPatch getComponentChanges() {
		return DataComponentPatch.EMPTY;
	}
	//?} else {
	/*@Override
	public CompoundTag getNbt() {
		return null;
	}
	*///?}
	
	@Override
	public Object getKey() {
		return key;
	}
	
	@Override
	public ResourceLocation getId() {
		return helper.getResourceLocation(ingredient);
	}
	
	//? if <21.1
	//@SuppressWarnings("removal")
	@Override
	public List<Component> getTooltipText() {
		return renderer.getTooltip(ingredient, Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.NORMAL : TooltipFlag.ADVANCED);
	}
	
	@Override
	public List<ClientTooltipComponent> getTooltip() {
		final var builder = new JemiTooltipBuilder();
		renderer.getTooltip(builder, ingredient, Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL);
		
		final var result = new ArrayList<>(builder.tooltip);
		if (EmiConfig.appendModId && getId() != null) {
			final var modName = EmiUtil.getModName(getId().getNamespace());
			result.add(ClientTooltipComponent.create(EmiPort.ordered(EmiPort.literal(modName, ChatFormatting.BLUE, ChatFormatting.ITALIC))));
		}
		
		result.addAll(super.getTooltip());
		return result;
	}
	
	@Override
	public Component getName() {
		return Component.literal(helper.getDisplayName(ingredient));
	}
	
}
