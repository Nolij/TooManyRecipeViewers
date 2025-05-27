package dev.nolij.toomanyrecipeviewers.mixin;

import mezz.jei.api.recipe.category.extensions.vanilla.smithing.ISmithingCategoryExtension;
import mezz.jei.library.plugins.vanilla.anvil.SmithingRecipeCategory;
import net.minecraft.world.item.crafting.SmithingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = SmithingRecipeCategory.class, remap = false)
public interface SmithingRecipeCategoryAccessor {
	
	@Invoker("getExtension") <R extends SmithingRecipe> ISmithingCategoryExtension<? super R> tmrv$getExtension(SmithingRecipe recipe);
	
}
