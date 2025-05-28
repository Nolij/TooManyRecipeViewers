package dev.nolij.toomanyrecipeviewers.impl.jei.api.gui.widgets;

import mezz.jei.api.gui.placement.IPlaceable;

public class DeferredPlaceableWidget implements IPlaceable<DeferredPlaceableWidget> {
	
	@FunctionalInterface
	public interface PlaceableCallback {
		void setPosition(int x, int y);
	}
	
	private final PlaceableCallback callback;
	private final int width, height;
	
	private boolean placed = false;
	
	public DeferredPlaceableWidget(PlaceableCallback callback, int width, int height) {
		this.callback = callback;
		this.width = width;
		this.height = height;
	}
	
	@Override
	public DeferredPlaceableWidget setPosition(int x, int y) {
		if (placed)
			throw new IllegalStateException();
		placed = true;
		
		callback.setPosition(x, y);
		return this;
	}
	
	@Override
	public int getWidth() {
		return width;
	}
	
	@Override
	public int getHeight() {
		return height;
	}
	
}
