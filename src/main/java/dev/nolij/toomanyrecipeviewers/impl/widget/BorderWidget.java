package dev.nolij.toomanyrecipeviewers.impl.widget;

import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.gui.GuiGraphics;

public class BorderWidget extends Widget {
	
	private final Bounds bounds;
	private final int hColour, vColour;
	
	public BorderWidget(Bounds bounds, int hColour, int vColour) {
		this.bounds = bounds;
		this.hColour = hColour;
		this.vColour = vColour;
	}
	
	public BorderWidget(Bounds bounds, int colour) {
		this(bounds, colour, colour);
	}
	
	@Override
	public Bounds getBounds() {
		return bounds;
	}
	
	@Override
	public void render(GuiGraphics draw, int mouseX, int mouseY, float delta) {
		final var context = EmiDrawContext.wrap(draw);
		
		context.fill(-1, -3, bounds.width() + 2, 3, hColour);
		context.fill(-3, -1, 3, bounds.height() + 2, vColour);
	}
	
}
