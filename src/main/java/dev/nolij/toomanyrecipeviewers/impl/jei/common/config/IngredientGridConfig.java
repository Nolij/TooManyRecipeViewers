package dev.nolij.toomanyrecipeviewers.impl.jei.common.config;

//? if >=21.1 {
import dev.nolij.toomanyrecipeviewers.impl.jei.api.runtime.config.JEIConfigValue;
import mezz.jei.api.runtime.config.IJeiConfigValue;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
//?} else {
/*import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
*///?}
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.config.IngredientGridLayoutMode;
import mezz.jei.common.config.IngredientGridNavigationMode;
import mezz.jei.common.util.NavigationVisibility;

//? if <21.1
//import static dev.nolij.toomanyrecipeviewers.TooManyRecipeViewersMod.LOGGER;

public class IngredientGridConfig implements IIngredientGridConfig {
	
	//? if >=21.1 {
	@Override
	public IJeiConfigValue<Integer> maxColumns() {
		return new JEIConfigValue<>(0);
	}
	
	@Override
	public int getMinColumns() {
		return 0;
	}
	
	@Override
	public IJeiConfigValue<Integer> maxRows() {
		return new JEIConfigValue<>(0);
	}
	
	@Override
	public int getMinRows() {
		return 0;
	}
	
	@Override
	public IJeiConfigValue<Boolean> drawBackground() {
		return new JEIConfigValue<>(false);
	}
	
	@Override
	public IJeiConfigValue<HorizontalAlignment> horizontalAlignment() {
		return new JEIConfigValue<>(HorizontalAlignment.RIGHT);
	}
	
	@Override
	public IJeiConfigValue<VerticalAlignment> verticalAlignment() {
		return new JEIConfigValue<>(VerticalAlignment.TOP);
	}
	
	@Override
	public IJeiConfigValue<NavigationVisibility> navigationVisibility() {
		return new JEIConfigValue<>(NavigationVisibility.DISABLED);
	}
	
	@Override
	public IJeiConfigValue<IngredientGridLayoutMode> layoutMode() {
		return new JEIConfigValue<>(IngredientGridLayoutMode.RECTANGULAR);
	}
	
	@Override
	public IJeiConfigValue<IngredientGridNavigationMode> navigationMode() {
		return new JEIConfigValue<>(IngredientGridNavigationMode.PAGED);
	}
	//?} else {
	/*@Override
	public int getMaxColumns() {
		return 0;
	}
	
	@Override
	public int getMinColumns() {
		return 0;
	}
	
	@Override
	public int getMaxRows() {
		return 0;
	}
	
	@Override
	public int getMinRows() {
		return 0;
	}
	
	@Override
	public boolean drawBackground() {
		return false;
	}
	
	@Override
	public HorizontalAlignment getHorizontalAlignment() {
		return HorizontalAlignment.RIGHT;
	}
	
	@Override
	public VerticalAlignment getVerticalAlignment() {
		return VerticalAlignment.TOP;
	}
	
	@Override
	public NavigationVisibility getNavigationVisibility() {
		return NavigationVisibility.DISABLED;
	}
	
	@Override
	public IngredientGridLayoutMode getLayoutMode() {
		return IngredientGridLayoutMode.RECTANGULAR;
	}
	
	@Override
	public IngredientGridNavigationMode getNavigationMode() {
		return IngredientGridNavigationMode.PAGED;
	}
	
	@Override
	public void addLayoutListener(Runnable runnable) {
		LOGGER.error(new UnsupportedOperationException());
	}
	*///?}
	
}
