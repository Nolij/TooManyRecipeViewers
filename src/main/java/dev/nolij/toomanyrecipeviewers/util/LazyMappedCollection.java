package dev.nolij.toomanyrecipeviewers.util;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;

public class LazyMappedCollection<T, E> extends AbstractCollection<T> {
	
	private final Collection<E> list;
	private final Function<E, T> mapper;
	
	public LazyMappedCollection(Collection<E> list, Function<E, T> mapper) {
		this.list = list;
		this.mapper = mapper;
	}
	
	@Override
	public @NotNull Iterator<T> iterator() {
		return list.stream().map(mapper).iterator();
	}
	
	@Override
	public int size() {
		return list.size();
	}
	
}
