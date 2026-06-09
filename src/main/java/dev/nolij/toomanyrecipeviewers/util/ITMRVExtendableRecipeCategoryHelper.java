package dev.nolij.toomanyrecipeviewers.util;

//? if <21.1 {
/*import mezz.jei.library.recipes.ExtendableRecipeCategoryHelper;
import org.jetbrains.annotations.Nullable;
*///?}
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryExtension;

//? if >=21.1
@SuppressWarnings("rawtypes")
public interface ITMRVExtendableRecipeCategoryHelper<T, W extends IRecipeCategoryExtension> {
	
	//? if <21.1 {
	/*<R extends T> ExtendableRecipeCategoryHelper.@Nullable RecipeHandler<R, W> tmrv$getRecipeHandler(R recipe);
	*///?}
	
}
