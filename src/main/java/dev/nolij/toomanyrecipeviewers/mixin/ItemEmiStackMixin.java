package dev.nolij.toomanyrecipeviewers.mixin;

//? if >=21.1
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import dev.emi.emi.api.stack.ItemEmiStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@SuppressWarnings({"UnstableApiUsage", "NonExtendableApiUsage"})
@Mixin(value = ItemEmiStack.class, remap = false)
public abstract class ItemEmiStackMixin implements ITypedIngredient<ItemStack> {
	
	@Shadow @Final private Item item;
	
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
	
}
