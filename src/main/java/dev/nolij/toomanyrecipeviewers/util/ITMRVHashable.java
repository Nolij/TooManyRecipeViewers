package dev.nolij.toomanyrecipeviewers.util;

import java.util.Arrays;
import java.util.Collection;

public interface ITMRVHashable {
	
	int tmrv$hash();
	
	static int hash(Object value) {
		return switch (value) {
			case null -> 0;
			case ITMRVHashable hashable -> hashable.tmrv$hash();
			case Object[] array -> hash(array);
			case Collection<?> collection -> hash(collection);
			default -> value.hashCode();
		};
	}
	
	static int hash(Collection<?> values) {
		return Arrays.hashCode(values.stream().mapToInt(ITMRVHashable::hash).toArray());
	}
	
	static int hash(Object... values) {
		return Arrays.hashCode(Arrays.stream(values).mapToInt(ITMRVHashable::hash).toArray());
	}
	
}
