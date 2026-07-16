package dev.nolij.toomanyrecipeviewers.impl.jei.common.config;

//? if >=21.1 {
import dev.nolij.toomanyrecipeviewers.impl.jei.api.runtime.config.JEIConfigValue;
import mezz.jei.api.runtime.config.IJeiConfigValue;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
//?} else {
/*import mezz.jei.common.util.HorizontalAlignment;
import mezz.jei.common.util.VerticalAlignment;
*///?}
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.util.NavigationVisibility;

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
	public IJeiConfigValue<NavigationVisibility> buttonNavigationVisibility() {
		return new JEIConfigValue<>(NavigationVisibility.DISABLED);
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
	public NavigationVisibility getButtonNavigationVisibility() {
		return NavigationVisibility.DISABLED;
	}
	*///?}
	
}
