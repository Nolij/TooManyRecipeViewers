package dev.nolij.toomanyrecipeviewers.impl.jei.api.gui.builder;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.nolij.toomanyrecipeviewers.impl.jei.api.runtime.IngredientManager;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.recipe.RecipeIngredientRole;

import java.util.ArrayList;
import java.util.List;

public class RecipeLayoutBuilder implements IRecipeLayoutBuilder {
	
	public record ExtractedEMIRecipeData(
		List<EmiIngredient> inputs, 
		List<EmiIngredient> catalysts, 
		List<EmiStack> outputs, 
		boolean shapeless
	) {}
	
	private final IngredientManager ingredientManager;
	
	private final List<RecipeSlotBuilder> slots = new ArrayList<>();
	private boolean shapeless = false;
	
	public RecipeLayoutBuilder(IngredientManager ingredientManager) {
		this.ingredientManager = ingredientManager;
	}
	
	public ExtractedEMIRecipeData extractEMIRecipeData() {
		return new ExtractedEMIRecipeData(
			slots.stream()
				.filter(x -> x.role == RecipeIngredientRole.INPUT)
				.map(RecipeSlotBuilder::getEMIIngredient)
				.toList(),
			slots.stream()
				.filter(x -> x.role == RecipeIngredientRole.CATALYST)
				.map(RecipeSlotBuilder::getEMIIngredient)
				.toList(),
			slots.stream()
				.filter(x -> x.role == RecipeIngredientRole.OUTPUT)
				.flatMap(x -> x.getEMIStacks().stream())
				.toList(),
			shapeless
		);
	}
	
	public List<RecipeSlotBuilder> getSlots() {
		return slots;
	}
	
	@Override
	public IRecipeSlotBuilder addSlot(RecipeIngredientRole role) {
		final var slot = new RecipeSlotBuilder(ingredientManager, role);
		slots.add(slot);
		return slot;
	}
	
	@SuppressWarnings("removal")
	@Override
	public IRecipeSlotBuilder addSlotToWidget(RecipeIngredientRole role, mezz.jei.api.gui.widgets.ISlottedWidgetFactory<?> widgetFactory) {
		return addSlot(role);
	}
	
	@Override
	public IIngredientAcceptor<?> addInvisibleIngredients(RecipeIngredientRole role) {
		return ((RecipeSlotBuilder) addSlot(role)).setInvisible();
	}
	
	@Override
	public void moveRecipeTransferButton(int x, int y) {
		// TODO
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
	
}
