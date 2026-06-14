package dev.nolij.toomanyrecipeviewers.impl.recipe;

import com.mojang.blaze3d.platform.InputConstants;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.nolij.toomanyrecipeviewers.TooManyRecipeViewers;
import dev.nolij.toomanyrecipeviewers.impl.jei.api.gui.builder.RecipeLayoutBuilder;
import dev.nolij.toomanyrecipeviewers.impl.jei.api.recipe.RecipeManager;
import dev.nolij.toomanyrecipeviewers.impl.jei.api.runtime.IngredientManager;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.library.focus.FocusGroup;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class TMRVRecipe<T> implements EmiRecipe, IDebuggableRecipe {
	
	private final IngredientManager ingredientManager;
	
	private final RecipeManager.Category<T> category;
	
	private final T jeiRecipe;
	
	public ResourceLocation id;
	
	private final List<EmiIngredient> inputs;
	private final List<EmiIngredient> catalysts;
	private final List<EmiStack> outputs;
	private final boolean supportsRecipeTree;
	
	public TMRVRecipe(TooManyRecipeViewers runtime, RecipeManager.Category<T> category, T jeiRecipe, ResourceLocation id) {
		Objects.requireNonNull(category.getJEICategory());
		this.ingredientManager = runtime.ingredientManager;
		this.category = category;
		this.jeiRecipe = jeiRecipe;
		
		this.id = id;
		
		final var recipeData = buildRecipe().extractEMIRecipeData();
		inputs = recipeData.inputs();
		catalysts = recipeData.catalysts();
		outputs = recipeData.outputs();
		supportsRecipeTree = recipeData.supportsRecipeTree();
	}
	
	private @NotNull RecipeLayoutBuilder buildRecipe() {
		final var builder = new RecipeLayoutBuilder(ingredientManager);
		//noinspection DataFlowIssue
		category.getJEICategory().setRecipe(builder, jeiRecipe, FocusGroup.EMPTY);
		
		return builder;
	}
	
	@Override
	public EmiRecipeCategory getCategory() {
		return category.getEMICategory();
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
		//noinspection DataFlowIssue
		return category.getJEICategory().getWidth();
	}
	
	@Override
	public int getDisplayHeight() {
		//noinspection DataFlowIssue
		return category.getJEICategory().getHeight();
	}
	
	@Override
	public boolean supportsRecipeTree() {
		return supportsRecipeTree;
	}
	
	@Override
	public void addWidgets(WidgetHolder widgets) {
		final var builder = buildRecipe();
		
		final var rootWidget = widgets.add(new TMRVRecipeWidget(widgets, getDisplayWidth(), getDisplayHeight()) {
			@SuppressWarnings("removal")
			@Override
			protected void renderSetup(EmiDrawContext context, int mouseX, int mouseY, float delta) {
				context.push();
				context.matrices().translate(0F, 0F, 0F);
				
				context.resetColor();
				context.pop();
				
				@SuppressWarnings("DataFlowIssue")
				final var categoryBackground = category.getJEICategory().getBackground();
				if (categoryBackground != null) {
					categoryBackground.draw(context.raw());
				}
				
				category.getJEICategory().draw(jeiRecipe, slotsView, context.raw(), mouseX, mouseY);
			}
			
			@Override
			protected void buildTooltip(ITooltipBuilder builder, int mouseX, int mouseY) {
				//noinspection DataFlowIssue
				category.getJEICategory().getTooltip(builder, jeiRecipe, slotsView, mouseX, mouseY);
			}
			
			@SuppressWarnings("removal")
			@Override
			protected boolean handleInput(int mouseX, int mouseY, InputConstants.Key key) {
				//noinspection DataFlowIssue
				return category.getJEICategory().handleInput(jeiRecipe, mouseX, mouseY, key);
			}
		});
		
		rootWidget.addSlotWidgets(builder, this);
		//noinspection DataFlowIssue
		category.getJEICategory().createRecipeExtras(rootWidget, jeiRecipe, FocusGroup.EMPTY);
	}
	
	@Override
	public DebugInfo getDebugInfo() {
		return new DebugInfo("TMRVRecipe", id, 0x7700FF00);
	}
	
}
