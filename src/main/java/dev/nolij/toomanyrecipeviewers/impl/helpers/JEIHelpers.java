package dev.nolij.toomanyrecipeviewers.impl.helpers;

import mezz.jei.api.helpers.*;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IIngredientVisibility;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.stream.Stream;

public class JEIHelpers implements IJeiHelpers {
	
	@Override
	public @NotNull IGuiHelper getGuiHelper() {
		return null;
	}
	
	@Override
	public @NotNull IStackHelper getStackHelper() {
		return null;
	}
	
	@Override
	public @NotNull IModIdHelper getModIdHelper() {
		return null;
	}
	
	@Override
	public @NotNull IFocusFactory getFocusFactory() {
		return null;
	}
	
	@Override
	public @NotNull IColorHelper getColorHelper() {
		return null;
	}
	
	@Override
	public @NotNull IPlatformFluidHelper<?> getPlatformFluidHelper() {
		return null;
	}
	
	@Override
	public <T> @NotNull Optional<RecipeType<T>> getRecipeType(@NotNull ResourceLocation resourceLocation, @NotNull Class<? extends T> aClass) {
		return Optional.empty();
	}
	
	@Override
	public @NotNull Optional<RecipeType<?>> getRecipeType(@NotNull ResourceLocation resourceLocation) {
		return Optional.empty();
	}
	
	@Override
	public @NotNull Stream<RecipeType<?>> getAllRecipeTypes() {
		return Stream.empty();
	}
	
	@Override
	public @NotNull IIngredientManager getIngredientManager() {
		return null;
	}
	
	@Override
	public @NotNull ICodecHelper getCodecHelper() {
		return null;
	}
	
	@Override
	public @NotNull IVanillaRecipeFactory getVanillaRecipeFactory() {
		return null;
	}
	
	@Override
	public @NotNull IIngredientVisibility getIngredientVisibility() {
		return null;
	}
	
}
