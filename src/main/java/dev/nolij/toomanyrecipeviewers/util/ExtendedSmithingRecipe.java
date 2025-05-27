package dev.nolij.toomanyrecipeviewers.util;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.nolij.toomanyrecipeviewers.TooManyRecipeViewers;
import dev.nolij.toomanyrecipeviewers.impl.jei.api.gui.builder.TMRVIngredientCollector;
import dev.nolij.toomanyrecipeviewers.impl.jei.api.gui.ingredient.TMRVSlotWidget;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.extensions.vanilla.smithing.ISmithingCategoryExtension;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.library.focus.FocusGroup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.SmithingRecipe;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("NonExtendableApiUsage")
public class ExtendedSmithingRecipe<R extends SmithingRecipe> implements EmiRecipe {
	
	private final TooManyRecipeViewers runtime;
	private final R backingRecipe;
	private final ISmithingCategoryExtension<R> extension;
	private final ResourceLocation id;
	
	public ExtendedSmithingRecipe(TooManyRecipeViewers runtime, R backingRecipe, ISmithingCategoryExtension<R> extension, ResourceLocation id) {
		this.runtime = runtime;
		this.backingRecipe = backingRecipe;
		this.extension = extension;
		this.id = id;
	}
	
	@Override
	public EmiRecipeCategory getCategory() {
		return VanillaEmiRecipeCategories.SMITHING;
	}
	
	@Override
	public @Nullable ResourceLocation getId() {
		return id;
	}
	
	@Override
	public List<EmiIngredient> getInputs() {
		return List.of(
			collect(extension::setTemplate).getEMIIngredient(),
			collect(extension::setBase).getEMIIngredient(),
			collect(extension::setAddition).getEMIIngredient()
		);
	}
	
	@Override
	public List<EmiStack> getOutputs() {
		return collect(extension::setOutput).getEMIStacks();
	}
	
	@Override
	public int getDisplayWidth() {
		return 112;
	}
	
	@Override
	public int getDisplayHeight() {
		return 18;
	}
	
	@FunctionalInterface
	private interface SetMethod<R extends SmithingRecipe> {
		<T extends IIngredientAcceptor<T>> void set(R recipe, T ingredientAcceptor);
	}
	
	private TMRVIngredientCollector collect(SetMethod<R> setMethod) {
		final var collector = new TMRVIngredientCollector(runtime.ingredientManager);
		setMethod.set(backingRecipe, collector);
		
		return collector;
	}
	
	private TMRVSlotWidget addSlot(WidgetHolder widgets, SetMethod<R> setMethod, ImmutableRect2i rect, RecipeIngredientRole role, Consumer<TMRVSlotWidget> updateHook) {
//		var typedIngredients = getTypedIngredients(setMethod).stream().map(runtime.ingredientManager::getEMIStack).toList();
//		
//		if (typedIngredients.isEmpty())
//			return widgets.addSlot(x, y);
//		else if (typedIngredients.size() == 1)
//			return widgets.addSlot(typedIngredients.getFirst(), x, y);
//		else
//			return widgets.addGeneratedSlot(r -> typedIngredients.get(r.nextInt(typedIngredients.size())), unique, x, y);
		
		final var slot = new TMRVSlotWidget(runtime.ingredientManager, role, rect, updateHook);
		setMethod.set(backingRecipe, slot.getIngredientCollector());
		return widgets.add(slot);
	}
	
	@Override
	public void addWidgets(WidgetHolder widgets) {
		widgets.addTexture(EmiTexture.EMPTY_ARROW, 62, 1);
		final var origin = new ImmutableRect2i(1, 1, 16, 16);
		final var templateSlot = addSlot(widgets, extension::setTemplate, origin, RecipeIngredientRole.INPUT, thiz -> {});
		final var baseSlot = addSlot(widgets, extension::setBase, origin.addOffset(18, 0), RecipeIngredientRole.INPUT, thiz -> {});
		final var additionSlot = addSlot(widgets, extension::setAddition, origin.addOffset(36, 0), RecipeIngredientRole.INPUT, thiz -> {});
		addSlot(widgets, extension::setOutput, origin.addOffset(94, 0), RecipeIngredientRole.OUTPUT, thiz -> extension.onDisplayedIngredientsUpdate(backingRecipe, templateSlot, baseSlot, additionSlot, thiz, FocusGroup.EMPTY)).recipeContext(this);
	}
	
}
