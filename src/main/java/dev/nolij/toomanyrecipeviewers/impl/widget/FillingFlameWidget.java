package dev.nolij.toomanyrecipeviewers.impl.widget;

import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.widget.AnimatedTextureWidget;
import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.gui.GuiGraphics;

public class FillingFlameWidget extends AnimatedTextureWidget {
	
	public FillingFlameWidget(int x, int y, int time) {
		super(EmiTexture.FULL_FLAME.texture, x, y,
				EmiTexture.FULL_FLAME.width, EmiTexture.FULL_FLAME.height,
				EmiTexture.FULL_FLAME.u, EmiTexture.FULL_FLAME.v,
				EmiTexture.FULL_FLAME.regionWidth, EmiTexture.FULL_FLAME.regionHeight,
				EmiTexture.FULL_FLAME.textureWidth, EmiTexture.FULL_FLAME.textureHeight,
				time, false, true, true);
	}
	
	@Override
	public void render(GuiGraphics draw, int mouseX, int mouseY, float delta) {
		EmiDrawContext context = EmiDrawContext.wrap(draw);

		context.drawTexture(EmiTexture.EMPTY_FLAME.texture, x, y,
				EmiTexture.EMPTY_FLAME.width, EmiTexture.EMPTY_FLAME.height,
				EmiTexture.EMPTY_FLAME.u, EmiTexture.EMPTY_FLAME.v,
				EmiTexture.EMPTY_FLAME.regionWidth, EmiTexture.EMPTY_FLAME.regionHeight,
				EmiTexture.EMPTY_FLAME.textureWidth, EmiTexture.EMPTY_FLAME.textureHeight);

		super.render(context.raw(), mouseX, mouseY, delta);
	}
}
