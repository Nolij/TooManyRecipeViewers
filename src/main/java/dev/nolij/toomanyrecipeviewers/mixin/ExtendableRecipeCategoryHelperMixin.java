package dev.nolij.toomanyrecipeviewers.mixin;

//? if <21.1 {
/*import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import mezz.jei.library.recipes.ExtendableRecipeCategoryHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Map;
import java.util.Optional;
*///?}
import dev.nolij.toomanyrecipeviewers.util.ITMRVExtendableRecipeCategoryHelper;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryExtension;
import org.spongepowered.asm.mixin.Mixin;

//? if >=21.1
@SuppressWarnings("rawtypes")
@Mixin(targets = "mezz/jei/library/recipes/ExtendableRecipeCategoryHelper", remap = false)
public abstract class ExtendableRecipeCategoryHelperMixin<T, W extends IRecipeCategoryExtension> implements ITMRVExtendableRecipeCategoryHelper<T, W> {
	
	//? if <21.1 {
	/*@Shadow
	protected abstract Optional<ExtendableRecipeCategoryHelper.RecipeHandler<Object, W>> getBestRecipeHandler(Object recipe);
	
	@Unique
	private final Map<Object, ExtendableRecipeCategoryHelper.RecipeHandler<Object, W>> tmrv$recipeHandlerCache = new Reference2ReferenceOpenHashMap<>();
	
	@SuppressWarnings("unchecked")
	@Override
	public ExtendableRecipeCategoryHelper.@Nullable RecipeHandler<Object, W> tmrv$getRecipeHandler(Object recipe) {
		return tmrv$recipeHandlerCache.computeIfAbsent(recipe, r -> getBestRecipeHandler(r).orElse(null));
	}
	*///?}
	
}
