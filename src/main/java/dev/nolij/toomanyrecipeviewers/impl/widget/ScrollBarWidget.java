package dev.nolij.toomanyrecipeviewers.impl.widget;

import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import mezz.jei.api.gui.placement.IPlaceable;
import mezz.jei.common.Internal;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

public class ScrollBarWidget extends Widget implements IPlaceable<ScrollBarWidget> {
	
	private ImmutableRect2i rect;
	private ImmutableRect2i upRect;
	private ImmutableRect2i downRect;
	private ImmutableRect2i midRect;
	private ImmutableRect2i scrollRect;
	private ImmutableRect2i dragRect;
	
	private int scroll = 0;
	private final int maxScroll;
	private final Runnable onScroll;
	
	public ScrollBarWidget(ImmutableRect2i rect, int rows, int visibleRows, Runnable onScroll) {
		setRect(rect);
		
		this.maxScroll = rows - visibleRows;
		this.onScroll = onScroll;
	}
	
	private boolean canScroll(int newScroll) {
		return scroll != Mth.clamp(newScroll, 0, maxScroll);
	}
	
	private boolean scroll(int newScroll) {
		final var clamped = Mth.clamp(newScroll, 0, maxScroll);
		
		if (scroll == clamped)
			return false;
		
		scroll = clamped;
		updateDragRect();
		onScroll.run();
		return true;
	}
	
	private void updateDragRect() {
		if (maxScroll == 0)
			return;
		
		final var increment = scrollRect.height() / ((double) maxScroll + 1);
		this.dragRect = scrollRect
			.keepTop((int) increment)
			.addOffset(0, (int) (increment * (double) scroll));
	}
	
	public int getScroll() {
		return scroll;
	}
	
	public void setRect(ImmutableRect2i rect) {
		this.rect = rect;
		
		upRect = rect.keepTop(rect.width());
		downRect = rect.keepBottom(rect.width());
		midRect = rect.cropTop(upRect.height()).cropBottom(downRect.height());
		scrollRect = midRect.insetBy(1);
		updateDragRect();
	}
	
	@Override
	public ScrollBarWidget setPosition(int x, int y) {
		setRect(rect.setPosition(x, y));
		return this;
	}
	
	@Override
	public int getWidth() {
		return rect.width();
	}
	
	@Override
	public int getHeight() {
		return rect.height();
	}
	
	@Override
	public Bounds getBounds() {
		return new Bounds(rect.x(), rect.y(), rect.width(), rect.height());
	}
	
	@Override
	public void render(GuiGraphics draw, int mouseX, int mouseY, float delta) {
		Internal.getTextures().getButtonForState(false, canScroll(scroll - 1), upRect.contains(mouseX, mouseY)).draw(draw, upRect);
		Internal.getTextures().getButtonForState(false, canScroll(scroll + 1), downRect.contains(mouseX, mouseY)).draw(draw, downRect);
		Internal.getTextures().getButtonForState(false, false, false).draw(draw, midRect);
		Internal.getTextures().getButtonForState(false, true, dragRect.contains(mouseX, mouseY)).draw(draw, dragRect);
	}
	
	@Override
	public boolean mouseClicked(int mouseX, int mouseY, int button) {
		if (!rect.contains(mouseX, mouseY))
			return false;
		
		if (upRect.contains(mouseX, mouseY)) {
			return scroll(scroll - 1);
		} else if (downRect.contains(mouseX, mouseY)) {
			return scroll(scroll + 1);
		} else if (midRect.contains(mouseX, mouseY)) {
			return scroll((int) ((mouseY - scrollRect.y()) / (double) scrollRect.height() * ((double) maxScroll + 1)));
		}
		
		return false;
	}
	
}
