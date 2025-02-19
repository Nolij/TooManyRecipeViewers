package dev.nolij.toomanyrecipeviewers.mixin;

import dev.nolij.toomanyrecipeviewers.util.IJEITypedItemStack;
import mezz.jei.library.ingredients.itemStacks.TypedItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;

@Mixin(value = TypedItemStack.class, remap = false)
public abstract class TypedItemStackMixin implements IJEITypedItemStack {
	
	@Shadow protected abstract Item getItem();
	
	@Override
	public Item tmrv$getItem() {
		return null;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(BuiltInRegistries.ITEM.getKey(tmrv$getItem()), tmrv$getDataComponentPatch(), tmrv$getCount());
	}
	
}
