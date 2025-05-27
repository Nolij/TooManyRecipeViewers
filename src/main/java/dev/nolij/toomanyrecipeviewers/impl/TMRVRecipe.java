package dev.nolij.toomanyrecipeviewers.impl;

import com.mojang.blaze3d.platform.InputConstants;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.jemi.impl.JemiTooltipBuilder;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.EmiScreenManager;
import dev.nolij.toomanyrecipeviewers.TooManyRecipeViewers;
import dev.nolij.toomanyrecipeviewers.impl.jei.api.gui.builder.RecipeLayoutBuilder;
import dev.nolij.toomanyrecipeviewers.impl.jei.api.recipe.RecipeManager;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.library.focus.FocusGroup;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TMRVRecipe<T> implements EmiRecipe {
	
	private final TooManyRecipeViewers runtime;
	
	private final RecipeManager.Category<T> category;
	private final IRecipeCategory<T> jeiCategory;
	
	private final T jeiRecipe;
	
	public ResourceLocation originalId;
	public ResourceLocation id;
	
	private final List<EmiIngredient> inputs;
	private final List<EmiIngredient> catalysts;
	private final List<EmiStack> outputs;
	
	public TMRVRecipe(TooManyRecipeViewers runtime, RecipeManager.Category<T> category, T jeiRecipe, ResourceLocation id) {
		this.runtime = runtime;
		this.category = category;
		this.jeiCategory = Objects.requireNonNull(category.getJEICategory());
		this.jeiRecipe = jeiRecipe;
		
		this.originalId = jeiCategory.getRegistryName(jeiRecipe);
		this.id = id;
		
		final var builder = new RecipeLayoutBuilder(runtime.ingredientManager);
		jeiCategory.setRecipe(builder, jeiRecipe, FocusGroup.EMPTY);
		
		final var recipeData = builder.extractEMIRecipeData();
		inputs = recipeData.inputs();
		catalysts = recipeData.catalysts();
		outputs = recipeData.outputs();
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
		return jeiCategory.getWidth();
	}
	
	@Override
	public int getDisplayHeight() {
		return jeiCategory.getHeight();
	}
	
	private static class RecipeSlotsView implements IRecipeSlotsView {
		
		private final List<IRecipeSlotView> slots = new ArrayList<>();
		
		@Override
		public @Unmodifiable List<IRecipeSlotView> getSlotViews() {
			return slots;
		}
		
	}
	
	@Override
	public void addWidgets(WidgetHolder widgets) {
		final var builder = new RecipeLayoutBuilder(runtime.ingredientManager);
		jeiCategory.setRecipe(builder, jeiRecipe, FocusGroup.EMPTY);
		
		final var recipeSlotsView = new RecipeSlotsView();
		widgets.add(new Widget(recipeSlotsView));
		
		for (final var slot : builder.getSlots()) {
			if (!slot.isVisible())
				continue;
			
			final var widget = widgets.add(slot.build());
			recipeSlotsView.slots.add((IRecipeSlotView) widget);
			
			if (slot.role == RecipeIngredientRole.OUTPUT)
				widget.recipeContext(this);
		}
	}
	
	private class Widget extends dev.emi.emi.api.widget.Widget {
		
		private final IRecipeSlotsView recipeSlotsView;
		private final Bounds bounds;
		
		private Widget(IRecipeSlotsView recipeSlotsView) {
			this.recipeSlotsView = recipeSlotsView;
			this.bounds = new Bounds(0, 0, getDisplayWidth(), getDisplayHeight());
		}
		
		@Override
		public Bounds getBounds() {
			return bounds;
		}
		
		@Override
		public void render(GuiGraphics draw, int mouseX, int mouseY, float delta) {
			final var context = EmiDrawContext.wrap(draw);
			context.push();
			context.matrices().translate(0F, 0F, 0F);
			//noinspection removal
			final var categoryBackground = jeiCategory.getBackground();
			if (categoryBackground != null) {
				categoryBackground.draw(context.raw());
			}
			
			jeiCategory.draw(jeiRecipe, recipeSlotsView, context.raw(), mouseX, mouseY);
			context.resetColor();
			context.pop();
		}
		
		@Override
		public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
			final var tooltipBuilder = new JemiTooltipBuilder();
			jeiCategory.getTooltip(tooltipBuilder, jeiRecipe, recipeSlotsView, mouseX, mouseY);
			return tooltipBuilder.tooltip;
		}
		
		@Override
		public boolean mouseClicked(int mouseX, int mouseY, int button) {
			//noinspection removal
			return jeiCategory.handleInput(jeiRecipe, mouseX, mouseY, InputConstants.Type.MOUSE.getOrCreate(button));
		}
		
		@Override
		public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
			//noinspection removal
			return jeiCategory.handleInput(jeiRecipe, EmiScreenManager.lastMouseX, EmiScreenManager.lastMouseY, InputConstants.getKey(keyCode, scanCode));
		}
		
	}
	
}
