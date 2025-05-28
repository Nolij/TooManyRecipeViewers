package dev.nolij.toomanyrecipeviewers.impl.widget;

import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import mezz.jei.common.Internal;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.gui.GuiGraphics;

public class ButtonWidget extends Widget {
	
	private ImmutableRect2i rect;
	private boolean enabled;
	private final Runnable pressedAction;
	
	public ButtonWidget(ImmutableRect2i rect, boolean enabled, Runnable pressedAction) {
		this.rect = rect;
		this.enabled = enabled;
		this.pressedAction = pressedAction;
	}
	
	public void setRect(ImmutableRect2i rect) {
		this.rect = rect;
	}
	
	public void setPosition(int x, int y) {
		setRect(rect.setPosition(x, y));
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	@Override
	public Bounds getBounds() {
		return new Bounds(rect.x(), rect.y(), rect.width(), rect.height());
	}
	
	@Override
	public void render(GuiGraphics draw, int mouseX, int mouseY, float delta) {
		final var hovering = rect.contains(mouseX, mouseY);
		final var texture = Internal.getTextures().getButtonForState(false, enabled, hovering);
		
		texture.draw(draw, rect);
	}
	
	@Override
	public boolean mouseClicked(int mouseX, int mouseY, int button) {
		if (enabled && button == 0 && rect.contains(mouseX, mouseY)) {
			pressedAction.run();
			
			return true;
		}
		
		return false;
	}
	
}
