package mezz.jei.library.ingredients.itemStacks;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;

public final class TypedItemStackExtension {
	
	public static TypedItemStack create(Item item) {
		return new NormalizedTypedItem(Holder.direct(item));
	}
	
	public static TypedItemStack create(Item item, DataComponentPatch dataComponentPatch) {
		if (dataComponentPatch.isEmpty())
			return create(item);
		
		return new NormalizedTypedItemStack(Holder.direct(item), dataComponentPatch);
	}
	
	public static TypedItemStack create(Item item, int count) {
		return create(item, count, DataComponentPatch.EMPTY);
	}
	
	public static TypedItemStack create(Item item, int count, DataComponentPatch dataComponentPatch) {
		if (count == 1)
			return create(item, dataComponentPatch);
		
		return new FullTypedItemStack(Holder.direct(item), dataComponentPatch, count);
	}
	
}
