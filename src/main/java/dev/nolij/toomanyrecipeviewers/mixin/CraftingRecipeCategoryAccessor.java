package dev.nolij.toomanyrecipeviewers.mixin;

//? if >=21.1 {
import mezz.jei.library.recipes.CraftingExtensionHelper;
//?} else {
/*import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import mezz.jei.library.recipes.ExtendableRecipeCategoryHelper;
import net.minecraft.world.item.crafting.Recipe;
*///?}
import mezz.jei.library.plugins.vanilla.crafting.CraftingRecipeCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = CraftingRecipeCategory.class, remap = false)
public interface CraftingRecipeCategoryAccessor {
	
	//? if >=21.1 {
	@Accessor("extendableHelper") CraftingExtensionHelper tmrv$getExtendableHelper();
	//?} else {
	/*@Accessor("extendableHelper") ExtendableRecipeCategoryHelper<Recipe<?>, ICraftingCategoryExtension> tmrv$getExtendableHelper();
	*///?}
	
}
