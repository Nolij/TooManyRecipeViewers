package dev.nolij.toomanyrecipeviewers.impl.recipe;

//? if >=21.1 {
import net.minecraft.world.item.crafting.RecipeHolder;
//?}
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;

public abstract class ExtendedRecipe<R extends Recipe<?>> implements EmiRecipe {
	
	protected final EmiRecipeCategory category;
	//? if >=21.1 {
	protected final RecipeHolder<R> backingRecipe;
	//?} else
	//protected final R backingRecipe;
	protected final ResourceLocation id;
	
	public ExtendedRecipe(EmiRecipeCategory category, R backingRecipe, ResourceLocation id) {
		this.category = category;
		//? if >=21.1 {
		this.backingRecipe = new RecipeHolder<>(id, backingRecipe);
		//?} else
		//this.backingRecipe = backingRecipe;
		this.id = id;
	}
	
	@Override
	public EmiRecipeCategory getCategory() {
		return category;
	}
	
	@Override
	public @Nullable ResourceLocation getId() {
		return id;
	}
	
	@Override
	//? if >=21.1 {
	public @Nullable RecipeHolder<?> getBackingRecipe() {
	//?} else
	//public @Nullable Recipe<?> getBackingRecipe() {
		return backingRecipe;
	}
	
}
