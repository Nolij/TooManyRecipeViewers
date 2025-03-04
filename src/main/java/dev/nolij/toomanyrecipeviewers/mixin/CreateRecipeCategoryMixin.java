package dev.nolij.toomanyrecipeviewers.mixin;

import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = CreateRecipeCategory.class, remap = false)
public class CreateRecipeCategoryMixin {
	
	/**
	 * @author	Nolij
	 * @reason	Create is dumb
	 */
	@Overwrite
	public static FluidStack withImprovedVisibility(FluidStack stack) {
		return stack.copy();
	}
	
}
