package dev.nolij.toomanyrecipeviewers.impl.jei.common.config;

//? if >=21.1 {
import dev.nolij.toomanyrecipeviewers.impl.jei.api.runtime.config.JEIConfigValue;
import mezz.jei.api.runtime.config.IJeiConfigValue;
//?} else {
/*import mezz.jei.common.config.file.IConfigListener;

import java.util.Set;
*///?}
import mezz.jei.common.config.BookmarkTooltipFeature;
import mezz.jei.common.config.GiveMode;
import mezz.jei.common.config.HistoryDisplaySide;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IngredientSortStage;
import mezz.jei.common.config.RecipeSorterStage;

import java.util.List;

public class ClientConfig implements IClientConfig {
	
	//? if >=21.1 {
	@Override
	public IJeiConfigValue<Boolean> centerSearchBarEnabled() {
		return new JEIConfigValue<>(false);
	}
	
	@Override
	public IJeiConfigValue<Integer> maxRecipeGuiHeight() {
		return new JEIConfigValue<>(0);
	}
	
	@Override
	public IJeiConfigValue<Boolean> toastReflowEnabled() {
		return new JEIConfigValue<>(false);
	}
	
	@Override
	public IJeiConfigValue<GiveMode> giveMode() {
		return new JEIConfigValue<>(GiveMode.defaultGiveMode);
	}
	
	@Override
	public IJeiConfigValue<Boolean> cheatToHotbarUsingHotkeysEnabled() {
		return new JEIConfigValue<>(false);
	}
	
	@Override
	public IJeiConfigValue<Boolean> showHiddenIngredients() {
		return new JEIConfigValue<>(true);
	}
	
	@Override
	public IJeiConfigValue<Boolean> showTagRecipesEnabled() {
		return new JEIConfigValue<>(false);
	}
	
	@Override
	public IJeiConfigValue<Boolean> addBookmarksToFrontEnabled() {
		return new JEIConfigValue<>(false);
	}
	
	@Override
	public IJeiConfigValue<List<BookmarkTooltipFeature>> bookmarkTooltipFeatures() {
		return new JEIConfigValue<>(List.of());
	}
	
	@Override
	public IJeiConfigValue<Boolean> holdShiftToShowBookmarkTooltipFeaturesEnabled() {
		return new JEIConfigValue<>(false);
	}
	
	@Override
	public IJeiConfigValue<Boolean> dragToRearrangeBookmarksEnabled() {
		return new JEIConfigValue<>(false);
	}
	
	@Override
	public IJeiConfigValue<Boolean> lookupHistoryEnabled() {
		return new JEIConfigValue<>(false);
	}
	
	@Override
	public IJeiConfigValue<Integer> maxLookupHistoryRows() {
		return new JEIConfigValue<>(0);
	}
	
	@Override
	public IJeiConfigValue<Integer> maxLookupHistoryIngredients() {
		return new JEIConfigValue<>(0);
	}
	
	@Override
	public IJeiConfigValue<HistoryDisplaySide> lookupHistoryDisplaySide() {
		return new JEIConfigValue<>(null);
	}
	
	@Override
	public IJeiConfigValue<Boolean> ingredientsSummaryEnabled() {
		return new JEIConfigValue<>(false);
	}
	
	@Override
	public IJeiConfigValue<Boolean> lowMemorySlowSearchEnabled() {
		return new JEIConfigValue<>(false);
	}
	
	@Override
	public IJeiConfigValue<Boolean> catchRenderErrorsEnabled() {
		return new JEIConfigValue<>(false);
	}
	
	@Override
	public IJeiConfigValue<Boolean> lookupFluidContentsEnabled() {
		return new JEIConfigValue<>(false);
	}
	
	@Override
	public IJeiConfigValue<Boolean> lookupBlockTagsEnabled() {
		return new JEIConfigValue<>(false);
	}
	
	@Override
	public IJeiConfigValue<Boolean> showCreativeTabNamesEnabled() {
		return new JEIConfigValue<>(false);
	}
	
	@Override
	public IJeiConfigValue<Integer> dragDelayMs() {
		return new JEIConfigValue<>(0);
	}
	
	@Override
	public IJeiConfigValue<Integer> smoothScrollRate() {
		return new JEIConfigValue<>(0);
	}
	
	@Override
	public IJeiConfigValue<List<IngredientSortStage>> ingredientSorterStages() {
		return new JEIConfigValue<>(List.of());
	}
	
	@Override
	public IJeiConfigValue<List<RecipeSorterStage>> recipeSorterStages() {
		return new JEIConfigValue<>(List.of());
	}
	
	@Override
	public IJeiConfigValue<Boolean> tagContentTooltipEnabled() {
		return new JEIConfigValue<>(false);
	}
	
	@Override
	public IJeiConfigValue<Boolean> hideSingleTagContentTooltipEnabled() {
		return new JEIConfigValue<>(false);
	}
	//?} else {
	/*@Override
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
	public boolean isShowHiddenItemsEnabled() {
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
	public boolean isHideSingleIngredientTagsEnabled() {
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
	*///?}
	
}
