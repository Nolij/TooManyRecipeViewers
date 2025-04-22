package dev.nolij.toomanyrecipeviewers.mixin;

//? if >=21.1 {
import net.minecraft.world.item.Item;
//?}
import dev.nolij.toomanyrecipeviewers.util.IItemStackish;
import mezz.jei.library.ingredients.itemStacks.TypedItemStack;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TypedItemStack.class, remap = false)
public abstract class TypedItemStackMixin implements IItemStackish<TypedItemStack> {
	
	//? if >=21.1 {
	@Shadow protected abstract Item getItem();
	
	@Override
	public Item tmrv$getItem() {
		return getItem();
	}
	//?}
	
	@Shadow protected abstract TypedItemStack getNormalized();
	
	@Inject(method = "normalize", at = @At("HEAD"), cancellable = true)
	private static void tmrv$normalize$HEAD(ITypedIngredient<ItemStack> typedIngredient, CallbackInfoReturnable<ITypedIngredient<ItemStack>> cir) {
		if (typedIngredient instanceof IItemStackish<?> itemStackish) {
			//noinspection unchecked
			cir.setReturnValue((ITypedIngredient<ItemStack>) itemStackish.tmrv$normalize());
		}
	}
	
	@Override
	public TypedItemStack tmrv$normalize() {
		return getNormalized();
	}
	
}
