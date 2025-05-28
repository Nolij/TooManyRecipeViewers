package dev.nolij.toomanyrecipeviewers.impl.jei.api.gui.drawable;

import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.gui.GuiGraphics;

public record OffsetDrawable(IDrawable drawable, int xOffset, int yOffset) implements IDrawable {
	
	@Override
	public int getWidth() {
		return drawable.getWidth();
	}
	
	@Override
	public int getHeight() {
		return drawable.getHeight();
	}
	
	@Override
	public void draw(GuiGraphics guiGraphics, int x, int y) {
		drawable.draw(guiGraphics, x + xOffset, y + yOffset);
	}
	
	@Override
	public void draw(GuiGraphics guiGraphics) {
		drawable.draw(guiGraphics, xOffset, yOffset);
	}
	
}
