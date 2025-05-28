package dev.nolij.toomanyrecipeviewers.impl.jei.api.gui.widgets;

import dev.nolij.toomanyrecipeviewers.impl.jei.api.gui.ingredient.ITMRVSlotWidget;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.gui.widgets.IScrollGridWidget;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;

import java.util.List;
import java.util.Optional;

public class ScrollGridWidget implements IScrollGridWidget {
	
	private static final int SLOT_SIZE = 18;
	
	private final List<IRecipeSlotDrawable> slots;
	private final int columns;
	private final int rows;
	private final int visibleRows;
	
	private ImmutableRect2i rect;
	
	public ScrollGridWidget(List<IRecipeSlotDrawable> slots, int columns, int visibleRows) {
		this.slots = slots;
		this.columns = columns;
		this.rows = (int) Math.ceil(((double) this.slots.size()) / (double) columns);
		this.visibleRows = visibleRows;
		
		for (final var slot : slots) {
			if (slot instanceof ITMRVSlotWidget tmrvSlotWidget) {
				tmrvSlotWidget.drawBack(true);
			}
		}
		
		this.rect = new ImmutableRect2i(0, 0, SLOT_SIZE * columns, SLOT_SIZE * rows);
		updateGrid();
	}
	
	private void updateGrid() {
		for (var i = 0; i < slots.size(); i++) {
			final var slot = slots.get(i);
			final var column = i % columns;
			final var row = i / columns;
			slot.setPosition(rect.x() + column * SLOT_SIZE, rect.y() + row * SLOT_SIZE);
		}
	}
	
	@Override
	public ScreenRectangle getScreenRectangle() {
		return rect.toScreenRectangle();
	}
	
	@Override
	public IScrollGridWidget setPosition(int x, int y) {
		rect = rect.setPosition(x + 1, y - SLOT_SIZE * (visibleRows - rows));
		updateGrid();
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
	public Optional<RecipeSlotUnderMouse> getSlotUnderMouse(double x, double y) {
		// TODO
		return Optional.empty();
	}
	
	@Override
	public ScreenPosition getPosition() {
		return rect.getScreenPosition();
	}
	
}
