package dev.nolij.toomanyrecipeviewers.impl.jei.api.gui.widgets;

import dev.emi.emi.api.widget.WidgetHolder;
import dev.nolij.toomanyrecipeviewers.impl.jei.api.gui.ingredient.ITMRVSlotWidget;
import dev.nolij.toomanyrecipeviewers.impl.widget.ButtonWidget;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.gui.widgets.IScrollGridWidget;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ScrollGridWidget implements IScrollGridWidget {
	
	private static final int SLOT_SIZE = 18;
	private static final int SCROLL_BAR_WIDTH = 10;
	
	private final List<ITMRVSlotWidget> slots = new ArrayList<>();
	private final int columns;
	private final int rows;
	private final int visibleRows;
	
	private ImmutableRect2i rect;
	private int scroll = 0;
	
	public ScrollGridWidget(List<IRecipeSlotDrawable> slots, int columns, int visibleRows) {
		for (final var slot : slots) {
			if (slot instanceof ITMRVSlotWidget tmrvSlotWidget) {
				tmrvSlotWidget.drawBack(true);
				this.slots.add(tmrvSlotWidget);
			} else {
				throw new IllegalStateException();
			}
		}
		
		this.columns = columns;
		this.rows = (int) Math.ceil(((double) this.slots.size()) / (double) columns);
		this.visibleRows = visibleRows;
		if (visibleRows > rows)
			scroll = -1;
		
		this.rect = new ImmutableRect2i(0, 0, SLOT_SIZE * columns + (scroll == -1 ? 0 : SCROLL_BAR_WIDTH), SLOT_SIZE * visibleRows);
		updateGrid();
	}
	
	private void updateGrid() {
		for (var i = 0; i < slots.size(); i++) {
			final var slot = slots.get(i);
			final var column = i % columns;
			final var row = i / columns;
			final var renderRow = scroll == -1 ? row : row - scroll;
			if (renderRow < 0 || renderRow >= visibleRows) {
				slot.setVisible(false);
			} else {
				slot.setVisible(true);
				slot.setPosition(rect.x() + column * SLOT_SIZE, rect.y() + renderRow * SLOT_SIZE);
			}
		}
	}
	
	public void scroll(int amount) {
		if (scroll == -1)
			return;
		
		this.scroll = Mth.clamp(scroll + amount, 0, rows - visibleRows);
		updateGrid();
	}
	
	public ScrollGridWidget addScrollWidgets(WidgetHolder widgets) {
		if (scroll == -1)
			return this;
		
		final var scrollArea = rect.keepRight(SCROLL_BAR_WIDTH).addOffset(2, SLOT_SIZE + 1);
		
		widgets.add(new ButtonWidget(scrollArea.keepTop(scrollArea.getHeight() / 2), () -> scroll > 0, () -> scroll(-1)));
		widgets.add(new ButtonWidget(scrollArea.keepBottom(scrollArea.getHeight() / 2), () -> scroll < rows - visibleRows, () -> scroll(1)));
		
		return this;
	}
	
	@Override
	public ScreenRectangle getScreenRectangle() {
		return rect.toScreenRectangle();
	}
	
	@Override
	public IScrollGridWidget setPosition(int x, int y) {
		rect = rect.setPosition(x, y);
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
