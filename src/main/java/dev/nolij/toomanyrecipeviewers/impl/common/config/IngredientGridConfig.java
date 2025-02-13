package dev.nolij.toomanyrecipeviewers.impl.common.config;

//? if >=1.21.1 {
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
//?} else {
/*import mezz.jei.common.util.HorizontalAlignment;
import mezz.jei.common.util.VerticalAlignment;
*///?}
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.util.NavigationVisibility;

public class IngredientGridConfig implements IIngredientGridConfig {
	
	@Override
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
	public NavigationVisibility getButtonNavigationVisibility() {
		return NavigationVisibility.DISABLED;
	}
	
}
