package dev.nolij.toomanyrecipeviewers.impl.widget;

import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.widgets.ITextWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.FormattedText;

import java.util.List;

public class TextWidget extends Widget implements ITextWidget {
	
	private final mezz.jei.common.gui.elements.TextWidget delegate;
	
	public TextWidget(List<FormattedText> lines, int maxWidth, int maxHeight) {
		this.delegate = new mezz.jei.common.gui.elements.TextWidget(lines, 0, 0, maxWidth, maxHeight);
	}
	
	@Override
	public ITextWidget setPosition(int x, int y) {
		delegate.setPosition(x, y);
		return this;
	}
	
	@Override
	public int getWidth() {
		return delegate.getWidth();
	}
	
	@Override
	public int getHeight() {
		return delegate.getHeight();
	}
	
	@Override
	public Bounds getBounds() {
		final var position = delegate.getPosition();
		return new Bounds(position.x(), position.y(), getWidth(), getHeight());
	}
	
	@Override
	public void render(GuiGraphics draw, int mouseX, int mouseY, float delta) {
		delegate.drawWidget(draw, mouseX, mouseY);
	}
	
	//region ITextWidget
	@Override
	public ITextWidget setFont(Font font) {
		delegate.setFont(font);
		return this;
	}
	
	@Override
	public ITextWidget setColor(int colour) {
		delegate.setColor(colour);
		return this;
	}
	
	@Override
	public ITextWidget setLineSpacing(int spacing) {
		delegate.setLineSpacing(spacing);
		return this;
	}
	
	@Override
	public ITextWidget setShadow(boolean shadow) {
		delegate.setShadow(shadow);
		return this;
	}
	
	@Override
	public ITextWidget setTextAlignment(HorizontalAlignment horizontalAlignment) {
		delegate.setTextAlignment(horizontalAlignment);
		return this;
	}
	
	@Override
	public ITextWidget setTextAlignment(VerticalAlignment verticalAlignment) {
		delegate.setTextAlignment(verticalAlignment);
		return this;
	}
	//endregion
	
}
