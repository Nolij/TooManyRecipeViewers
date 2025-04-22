package dev.nolij.toomanyrecipeviewers.mixin;

//? if >=21.1 {
import net.minecraft.core.Holder;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
//?}
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.FluidEmiStack;
import dev.nolij.toomanyrecipeviewers.util.IFluidStackish;
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
public abstract class FluidEmiStackMixin implements ITypedIngredient<FluidStack>, IFluidStackish<EmiStack> {
	
	@Shadow @Final private Fluid fluid;
	
	//? if >=21.1 {
	@Shadow @Final private DataComponentPatch componentChanges;
	//?} else
	/*@Shadow @Final private CompoundTag nbt;*/
	
	@Shadow public abstract EmiStack copy();
	
	@Override
	public IIngredientType<FluidStack> getType() {
		//noinspection unchecked
		return (IIngredientType<FluidStack>) fluidHelper.getFluidIngredientType();
	}
	
	@Override
	public FluidStack getIngredient() {
		//? if >=21.1 {
		return (FluidStack) fluidHelper.create(Holder.direct(fluid), tmrv$getAmount(), componentChanges);
		//?} else
		/*return (FluidStack) fluidHelper.create(fluid, tmrv$getAmount(), nbt);*/
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
	
	@Override
	public Fluid tmrv$getFluid() {
		return fluid;
	}
	
	@Override
	public /*? if >=21.1 {*/ DataComponentPatch /*?} else {*/ /*CompoundTag *//*?}*/ tmrv$getDataComponentPatch() {
		//? if >=21.1 {
		return componentChanges;
		//?} else
		/*return nbt;*/
	}
	
	@Override
	public long tmrv$getAmount() {
		final var amount = ((FluidEmiStack) (Object) this).getAmount();
		if (amount == 0L)
			return 1000L;
		
		return amount;
	}
	
	@Override
	public EmiStack tmrv$normalize() {
		return copy().setAmount(0L);
	}
	
}
