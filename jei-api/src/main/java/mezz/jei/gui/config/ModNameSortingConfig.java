package mezz.jei.gui.config;

import mezz.jei.gui.ingredients.IListElementInfo;

import java.util.Collection;
import java.util.Comparator;

public class ModNameSortingConfig {
	
	public Comparator<IListElementInfo<?>> getComparatorFromMappedValues(Collection<String> allMappedValues) {
		return Comparator.comparing(IListElementInfo::getModNameForSorting);
	}
	
}
