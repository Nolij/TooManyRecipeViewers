package dev.nolij.toomanyrecipeviewers.mixin;

//? if >=21.1
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ItemEmiStack;
import dev.nolij.toomanyrecipeviewers.util.IItemStackish;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@SuppressWarnings({"UnstableApiUsage", "NonExtendableApiUsage"})
@Mixin(value = ItemEmiStack.class, remap = false)
public abstract class ItemEmiStackMixin implements ITypedIngredient<ItemStack>, IItemStackish<EmiStack> {
	
	@Shadow @Final private Item item;
	
	//? if >=21.1 {
	@Shadow @Final private DataComponentPatch componentChanges;
	//?} else
	/*@Shadow @Final private CompoundTag nbt;*/
	
	@Shadow public abstract EmiStack copy();
	
	@Override
	public IIngredientType<ItemStack> getType() {
		return VanillaTypes.ITEM_STACK;
	}
	
	@Override
	public ItemStack getIngredient() {
		return ((ItemEmiStack) (Object) this).getItemStack();
	}
	
	//? if >=21.1 {
	@Override
	public <B> B getBaseIngredient(IIngredientTypeWithSubtypes<B, ItemStack> ingredientType) {
		if (ingredientType != VanillaTypes.ITEM_STACK)
			return ITypedIngredient.super.getBaseIngredient(ingredientType);
		
		//noinspection unchecked
		return (B) item;
	}
	//?}
	
	@Override
	public Optional<ItemStack> getItemStack() {
		return Optional.of(getIngredient());
	}
	
	@Override
	public Item tmrv$getItem() {
		return item;
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
		return ((ItemEmiStack) (Object) this).getAmount();
	}
	
	@Override
	public EmiStack tmrv$normalize() {
		return copy().setAmount(1);
	}
	
}
