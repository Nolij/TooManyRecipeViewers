package dev.nolij.toomanyrecipeviewers.impl.api.runtime;

import dev.emi.emi.config.EmiConfig;
import mezz.jei.api.runtime.IJeiKeyMapping;
import mezz.jei.api.runtime.IJeiKeyMappings;
import org.jetbrains.annotations.NotNull;

public class JEIKeyMappings implements IJeiKeyMappings {
	
	@Override
	public @NotNull IJeiKeyMapping getShowRecipe() {
		return new JEIKeyMapping(() -> EmiConfig.viewRecipes);
	}
	
	@Override
	public @NotNull IJeiKeyMapping getShowUses() {
		return new JEIKeyMapping(() -> EmiConfig.viewUses);
	}
	
}
