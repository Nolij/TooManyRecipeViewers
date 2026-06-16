package dev.nolij.toomanyrecipeviewers.impl.widget;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.widget.AnimatedTextureWidget;
import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.gui.GuiGraphics;

public class FillingFlameWidget extends AnimatedTextureWidget {
	
	public FillingFlameWidget(int x, int y, int time) {
		super(EmiRenderHelper.WIDGETS, x, y, 14, 14, 68, 0, time, false, false, false);
	}
	
	@Override
	public void render(GuiGraphics draw, int mouseX, int mouseY, float delta) {
		EmiDrawContext context = EmiDrawContext.wrap(draw);
		context.drawTexture(this.texture, x, y, width, height, u, 14, regionWidth, regionHeight, textureWidth, textureHeight);
		super.render(context.raw(), mouseX, mouseY, delta);
	}
	
}
