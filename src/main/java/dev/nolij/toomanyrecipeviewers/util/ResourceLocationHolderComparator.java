package dev.nolij.toomanyrecipeviewers.util;

import net.minecraft.resources.ResourceLocation;

import java.util.Comparator;
import java.util.function.Function;

public abstract class ResourceLocationHolderComparator<T> implements Comparator<T> {
	
	public static <T> ResourceLocationHolderComparator<T> create(Function<T, ResourceLocation> getter) {
		return new ResourceLocationHolderComparator<>() {
			@Override
			public ResourceLocation getResourceLocation(T obj) {
				return getter.apply(obj);
			}
		};
	}
	
	public abstract ResourceLocation getResourceLocation(T t);
	
	private static boolean isVanillaNamespace(ResourceLocation location) {
		return location.getNamespace().equals("minecraft");
	}
	
	@Override
	public int compare(T _left, T _right) {
		final ResourceLocation left = getResourceLocation(_left);
		final ResourceLocation right = getResourceLocation(_right);
		final boolean leftIsVanilla = isVanillaNamespace(left);
		final boolean rightIsVanilla = isVanillaNamespace(right);
		if (leftIsVanilla ^ rightIsVanilla) {
			if (leftIsVanilla)
				return -1;
			
			return 1;
		}
		
		return left.compareNamespaced(right);
	}
}
