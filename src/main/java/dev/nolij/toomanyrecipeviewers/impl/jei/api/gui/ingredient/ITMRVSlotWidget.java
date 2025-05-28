package dev.nolij.toomanyrecipeviewers.impl.jei.api.gui.ingredient;

import dev.emi.emi.api.widget.SlotWidget;
import dev.nolij.toomanyrecipeviewers.impl.jei.api.gui.drawable.OffsetDrawable;
import dev.nolij.toomanyrecipeviewers.impl.jei.api.gui.builder.TMRVIngredientCollector;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("UnusedReturnValue")
public interface ITMRVSlotWidget {
	
	void setName(@Nullable String name);
	
	void setBackground(@Nullable OffsetDrawable background);
	
	void setOverlay(@Nullable OffsetDrawable overlay);
	
	void addTooltipCallbacks(List<IRecipeSlotRichTooltipCallback> tooltipCallbacks);
	
	void setPosition(int x, int y);
	
	void setVisible(boolean visible);
	
	TMRVIngredientCollector getIngredientCollector();
	
	SlotWidget drawBack(boolean drawBack);
	
	SlotWidget large(boolean large);
	
}
