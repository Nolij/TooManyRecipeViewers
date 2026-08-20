package dev.nolij.toomanyrecipeviewers.impl.jei.common.config;

//? if >=21.1 {
import dev.nolij.toomanyrecipeviewers.impl.jei.api.runtime.config.JEIConfigValue;
import mezz.jei.api.runtime.config.IJeiConfigValue;
//?}
import mezz.jei.common.config.IIngredientFilterConfig;
import mezz.jei.common.search.SearchMode;

//? if <21.1
//import static dev.nolij.toomanyrecipeviewers.TooManyRecipeViewersMod.LOGGER;

public class IngredientFilterConfig implements IIngredientFilterConfig {
	
	//? if >=21.1 {
	@Override
	public IJeiConfigValue<SearchMode> modNameSearchMode() {
		return new JEIConfigValue<>(SearchMode.DISABLED);
	}
	
	@Override
	public IJeiConfigValue<SearchMode> tooltipSearchMode() {
		return new JEIConfigValue<>(SearchMode.DISABLED);
	}
	
	@Override
	public IJeiConfigValue<SearchMode> tagSearchMode() {
		return new JEIConfigValue<>(SearchMode.DISABLED);
	}
	
	@Override
	public IJeiConfigValue<SearchMode> colorSearchMode() {
		return new JEIConfigValue<>(SearchMode.DISABLED);
	}
	
	@Override
	public IJeiConfigValue<SearchMode> resourceLocationSearchMode() {
		return new JEIConfigValue<>(SearchMode.DISABLED);
	}
	
	@Override
	public IJeiConfigValue<SearchMode> creativeTabSearchMode() {
		return new JEIConfigValue<>(SearchMode.DISABLED);
	}
	
	@Override
	public IJeiConfigValue<Boolean> searchAdvancedTooltips() {
		return new JEIConfigValue<>(false);
	}
	
	@Override
	public IJeiConfigValue<Boolean> searchModIds() {
		return new JEIConfigValue<>(false);
	}
	
	@Override
	public IJeiConfigValue<Boolean> searchModAliases() {
		return new JEIConfigValue<>(false);
	}
	
	@Override
	public IJeiConfigValue<Boolean> searchIngredientAliases() {
		return new JEIConfigValue<>(false);
	}
	
	@Override
	public IJeiConfigValue<Boolean> searchShortModNames() {
		return new JEIConfigValue<>(false);
	}
	//?} else {
	/*@Override
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
	public boolean getSearchIngredientAliases() {
		return false;
	}
	
	@Override
	public boolean getSearchShortModNames() {
		return false;
	}
	
	@Override
	public boolean getSearchModAliases() {
		return false;
	}
	
	@Override
	public void addSearchConfigListener(Runnable runnable) {
		LOGGER.error(new UnsupportedOperationException());
	}
	*///?}
	
}
