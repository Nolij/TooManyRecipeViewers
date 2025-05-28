package dev.nolij.toomanyrecipeviewers.impl.jei.api.gui.widgets;

import dev.emi.emi.api.widget.WidgetHolder;
import dev.nolij.toomanyrecipeviewers.impl.jei.api.gui.ingredient.ITMRVSlotWidget;
import dev.nolij.toomanyrecipeviewers.impl.widget.ScrollBarWidget;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.gui.widgets.IScrollGridWidget;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ScrollGridWidget implements IScrollGridWidget {
	
	private static final int SLOT_SIZE = 18;
	private static final int SCROLL_BAR_WIDTH = 10;
	
	private final List<ITMRVSlotWidget> slots = new ArrayList<>();
	private final int columns;
	private final int visibleRows;
	
	private ImmutableRect2i rect;
	
	private final @Nullable ScrollBarWidget scrollBar;
	
	public ScrollGridWidget(WidgetHolder widgets, List<IRecipeSlotDrawable> slots, int columns, int visibleRows) {
		for (final var slot : slots) {
			if (slot instanceof ITMRVSlotWidget tmrvSlotWidget) {
				tmrvSlotWidget.drawBack(true);
				this.slots.add(tmrvSlotWidget);
			} else {
				throw new IllegalStateException();
			}
		}
		
		this.columns = columns;
		final var rows = (int) Math.ceil(((double) this.slots.size()) / (double) columns);
		this.visibleRows = visibleRows;
		
		if (rows > visibleRows)
			this.scrollBar = widgets.add(new ScrollBarWidget(ImmutableRect2i.EMPTY, rows, visibleRows, this::updateGrid));
		else
			this.scrollBar = null;
		this.rect = new ImmutableRect2i(0, 0, SLOT_SIZE * columns + (scrollBar == null ? 0 : SCROLL_BAR_WIDTH), SLOT_SIZE * visibleRows);
		updateGrid();
	}
	
	private void updateGrid() {
		for (var i = 0; i < slots.size(); i++) {
			final var slot = slots.get(i);
			final var column = i % columns;
			final var row = i / columns;
			final var renderRow = scrollBar == null ? row : row - scrollBar.getScroll();
			if (renderRow < 0 || renderRow >= visibleRows) {
				slot.setVisible(false);
			} else {
				slot.setVisible(true);
				slot.setPosition(rect.x() + column * SLOT_SIZE, rect.y() + renderRow * SLOT_SIZE);
			}
		}
	}
	
	@Override
	public ScreenRectangle getScreenRectangle() {
		return rect.toScreenRectangle();
	}
	
	@Override
	public IScrollGridWidget setPosition(int x, int y) {
		rect = rect.setPosition(x + 1, y);
		
		if (this.scrollBar != null)
			scrollBar.setRect(rect.keepRight(SCROLL_BAR_WIDTH).addOffset(0, -1));
		
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
