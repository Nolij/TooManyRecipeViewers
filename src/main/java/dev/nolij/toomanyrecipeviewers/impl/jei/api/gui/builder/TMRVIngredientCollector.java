package dev.nolij.toomanyrecipeviewers.impl.jei.api.gui.builder;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.FluidEmiStack;
import dev.emi.emi.api.stack.ItemEmiStack;
import dev.nolij.toomanyrecipeviewers.impl.jei.api.runtime.IngredientManager;
import dev.nolij.toomanyrecipeviewers.impl.ingredient.ErrorIngredient;
import dev.nolij.toomanyrecipeviewers.util.IStackish;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.IIngredientConsumer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.library.ingredients.TypedIngredient;
import mezz.jei.library.ingredients.itemStacks.TypedItemStack;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@SuppressWarnings({"NonExtendableApiUsage", "unchecked", "UnstableApiUsage"})
public class TMRVIngredientCollector implements IIngredientAcceptor<TMRVIngredientCollector> {
	
	private final IngredientManager ingredientManager;
	private final ArrayList<ITypedIngredient<?>> collectedIngredients = new ArrayList<>();
	
	public TMRVIngredientCollector(IngredientManager ingredientManager) {
		this.ingredientManager = ingredientManager;
	}
	
	public Stream<ITypedIngredient<?>> stream() {
		return collectedIngredients.stream();
	}
	
	public List<ITypedIngredient<?>> getCollectedIngredients() {
		return stream().toList();
	}
	
	public boolean isEmpty() {
		return collectedIngredients.isEmpty();
	}
	
	public List<EmiStack> getEMIStacks() {
		return collectedIngredients.stream().map(ingredientManager::getEMIStack).toList();
	}
	
	public EmiIngredient getEMIIngredient() {
		return EmiIngredient.of(getEMIStacks());
	}
	
	public void copy(TMRVIngredientCollector other) {
		collectedIngredients.addAll(other.collectedIngredients);
	}
	
	//region IIngredientAcceptor
	@Override
	public <I> TMRVIngredientCollector addIngredient(IIngredientType<I> type, I ingredient) {
		if (type == VanillaTypes.ITEM_STACK) {
			if (ingredient instanceof ItemStack itemStack)
				collectedIngredients.add(TypedItemStack.create(itemStack));
			else
				collectedIngredients.add(ErrorIngredient.TYPED_INSTANCE);
		} else {
			final var typedIngredient = TypedIngredient.createAndFilterInvalid(ingredientManager, type, ingredient, false)
				//? if <21.1
				/*.orElse(null)*/
				;
			collectedIngredients.add(typedIngredient != null ? typedIngredient : ErrorIngredient.TYPED_INSTANCE);
		}
		
		return this;
	}
	
	@Override
	public <I> TMRVIngredientCollector addIngredients(IIngredientType<I> type, List<@Nullable I> ingredients) {
		for (final var ingredient : ingredients) {
			addIngredient(type, ingredient);
		}
		
		return this;
	}
	
	@Override
	public TMRVIngredientCollector addIngredientsUnsafe(List<?> ingredients) {
		for (final var ingredient : ingredients) {
			final var type = ingredientManager.getIngredientType(ingredient);
			if (type != null)
				collectedIngredients.add(TypedIngredient.createUnvalidated(type, ingredient));
			else
				collectedIngredients.add(ErrorIngredient.TYPED_INSTANCE);
		}
		
		return this;
	}
	
	@Override
	public <I> TMRVIngredientCollector addTypedIngredient(ITypedIngredient<I> typedIngredient) {
		if (typedIngredient instanceof IStackish<?>) {
			collectedIngredients.add(typedIngredient);
		} else {
			final var copy = TypedIngredient.defensivelyCopyTypedIngredientFromApi(ingredientManager, typedIngredient)
				//? if <21.1
				/*.orElse(null)*/
				;
			collectedIngredients.add(copy != null ? copy : ErrorIngredient.TYPED_INSTANCE);
		}
		
		return this;
	}
	
	@Override
	public TMRVIngredientCollector addTypedIngredients(List<ITypedIngredient<?>> typedIngredients) {
		for (final var typedIngredient : typedIngredients) {
			addTypedIngredient(typedIngredient);
		}
		
		return this;
	}
	
	@Override
	public TMRVIngredientCollector addOptionalTypedIngredients(List<Optional<ITypedIngredient<?>>> typedIngredients) {
		typedIngredients.stream()
			.map(x -> x.orElse(null))
			.forEach(this::addTypedIngredient);
		
		return this;
	}
	
	@Override
	public IIngredientConsumer addItemLike(ItemLike itemLike) {
		return addTypedIngredient((ITypedIngredient<ItemStack>) ItemEmiStack.of(itemLike));
	}
	
	@Override
	public TMRVIngredientCollector addFluidStack(Fluid fluid) {
		return addTypedIngredient((ITypedIngredient<FluidStack>) FluidEmiStack.of(fluid));
	}
	
	@Override
	public TMRVIngredientCollector addFluidStack(Fluid fluid, long amount) {
		return addTypedIngredient((ITypedIngredient<FluidStack>) FluidEmiStack.of(fluid, amount));
	}
	
	@Override
	public TMRVIngredientCollector addFluidStack(Fluid fluid, long amount, DataComponentPatch dataComponentPatch) {
		return addTypedIngredient((ITypedIngredient<FluidStack>) FluidEmiStack.of(fluid, dataComponentPatch, amount));
	}
	//endregion
	
}
