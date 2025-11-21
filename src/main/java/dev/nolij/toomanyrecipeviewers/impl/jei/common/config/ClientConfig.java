package dev.nolij.toomanyrecipeviewers.impl.jei.common.config;

import mezz.jei.common.config.BookmarkTooltipFeature;
import mezz.jei.common.config.GiveMode;
import mezz.jei.common.config.HistoryDisplaySide;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IngredientSortStage;
import mezz.jei.common.config.RecipeSorterStage;
import mezz.jei.common.config.file.IConfigListener;

import java.util.List;
import java.util.Set;

public class ClientConfig implements IClientConfig {
	
	@Override
	public boolean isCenterSearchBarEnabled() {
		return false;
	}
	
	@Override
	public boolean isLowMemorySlowSearchEnabled() {
		return false;
	}
	
	@Override
	public boolean isCatchRenderErrorsEnabled() {
		return false;
	}
	
	@Override
	public boolean isCheatToHotbarUsingHotkeysEnabled() {
		return false;
	}
	
	@Override
	public boolean isAddingBookmarksToFrontEnabled() {
		return false;
	}
	
	@Override
	public boolean isLookupFluidContentsEnabled() {
		return false;
	}
	
	@Override
	public boolean isLookupBlockTagsEnabled() {
		return false;
	}
	
	@Override
	public GiveMode getGiveMode() {
		return GiveMode.defaultGiveMode;
	}
	
	@Override
	//? if >=21.1 {
	public boolean getShowHiddenIngredients() {
	//?} else
	/*public boolean isShowHiddenItemsEnabled() {*/
		return true;
	}
	
	@Override
	public List<BookmarkTooltipFeature> getBookmarkTooltipFeatures() {
		return List.of();
	}
	
	@Override
	public boolean isHoldShiftToShowBookmarkTooltipFeaturesEnabled() {
		return false;
	}
	
	@Override
	public boolean isDragToRearrangeBookmarksEnabled() {
		return false;
	}
	
	@Override
	public int getDragDelayMs() {
		return 0;
	}
	
	@Override
	public int getSmoothScrollRate() {
		return 0;
	}
	
	@Override
	public int getMaxRecipeGuiHeight() {
		return 0;
	}
	
	@Override
	public List<IngredientSortStage> getIngredientSorterStages() {
		return List.of();
	}
	
	@Override
	public Set<RecipeSorterStage> getRecipeSorterStages() {
		return Set.of();
	}
	
	@Override
	public void enableRecipeSorterStage(RecipeSorterStage recipeSorterStage) {
		
	}
	
	@Override
	public void disableRecipeSorterStage(RecipeSorterStage recipeSorterStage) {
		
	}
	
	@Override
	public boolean isTagContentTooltipEnabled() {
		return false;
	}
	
	@Override
	//? if >=21.1 {
	public boolean getHideSingleTagContentTooltipEnabled() {
	//?} else
	/*public boolean isHideSingleIngredientTagsEnabled() {*/
		return false;
	}
	
	@Override
	public boolean isShowTagRecipesEnabled() {
		return false;
	}
	
	@Override
	public boolean isShowCreativeTabNamesEnabled() {
		return false;
	}
	
	@Override
	public boolean isLookupHistoryEnabled() {
		return false;
	}
	
	@Override
	public void setLookupHistoryEnabled(boolean b) {

	}

	@Override
	public void addLookupHistoryEnabledListener(IConfigListener<Boolean> iConfigListener) {

	}

	@Override
	public void addLookupHistoryDisplaySideListener(IConfigListener iConfigListener) {

	}

	@Override
	public int getMaxLookupHistoryRows() {
		return 0;
	}

	@Override
	public int getMaxLookupHistoryIngredients() {
		return 0;
	}

	@Override
	public HistoryDisplaySide getLookupHistoryDisplaySide() {
		return null;
	}
	
	//? if >=21.1 {
	@Override
	public boolean isIngredientsSummaryEnabled() {
		return false;
	}
	//?}
	
}
