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
		final var left = getResourceLocation(_left);
		final var right = getResourceLocation(_right);
		final var leftIsVanilla = isVanillaNamespace(left);
		final var rightIsVanilla = isVanillaNamespace(right);
		if (leftIsVanilla ^ rightIsVanilla) {
			if (leftIsVanilla)
				return -1;
			
			return 1;
		}
		
		return left.compareNamespaced(right);
	}
}
