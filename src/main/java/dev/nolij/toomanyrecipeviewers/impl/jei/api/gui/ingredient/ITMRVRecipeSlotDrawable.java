package dev.nolij.toomanyrecipeviewers.impl.jei.api.gui.ingredient;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;

import java.util.List;

@SuppressWarnings("NonExtendableApiUsage")
public interface ITMRVRecipeSlotDrawable extends IRecipeSlotDrawable {
	
	@SuppressWarnings("removal")
	@Override
	default void draw(GuiGraphics guiGraphics) {
		throw new UnsupportedOperationException();
	}
	
	@SuppressWarnings("removal")
	@Override
	default void drawHoverOverlays(GuiGraphics guiGraphics) {
		throw new UnsupportedOperationException();
	}
	
	@SuppressWarnings("removal")
	@Override
	default List<Component> getTooltip() {
		throw new UnsupportedOperationException(); // TODO
	}
	
	@SuppressWarnings("removal")
	@Override
	default void getTooltip(ITooltipBuilder tooltipBuilder) {
		throw new UnsupportedOperationException(); // TODO
	}
	
	@Override
	default void drawTooltip(GuiGraphics guiGraphics, int i, int i1) {
		throw new UnsupportedOperationException(); // TODO
	}
	
	@SuppressWarnings("removal")
	@Override
	default boolean isMouseOver(double x, double y) {
		return getRect().contains((int) x, (int) y);
	}
	
	@SuppressWarnings("removal")
	@Override
	default void addTooltipCallback(mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback tooltipCallback) {
		throw new UnsupportedOperationException(); // TODO
	}
	
	@SuppressWarnings("removal")
	@Override
	default Rect2i getAreaIncludingBackground() {
		return getRect();
	}
	
	@Override
	default void drawHighlight(GuiGraphics guiGraphics, int colour) {
		throw new UnsupportedOperationException(); // TODO
	}
	
}
