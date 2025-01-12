package mezz.jei.gui.startup;

import mezz.jei.api.helpers.ICodecHelper;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.gui.bookmarks.IBookmark;
import mezz.jei.gui.config.IBookmarkConfig;
import mezz.jei.gui.config.IngredientTypeSortingConfig;
import mezz.jei.gui.config.ModNameSortingConfig;
import net.minecraft.core.RegistryAccess;

import java.util.List;

public record GuiConfigData(
	IBookmarkConfig bookmarkConfig,
	ModNameSortingConfig modNameSortingConfig,
	IngredientTypeSortingConfig ingredientTypeSortingConfig
) {
	
	public static GuiConfigData create() {
		return new GuiConfigData(
			new IBookmarkConfig() {
				@Override
				public boolean saveBookmarks(IRecipeManager recipeManager, IFocusFactory focusFactory, IGuiHelper guiHelper, IIngredientManager ingredientManager, RegistryAccess registryAccess, ICodecHelper codecHelper, List<IBookmark> bookmarks) {
					return false;
				}
				
				@Override
				public void loadBookmarks(IRecipeManager recipeManager, IFocusFactory focusFactory, IGuiHelper guiHelper, IIngredientManager ingredientManager, RegistryAccess registryAccess, BookmarkList bookmarkList, ICodecHelper codecHelper) {}
			},
			new ModNameSortingConfig(),
			new IngredientTypeSortingConfig()
		);
	}
	
}
