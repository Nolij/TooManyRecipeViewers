package dev.nolij.toomanyrecipeviewers.impl.recipe;

import com.mojang.blaze3d.platform.InputConstants;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.nolij.toomanyrecipeviewers.impl.jei.api.gui.builder.RecipeLayoutBuilder;
import dev.nolij.toomanyrecipeviewers.impl.jei.api.runtime.IngredientManager;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.library.focus.FocusGroup;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TMRVRecipe<T> implements EmiRecipe, IDebuggableRecipe {
	
	private final IngredientManager ingredientManager;
	
	private final EmiRecipeCategory emiCategory;
	private final IRecipeCategory<T> jeiCategory;
	
	private final T jeiRecipe;
	
	public ResourceLocation id;
	
	private final List<EmiIngredient> inputs;
	private final List<EmiIngredient> catalysts;
	private final List<EmiStack> outputs;
	private final boolean supportsRecipeTree;
	
	public TMRVRecipe(IngredientManager ingredientManager, EmiRecipeCategory emiCategory, IRecipeCategory<T> jeiCategory, T jeiRecipe, ResourceLocation id) {
		this.ingredientManager = ingredientManager;
		this.emiCategory = emiCategory;
		this.jeiCategory = jeiCategory;
		this.jeiRecipe = jeiRecipe;
		
		this.id = id;
		
		final var builder = new RecipeLayoutBuilder(ingredientManager);
		jeiCategory.setRecipe(builder, jeiRecipe, FocusGroup.EMPTY);
		
		final var recipeData = builder.extractEMIRecipeData();
		inputs = recipeData.inputs();
		catalysts = recipeData.catalysts();
		outputs = recipeData.outputs();
		supportsRecipeTree = recipeData.supportsRecipeTree();
	}
	
	@Override
	public EmiRecipeCategory getCategory() {
		return emiCategory;
	}
	
	@Override
	public @Nullable ResourceLocation getId() {
		return id;
	}
	
	@Override
	public List<EmiIngredient> getInputs() {
		return inputs;
	}
	
	@Override
	public List<EmiIngredient> getCatalysts() {
		return catalysts;
	}
	
	@Override
	public List<EmiStack> getOutputs() {
		return outputs;
	}
	
	@Override
	public int getDisplayWidth() {
		return jeiCategory.getWidth();
	}
	
	@Override
	public int getDisplayHeight() {
		return jeiCategory.getHeight();
	}
	
	@Override
	public boolean supportsRecipeTree() {
		return supportsRecipeTree;
	}
	
	@Override
	public void addWidgets(WidgetHolder widgets) {
		final var builder = new RecipeLayoutBuilder(ingredientManager);
		jeiCategory.setRecipe(builder, jeiRecipe, FocusGroup.EMPTY);
		
		final var rootWidget = widgets.add(new TMRVRecipeWidget(widgets, getDisplayWidth(), getDisplayHeight()) {
			@SuppressWarnings("removal")
			@Override
			protected void renderSetup(EmiDrawContext context, int mouseX, int mouseY, float delta) {
				context.push();
				context.matrices().translate(0F, 0F, 0F);
				
				context.resetColor();
				context.pop();
				
				final var categoryBackground = jeiCategory.getBackground();
				if (categoryBackground != null) {
					categoryBackground.draw(context.raw());
				}
				
				jeiCategory.draw(jeiRecipe, slotsView, context.raw(), mouseX, mouseY);
			}
			
			@Override
			protected void buildTooltip(ITooltipBuilder builder, int mouseX, int mouseY) {
				jeiCategory.getTooltip(builder, jeiRecipe, slotsView, mouseX, mouseY);
			}
			
			@SuppressWarnings("removal")
			@Override
			protected boolean handleInput(int mouseX, int mouseY, InputConstants.Key key) {
				return jeiCategory.handleInput(jeiRecipe, mouseX, mouseY, key);
			}
		});
		
		rootWidget.addSlotWidgets(builder, this);
		jeiCategory.createRecipeExtras(rootWidget, jeiRecipe, FocusGroup.EMPTY);
	}
	
	@Override
	public DebugInfo getDebugInfo() {
		return new DebugInfo("TMRVRecipe", id, 0x7700FF00);
	}
	
}
