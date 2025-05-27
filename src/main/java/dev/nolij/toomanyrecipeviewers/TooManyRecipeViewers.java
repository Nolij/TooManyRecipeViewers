package dev.nolij.toomanyrecipeviewers;

//? if >=21.1
import mezz.jei.api.helpers.ICodecHelper;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSetMultimap;
import dev.emi.emi.api.EmiRegistry;
import dev.nolij.toomanyrecipeviewers.impl.jei.api.recipe.RecipeManager;
import dev.nolij.toomanyrecipeviewers.impl.jei.api.runtime.IngredientManager;
import dev.nolij.toomanyrecipeviewers.impl.jei.api.runtime.JEIKeyMappings;
import dev.nolij.toomanyrecipeviewers.impl.jei.api.runtime.JEIRuntime;
import dev.nolij.toomanyrecipeviewers.impl.jei.api.runtime.config.JEIConfigManager;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
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
import mezz.jei.common.platform.IPlatformFluidHelperInternal;
import mezz.jei.common.util.StackHelper;
import mezz.jei.library.config.EditModeConfig;
import mezz.jei.library.gui.helpers.GuiHelper;
import mezz.jei.library.ingredients.IngredientBlacklistInternal;
import mezz.jei.library.ingredients.subtypes.SubtypeManager;
import mezz.jei.library.plugins.vanilla.anvil.SmithingRecipeCategory;
import mezz.jei.library.plugins.vanilla.crafting.CraftingRecipeCategory;
import mezz.jei.library.runtime.JeiHelpers;
import mezz.jei.neoforge.platform.FluidHelper;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public final class TooManyRecipeViewers {
	
	public static volatile TooManyRecipeViewers runtime = null;
	
	//region Storage
	public volatile EmiRegistry emiRegistry = null;
	public volatile SubtypeManager subtypeManager = null;
	public volatile StackHelper stackHelper = null;
	public volatile IColorHelper colorHelper = null;
	public volatile IngredientManager ingredientManager = null;
	public volatile GuiHelper guiHelper = null;
	public volatile IFocusFactory focusFactory = null;
	//? if >=21.1
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
	public volatile @Unmodifiable List<IRecipeCategory<?>> recipeCategories = null;
	public volatile ImmutableListMultimap<RecipeType<?>, ITypedIngredient<?>> recipeCatalysts = null;
	public final Set<Object> ignoredRecipes = new HashSet<>();
	public volatile RecipeManager recipeManager = null;
	public volatile IRecipeTransferManager recipeTransferManager = null;
	public volatile IScreenHelper screenHelper = null;
	public volatile JEIRuntime jeiRuntime = null;
	//endregion
	
	public interface ILockable {
		void lock() throws IllegalStateException;
	}
	
	private volatile boolean registrationLocked = false;
	private final List<ILockable> lockAfterRegistration = Collections.synchronizedList(new ArrayList<>());
	
	public synchronized void lockAfterRegistration(ILockable lockable) throws IllegalStateException {
		if (registrationLocked)
			throw new IllegalStateException();
		
		lockAfterRegistration.add(lockable);
	}
	
	public synchronized void lockRegistration() throws IllegalStateException {
		if (registrationLocked)
			throw new IllegalStateException();
		registrationLocked = true;
		
		lockAfterRegistration.forEach(ILockable::lock);
		lockAfterRegistration.clear();
	}
	
	//region Static Storage
	public static final JEIConfigManager jeiConfigManager = new JEIConfigManager();
	public static final IPlatformFluidHelperInternal<?> fluidHelper = new FluidHelper();
	public static final IInternalKeyMappings jeiKeyMappings = new JEIKeyMappings();
	
	public static final IJeiHelpers staticJEIHelpers = new IJeiHelpers() {
		@Override
		public IGuiHelper getGuiHelper() {
			return runtime.guiHelper;
		}
		
		@Override
		public IStackHelper getStackHelper() {
			return runtime.stackHelper;
		}
		
		@Override
		public IModIdHelper getModIdHelper() {
			return runtime.modIdHelper;
		}
		
		@Override
		public IFocusFactory getFocusFactory() {
			return runtime.focusFactory;
		}
		
		@Override
		public IColorHelper getColorHelper() {
			return runtime.colorHelper;
		}
		
		@Override
		public IPlatformFluidHelper<?> getPlatformFluidHelper() {
			return fluidHelper;
		}
		
		@Override
		public <T> Optional<RecipeType<T>> getRecipeType(ResourceLocation recipeUid, Class<? extends T> recipeClass) {
			if (runtime == null || runtime.recipeManager == null)
				return Optional.empty();
			
			return runtime.recipeManager.getRecipeType(recipeUid, recipeClass);
		}
		
		//? if <21.1
		/*@SuppressWarnings("removal")*/
		@Override
		public Optional<RecipeType<?>> getRecipeType(ResourceLocation recipeUid) {
			if (runtime == null || runtime.recipeManager == null)
				return Optional.empty();
			
			return runtime.recipeManager.getRecipeType(recipeUid);
		}
		
		@Override
		public Stream<RecipeType<?>> getAllRecipeTypes() {
			if (runtime == null || runtime.recipeManager == null)
				return Stream.empty();
			
			return runtime.recipeManager.getAllRecipeTypes();
		}
		
		@Override
		public IIngredientManager getIngredientManager() {
			return runtime.ingredientManager;
		}
		
		//? if >=21.1 {
		@Override
		public ICodecHelper getCodecHelper() {
			return runtime.codecHelper;
		}
		//?}
		
		@Override
		public IVanillaRecipeFactory getVanillaRecipeFactory() {
			return runtime.vanillaRecipeFactory;
		}
		
		@Override
		public IIngredientVisibility getIngredientVisibility() {
			return runtime.ingredientVisibility;
		}
	};
	
	public static final IJeiRuntime staticJEIRuntime = new IJeiRuntime() {
		@Override
		public IRecipeManager getRecipeManager() {
			return runtime.recipeManager;
		}
		
		@Override
		public IRecipesGui getRecipesGui() {
			return runtime.jeiRuntime.getRecipesGui();
		}
		
		@Override
		public IIngredientFilter getIngredientFilter() {
			return runtime.jeiRuntime.getIngredientFilter();
		}
		
		@Override
		public IIngredientListOverlay getIngredientListOverlay() {
			return runtime.jeiRuntime.getIngredientListOverlay();
		}
		
		@Override
		public IBookmarkOverlay getBookmarkOverlay() {
			return runtime.jeiRuntime.getBookmarkOverlay();
		}
		
		@Override
		public IJeiHelpers getJeiHelpers() {
			return staticJEIHelpers;
		}
		
		@Override
		public IIngredientManager getIngredientManager() {
			return runtime.ingredientManager;
		}
		
		@Override
		public IJeiKeyMappings getKeyMappings() {
			return jeiKeyMappings;
		}
		
		@Override
		public IScreenHelper getScreenHelper() {
			return runtime.screenHelper;
		}
		
		@Override
		public IRecipeTransferManager getRecipeTransferManager() {
			return runtime.recipeTransferManager;
		}
		
		@Override
		public IEditModeConfig getEditModeConfig() {
			return runtime.editModeConfig;
		}
		
		@Override
		public IJeiConfigManager getConfigManager() {
			return jeiConfigManager;
		}
	};
	//endregion
	
}
