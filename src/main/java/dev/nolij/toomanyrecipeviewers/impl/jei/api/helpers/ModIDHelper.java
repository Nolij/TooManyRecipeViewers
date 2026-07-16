package dev.nolij.toomanyrecipeviewers.impl.jei.api.helpers;

import dev.nolij.toomanyrecipeviewers.TooManyRecipeViewers;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.common.platform.Services;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ModIDHelper implements IModIdHelper {
	
	private final TooManyRecipeViewers runtime;
	
	public ModIDHelper(TooManyRecipeViewers runtime) {
		this.runtime = runtime;
	}
	
	@Override
	public String getModNameForModId(String id) {
		return Services.PLATFORM.getModHelper().getModNameForModId(id);
	}
	
	@Override
	public boolean isDisplayingModNameEnabled() {
		return false;
	}
	
	//? if >=21.1
	@SuppressWarnings("removal")
	@Override
	public String getFormattedModNameForModId(String id) {
		return getModNameForModId(id);
	}
	
	//? if >=21.1 {
	@Override
	public Set<String> getModAliases(String id) {
		return runtime.modAliases.get(id);
	}
	//?}
	
	@SuppressWarnings("removal")
	@Override
	public <T> List<Component> addModNameToIngredientTooltip(List<Component> tooltip, T ingredient, IIngredientHelper<T> helper) {
		return tooltip;
	}
	
	@SuppressWarnings("removal")
	@Override
	public <T> List<Component> addModNameToIngredientTooltip(List<Component> tooltip, ITypedIngredient<T> typedIngredient) {
		return tooltip;
	}
	
	@Override
	public <T> Optional<Component> getModNameForTooltip(ITypedIngredient<T> typedIngredient) {
		return Optional.empty();
	}
	
}
