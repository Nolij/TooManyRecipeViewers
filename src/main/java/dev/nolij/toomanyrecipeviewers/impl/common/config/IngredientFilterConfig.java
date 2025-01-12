package dev.nolij.toomanyrecipeviewers.impl.common.config;

import mezz.jei.common.config.IIngredientFilterConfig;
import mezz.jei.core.search.SearchMode;

public class IngredientFilterConfig implements IIngredientFilterConfig {
	
	@Override
	public SearchMode getModNameSearchMode() {
		return SearchMode.DISABLED;
	}
	
	@Override
	public SearchMode getTooltipSearchMode() {
		return SearchMode.DISABLED;
	}
	
	@Override
	public SearchMode getTagSearchMode() {
		return SearchMode.DISABLED;
	}
	
	@Override
	public SearchMode getColorSearchMode() {
		return SearchMode.DISABLED;
	}
	
	@Override
	public SearchMode getResourceLocationSearchMode() {
		return SearchMode.DISABLED;
	}
	
	@Override
	public SearchMode getCreativeTabSearchMode() {
		return SearchMode.DISABLED;
	}
	
	@Override
	public boolean getSearchAdvancedTooltips() {
		return false;
	}
	
	@Override
	public boolean getSearchModIds() {
		return false;
	}
	
	@Override
	public boolean getSearchModAliases() {
		return false;
	}
	
	@Override
	public boolean getSearchIngredientAliases() {
		return false;
	}
	
	@Override
	public boolean getSearchShortModNames() {
		return false;
	}
	
}
