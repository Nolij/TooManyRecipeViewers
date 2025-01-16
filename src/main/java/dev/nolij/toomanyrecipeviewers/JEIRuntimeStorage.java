package dev.nolij.toomanyrecipeviewers;

import com.google.common.collect.ImmutableSetMultimap;
import dev.nolij.toomanyrecipeviewers.impl.api.recipe.RecipeManager;
import dev.nolij.toomanyrecipeviewers.impl.api.runtime.JEIKeyMappings;
import dev.nolij.toomanyrecipeviewers.impl.api.runtime.JEIRuntime;
import dev.nolij.toomanyrecipeviewers.impl.api.runtime.config.JEIConfigManager;
import mezz.jei.api.helpers.ICodecHelper;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferManager;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.runtime.IBookmarkOverlay;
import mezz.jei.api.runtime.IEditModeConfig;
import mezz.jei.api.runtime.IIngredientFilter;
import mezz.jei.api.runtime.IIngredientListOverlay;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.api.runtime.IJeiKeyMappings;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.api.runtime.config.IJeiConfigManager;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.library.config.EditModeConfig;
import mezz.jei.library.gui.helpers.GuiHelper;
import mezz.jei.library.ingredients.IngredientBlacklistInternal;
import mezz.jei.library.ingredients.subtypes.SubtypeManager;
import mezz.jei.library.plugins.vanilla.anvil.SmithingRecipeCategory;
import mezz.jei.library.plugins.vanilla.crafting.CraftingRecipeCategory;
import mezz.jei.library.runtime.JeiHelpers;
import mezz.jei.neoforge.platform.FluidHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

import java.util.Optional;
import java.util.stream.Stream;

public final class JEIRuntimeStorage {
	
	public volatile SubtypeManager subtypeManager = null;
	public volatile IStackHelper stackHelper = null;
	public volatile IColorHelper colorHelper = null;
	public volatile IIngredientManager ingredientManager = null;
	public volatile GuiHelper guiHelper = null;
	public volatile IFocusFactory focusFactory = null;
	public volatile ICodecHelper codecHelper = null;
	public volatile IVanillaRecipeFactory vanillaRecipeFactory = null;
	public volatile IngredientBlacklistInternal blacklist = null;
	public volatile IClientToggleState clientToggleState = null;
	public volatile EditModeConfig editModeConfig = null;
	public volatile IIngredientVisibility ingredientVisibility = null;
	public volatile ImmutableSetMultimap<String, String> modAliases = null;
	public volatile IModIdHelper modIdHelper = null;
	public volatile JeiHelpers jeiHelpers = null;
	public volatile CraftingRecipeCategory craftingCategory = null;
	public volatile SmithingRecipeCategory smithingCategory = null;
	public volatile RecipeManager recipeManager = null;
	public volatile IRecipeTransferManager recipeTransferManager = null;
	public volatile IScreenHelper screenHelper = null;
	public volatile ResourceManagerReloadListener resourceReloadHandler = null;
	public volatile JEIRuntime jeiRuntime = null;
	
	public static volatile JEIRuntimeStorage storage = null;
	
	public static final JEIConfigManager jeiConfigManager = new JEIConfigManager();
	public static final IPlatformFluidHelper<?> fluidHelper = new FluidHelper();
	public static final IInternalKeyMappings jeiKeyMappings = new JEIKeyMappings();
	
	public static final IJeiHelpers staticJEIHelpers = new IJeiHelpers() {
		@Override
		public IGuiHelper getGuiHelper() {
			return storage.guiHelper;
		}
		
		@Override
		public IStackHelper getStackHelper() {
			return storage.stackHelper;
		}
		
		@Override
		public IModIdHelper getModIdHelper() {
			return storage.modIdHelper;
		}
		
		@Override
		public IFocusFactory getFocusFactory() {
			return storage.focusFactory;
		}
		
		@Override
		public IColorHelper getColorHelper() {
			return storage.colorHelper;
		}
		
		@Override
		public IPlatformFluidHelper<?> getPlatformFluidHelper() {
			return fluidHelper;
		}
		
		@Override
		public <T> Optional<RecipeType<T>> getRecipeType(ResourceLocation recipeUid, Class<? extends T> recipeClass) {
			if (storage == null || storage.recipeManager == null)
				return Optional.empty();
			
			return storage.recipeManager.getRecipeType(recipeUid, recipeClass);
		}
		
		@Override
		public Optional<RecipeType<?>> getRecipeType(ResourceLocation recipeUid) {
			if (storage == null || storage.recipeManager == null)
				return Optional.empty();
			
			return storage.recipeManager.getRecipeType(recipeUid);
		}
		
		@Override
		public Stream<RecipeType<?>> getAllRecipeTypes() {
			if (storage == null || storage.recipeManager == null)
				return Stream.empty();
			
			return storage.recipeManager.getAllRecipeTypes();
		}
		
		@Override
		public IIngredientManager getIngredientManager() {
			return storage.ingredientManager;
		}
		
		@Override
		public ICodecHelper getCodecHelper() {
			return storage.codecHelper;
		}
		
		@Override
		public IVanillaRecipeFactory getVanillaRecipeFactory() {
			return storage.vanillaRecipeFactory;
		}
		
		@Override
		public IIngredientVisibility getIngredientVisibility() {
			return storage.ingredientVisibility;
		}
	};
	
	public static final IJeiRuntime staticJEIRuntime = new IJeiRuntime() {
		@Override
		public IRecipeManager getRecipeManager() {
			return storage.recipeManager;
		}
		
		@Override
		public IRecipesGui getRecipesGui() {
			return storage.jeiRuntime.getRecipesGui();
		}
		
		@Override
		public IIngredientFilter getIngredientFilter() {
			return storage.jeiRuntime.getIngredientFilter();
		}
		
		@Override
		public IIngredientListOverlay getIngredientListOverlay() {
			return storage.jeiRuntime.getIngredientListOverlay();
		}
		
		@Override
		public IBookmarkOverlay getBookmarkOverlay() {
			return storage.jeiRuntime.getBookmarkOverlay();
		}
		
		@Override
		public IJeiHelpers getJeiHelpers() {
			return staticJEIHelpers;
		}
		
		@Override
		public IIngredientManager getIngredientManager() {
			return storage.ingredientManager;
		}
		
		@Override
		public IJeiKeyMappings getKeyMappings() {
			return jeiKeyMappings;
		}
		
		@Override
		public IScreenHelper getScreenHelper() {
			return storage.screenHelper;
		}
		
		@Override
		public IRecipeTransferManager getRecipeTransferManager() {
			return storage.recipeTransferManager;
		}
		
		@Override
		public IEditModeConfig getEditModeConfig() {
			return storage.editModeConfig;
		}
		
		@Override
		public IJeiConfigManager getConfigManager() {
			return jeiConfigManager;
		}
	};
	
}
