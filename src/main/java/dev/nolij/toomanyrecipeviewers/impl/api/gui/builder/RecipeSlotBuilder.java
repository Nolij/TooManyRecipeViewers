package dev.nolij.toomanyrecipeviewers.impl.api.gui.builder;

import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.library.ingredients.TypedIngredient;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static dev.nolij.toomanyrecipeviewers.TooManyRecipeViewers.fluidHelper;

@SuppressWarnings("NonExtendableApiUsage")
public class RecipeSlotBuilder implements IRecipeSlotBuilder {
	
	private final IIngredientManager ingredientManager;
	
	private final List<ITypedIngredient<?>> collectedIngredients;
	
	RecipeSlotBuilder(RecipeLayoutBuilder recipeLayoutBuilder, IIngredientManager ingredientManager, RecipeIngredientRole role) {
		this.ingredientManager = ingredientManager;
		switch (role) {
			case INPUT -> this.collectedIngredients = recipeLayoutBuilder.inputs;
			case CATALYST -> this.collectedIngredients = recipeLayoutBuilder.catalysts;
			case OUTPUT -> this.collectedIngredients = recipeLayoutBuilder.outputs;
			default -> this.collectedIngredients = new ArrayList<>();
		}
	}
	
	@SuppressWarnings("removal")
	@Override
	public IRecipeSlotBuilder addTooltipCallback(mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback tooltipCallback) {
		return this;
	}
	
	@Override
	public IRecipeSlotBuilder addRichTooltipCallback(IRecipeSlotRichTooltipCallback iRecipeSlotRichTooltipCallback) {
		return this;
	}
	
	@Override
	public IRecipeSlotBuilder setSlotName(String s) {
		return this;
	}
	
	@Override
	public IRecipeSlotBuilder setStandardSlotBackground() {
		return this;
	}
	
	@Override
	public IRecipeSlotBuilder setOutputSlotBackground() {
		return this;
	}
	
	@Override
	public IRecipeSlotBuilder setBackground(IDrawable drawable, int x, int y) {
		return this;
	}
	
	@Override
	public IRecipeSlotBuilder setOverlay(IDrawable drawable, int x, int y) {
		return this;
	}
	
	@Override
	public IRecipeSlotBuilder setFluidRenderer(long capacity, boolean showCapacity, int width, int height) {
		return this;
	}
	
	@Override
	public <T> IRecipeSlotBuilder setCustomRenderer(IIngredientType<T> type, IIngredientRenderer<T> renderer) {
		return this;
	}
	
	@Override
	public <I> IRecipeSlotBuilder addIngredients(IIngredientType<I> type, List<@Nullable I> ingredients) {
		collectedIngredients.addAll(TypedIngredient.createAndFilterInvalidList(ingredientManager, type, ingredients, false));
		return this;
	}
	
	@Override
	public <I> IRecipeSlotBuilder addIngredient(IIngredientType<I> type, I ingredient) {
		collectedIngredients.add(TypedIngredient.createAndFilterInvalid(ingredientManager, type, ingredient, false));
		return this;
	}
	
	@Override
	public IRecipeSlotBuilder addIngredientsUnsafe(List<?> ingredients) {
		collectedIngredients.addAll(ingredients.stream()
			.map(x -> TypedIngredient.createAndFilterInvalid(ingredientManager, x, false))
			.toList());
		return this;
	}
	
	@Override
	public IRecipeSlotBuilder addTypedIngredients(List<ITypedIngredient<?>> ingredients) {
		collectedIngredients.addAll(ingredients.stream()
			.map(x -> TypedIngredient.defensivelyCopyTypedIngredientFromApi(ingredientManager, x))
			.toList());
		return this;
	}
	
	@Override
	public IRecipeSlotBuilder addOptionalTypedIngredients(List<Optional<ITypedIngredient<?>>> ingredients) {
		ingredients.stream()
			.map(x -> x.orElse(null))
			.forEach(collectedIngredients::add);
		return this;
	}
	
	@Override
	public IRecipeSlotBuilder addFluidStack(Fluid fluid) {
		return this.addFluidStack(fluid, fluidHelper.bucketVolume(), DataComponentPatch.EMPTY);
	}
	
	@Override
	public IRecipeSlotBuilder addFluidStack(Fluid fluid, long amount) {
		return this.addFluidStack(fluid, amount, DataComponentPatch.EMPTY);
	}
	
	@Override
	public IRecipeSlotBuilder addFluidStack(Fluid fluid, long amount, DataComponentPatch componentPatch) {
		//noinspection deprecation,unchecked
		return this.addIngredient((IIngredientType<Object>) fluidHelper.getFluidIngredientType(), fluidHelper.create(fluid.builtInRegistryHolder(), amount, componentPatch));
	}
	
	@Override
	public IRecipeSlotBuilder setPosition(int x, int y) {
		return this;
	}
	
	@Override
	public int getWidth() {
		return 0;
	}
	
	@Override
	public int getHeight() {
		return 0;
	}
	
}
