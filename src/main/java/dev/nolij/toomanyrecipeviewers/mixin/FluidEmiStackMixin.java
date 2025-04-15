package dev.nolij.toomanyrecipeviewers.mixin;

//? if >=21.1 {
import net.minecraft.core.Holder;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
//?}
import dev.emi.emi.api.stack.FluidEmiStack;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import static dev.nolij.toomanyrecipeviewers.TooManyRecipeViewers.fluidHelper;

@SuppressWarnings({"UnstableApiUsage", "NonExtendableApiUsage"})
@Mixin(value = FluidEmiStack.class, remap = false)
public class FluidEmiStackMixin implements ITypedIngredient<FluidStack> {
	
	@Shadow @Final private Fluid fluid;
	//? if >=21.1 {
	@Shadow @Final private DataComponentPatch componentChanges;
	//?} else
	/*@Shadow @Final private CompoundTag nbt;*/
	
	@Override
	public IIngredientType<FluidStack> getType() {
		//noinspection unchecked
		return (IIngredientType<FluidStack>) fluidHelper.getFluidIngredientType();
	}
	
	@Override
	public FluidStack getIngredient() {
		var amount = ((FluidEmiStack) (Object) this).getAmount();
		if (amount == 0L)
			amount = 1000L;
		
		//? if >=21.1 {
		return (FluidStack) fluidHelper.create(Holder.direct(fluid), amount, componentChanges);
		//?} else
		/*return (FluidStack) fluidHelper.create(fluid, amount, nbt);*/
	}
	
	//? if >=21.1 {
	@Override
	public <B> B getBaseIngredient(IIngredientTypeWithSubtypes<B, FluidStack> ingredientType) {
		if (ingredientType != getType())
			return ITypedIngredient.super.getBaseIngredient(ingredientType);
		
		//noinspection unchecked
		return (B) fluid;
	}
	//?}
	
}
