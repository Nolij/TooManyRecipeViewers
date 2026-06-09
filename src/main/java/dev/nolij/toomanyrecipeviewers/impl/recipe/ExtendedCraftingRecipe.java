package dev.nolij.toomanyrecipeviewers.impl.recipe;

import com.mojang.blaze3d.platform.InputConstants;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.nolij.toomanyrecipeviewers.TooManyRecipeViewers;
import dev.nolij.toomanyrecipeviewers.impl.jei.api.gui.builder.RecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import mezz.jei.library.focus.FocusGroup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingRecipe;

import java.util.List;

public class ExtendedCraftingRecipe<R extends CraftingRecipe> extends ExtendedRecipe<R> implements IDebuggableRecipe {
	
	private final TooManyRecipeViewers runtime;
	//? if >=21.1 {
	private final ICraftingCategoryExtension<R> extension;
	//?} else
	//private final ICraftingCategoryExtension extension;
	private final RecipeLayoutBuilder.ExtractedEMIRecipeData recipeData;
	
	private final int width, height;
	
	public ExtendedCraftingRecipe(TooManyRecipeViewers runtime, R backingRecipe,
								  //? if >=21.1 {
								  ICraftingCategoryExtension<R> extension,
								  //?} else
								  //ICraftingCategoryExtension extension, 
								  ResourceLocation id) {
		super(VanillaEmiRecipeCategories.CRAFTING, backingRecipe, id);
		this.runtime = runtime;
		this.extension = extension;
		
		this.width = Math.max(118, extension.getWidth(
			//? if >=21.1
			this.backingRecipe
		));
		this.height = Math.max(54, extension.getHeight(
			//? if >=21.1
			this.backingRecipe
		));
		
		final var builder = new RecipeLayoutBuilder(runtime.ingredientManager);
		extension.setRecipe(
			//? if >=21.1
			this.backingRecipe, 
			builder, runtime.guiHelper.createCraftingGridHelper(), FocusGroup.EMPTY);
		recipeData = builder.extractEMIRecipeData();
	}
	
	@Override
	public List<EmiIngredient> getInputs() {
		return recipeData.inputs();
	}
	
	@Override
	public List<EmiStack> getOutputs() {
		return recipeData.outputs();
	}
	
	@Override
	public int getDisplayWidth() {
		return width;
	}
	
	@Override
	public int getDisplayHeight() {
		return height;
	}
	
	@Override
	public void addWidgets(WidgetHolder widgets) {
		final var builder = new RecipeLayoutBuilder(runtime.ingredientManager);
		extension.setRecipe(
			//? if >=21.1
			backingRecipe, 
			builder, runtime.guiHelper.createCraftingGridHelper(), FocusGroup.EMPTY);

		final var rootWidget = widgets.add(new TMRVRecipeWidget(widgets, width, height) {
			@Override
			protected void renderSetup(EmiDrawContext context, int mouseX, int mouseY, float delta) {
				context.push();
				context.matrices().translate(0F, 0F, 0F);
				
				extension.drawInfo(
					//? if >=21.1
					backingRecipe, 
					width, height, context.raw(), mouseX, mouseY);
				
				context.pop();
			}
			
			@Override
			protected void buildTooltip(ITooltipBuilder builder, int mouseX, int mouseY) {
				extension.getTooltip(builder,
					//? if >=21.1
					backingRecipe, 
					mouseX, mouseY);
			}
			
			@SuppressWarnings("removal")
			@Override
			protected boolean handleInput(int mouseX, int mouseY, InputConstants.Key key) {
				return extension.handleInput(
					//? if >=21.1
					backingRecipe, 
					mouseX, mouseY, key);
			}
		});
		
		widgets.addTexture(EmiTexture.EMPTY_ARROW, 60, 18);
		
		rootWidget.addSlotWidgets(builder, this);
		extension.createRecipeExtras(
			//? if >=21.1
			backingRecipe, 
			rootWidget, runtime.guiHelper.createCraftingGridHelper(), FocusGroup.EMPTY);
		extension.onDisplayedIngredientsUpdate(
			//? if >=21.1
			backingRecipe, 
			rootWidget.getRecipeSlots().getSlots(), FocusGroup.EMPTY);
	}
	
	@Override
	public boolean supportsRecipeTree() {
		return recipeData.supportsRecipeTree();
	}
	
	@Override
	public DebugInfo getDebugInfo() {
		return new DebugInfo("ExtendedCraftingRecipe", id, 0x7700FF00, 0x77FF00FF);
	}
	
}