package dev.nolij.toomanyrecipeviewers.extensions.java.util.stream.Stream;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;

import java.util.Optional;
import java.util.stream.Stream;

@Extension
public final class StreamExtension<R> {
	
	public static <T, R> Stream<Optional<R>> castOptional(@This Stream<T> thiz, Class<R> clazz) {
		//noinspection unchecked
		return thiz.map(x -> clazz.isInstance(x) ? Optional.of((R) x) : Optional.empty());
	}
	
}
