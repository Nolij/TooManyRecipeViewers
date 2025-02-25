package dev.nolij.toomanyrecipeviewers.impl.api.gui.builder;

import dev.nolij.toomanyrecipeviewers.util.ITMRVHashable;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;

import java.util.*;

public class RecipeLayoutBuilder implements IRecipeLayoutBuilder, ITMRVHashable {
	
	private final IIngredientManager ingredientManager;
	
	public boolean shapeless = false;
	
	public final List<ITypedIngredient<?>> inputs = new ArrayList<>();
	public final List<ITypedIngredient<?>> catalysts = new ArrayList<>();
	public final List<ITypedIngredient<?>> outputs = new ArrayList<>();
	
	public RecipeLayoutBuilder(IIngredientManager ingredientManager) {
		this.ingredientManager = ingredientManager;
	}
	
	@Override
	public IRecipeSlotBuilder addSlot(RecipeIngredientRole role) {
		return new RecipeSlotBuilder(this, ingredientManager, role);
	}
	
	@SuppressWarnings("removal")
	@Override
	public IRecipeSlotBuilder addSlotToWidget(RecipeIngredientRole role, mezz.jei.api.gui.widgets.ISlottedWidgetFactory<?> widgetFactory) {
		return new RecipeSlotBuilder(this, ingredientManager, role);
	}
	
	@Override
	public IIngredientAcceptor<?> addInvisibleIngredients(RecipeIngredientRole role) {
		return new RecipeSlotBuilder(this, ingredientManager, role);
	}
	
	@Override
	public void moveRecipeTransferButton(int x, int y) {
		
	}
	
	@Override
	public void setShapeless() {
		shapeless = true;
	}
	
	@Override
	public void setShapeless(int x, int y) {
		shapeless = true;
	}
	
	@Override
	public void createFocusLink(IIngredientAcceptor<?>... ingredientAcceptors) {
		
	}
	
	@Override
	public int tmrv$hash() {
		return ITMRVHashable.hash(
			inputs,
			catalysts,
			outputs,
			shapeless
		);
	}
	
}
