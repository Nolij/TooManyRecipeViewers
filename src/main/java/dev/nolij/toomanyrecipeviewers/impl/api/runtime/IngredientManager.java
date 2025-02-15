package dev.nolij.toomanyrecipeviewers.impl.api.runtime;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import dev.emi.emi.EmiPort;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.jemi.JemiStack;
import dev.emi.emi.jemi.JemiUtil;
import dev.nolij.toomanyrecipeviewers.TooManyRecipeViewers;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.ISubtypeManager;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.IExtraIngredientRegistration;
import mezz.jei.api.registration.IIngredientAliasRegistration;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.input.ClickableIngredient;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.core.util.WeakList;
import mezz.jei.library.ingredients.IngredientInfo;
import mezz.jei.library.ingredients.TypedIngredient;
import net.minecraft.client.renderer.Rect2i;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static dev.nolij.toomanyrecipeviewers.TooManyRecipeViewers.fluidHelper;
import static dev.nolij.toomanyrecipeviewers.TooManyRecipeViewersMod.LOGGER;

public class IngredientManager implements IIngredientManager, IModIngredientRegistration, IExtraIngredientRegistration, IIngredientAliasRegistration, TooManyRecipeViewers.ILockable {
	
	private final TooManyRecipeViewers runtime;
	
	private final Map<IIngredientType<?>, IngredientInfo<?>> typeInfoMap = Collections.synchronizedMap(new HashMap<>());
	private final Map<Class<?>, IIngredientType<?>> classTypeMap = Collections.synchronizedMap(new HashMap<>());
	private final Map<Class<?>, IIngredientTypeWithSubtypes<?, ?>> baseClassTypeMap = Collections.synchronizedMap(new HashMap<>());
	
	private final WeakList<IIngredientListener> listeners = new WeakList<>();
	
	private volatile boolean locked = false;
	
	public IngredientManager(TooManyRecipeViewers runtime) {
		runtime.lockAfterRegistration(this);
		this.runtime = runtime;
	}
	
	//region IIngredientManager
	@Override
	public @Unmodifiable <V> Collection<V> getAllIngredients(IIngredientType<V> ingredientType) {
		//noinspection unchecked
		return (Collection<V>) typeInfoMap.get(ingredientType).getAllIngredients();
	}
	
	@Override
	public <V> IIngredientHelper<V> getIngredientHelper(V ingredient) {
		return getIngredientHelper(getIngredientType(ingredient));
	}
	
	@Override
	public <V> IIngredientHelper<V> getIngredientHelper(IIngredientType<V> ingredientType) {
		//noinspection unchecked
		return (IIngredientHelper<V>) typeInfoMap.get(ingredientType).getIngredientHelper();
	}
	
	@Override
	public <V> IIngredientRenderer<V> getIngredientRenderer(V ingredient) {
		return getIngredientRenderer(getIngredientType(ingredient));
	}
	
	@Override
	public <V> IIngredientRenderer<V> getIngredientRenderer(IIngredientType<V> ingredientType) {
		//noinspection unchecked
		return (IIngredientRenderer<V>) typeInfoMap.get(ingredientType).getIngredientRenderer();
	}
	
	@Override
	public <V> Codec<V> getIngredientCodec(IIngredientType<V> ingredientType) {
		//noinspection unchecked
		return (Codec<V>) typeInfoMap.get(ingredientType).getIngredientCodec();
	}
	
	@Override
	public @Unmodifiable Collection<IIngredientType<?>> getRegisteredIngredientTypes() {
		return classTypeMap.values();
	}
	
	@Override
	public Optional<IIngredientType<?>> getIngredientTypeForUid(String uid) {
		return getRegisteredIngredientTypes()
			.stream()
			.filter(x -> uid.equals(x.getUid()))
			.findFirst();
	}
	
	@Override
	public <V> void addIngredientsAtRuntime(IIngredientType<V> ingredientType, Collection<V> ingredients) {
		if (locked)
			throw new IllegalStateException();
		
		//noinspection unchecked
		final var ingredientInfo = (IngredientInfo<V>) typeInfoMap.get(ingredientType);
		final var ingredientHelper = ingredientInfo.getIngredientHelper();
		
		final var validIngredients = ingredients.stream()
			.filter(ingredient -> {
				if (!ingredientHelper.isValidIngredient(ingredient)) {
					LOGGER.error("Attempted to add an invalid Ingredient: {}", ingredientHelper.getErrorInfo(ingredient));
					return false;
				}
				if (!ingredientHelper.isIngredientOnServer(ingredient)) {
					LOGGER.error("Attempted to add an Ingredient that is not on the server: {}", ingredientHelper.getErrorInfo(ingredient));
					return false;
				}
				return true;
			})
			.toList();
		
		registerIngredients(ingredientType, ingredients);
		ingredientInfo.addIngredients(ingredients);
		
		if (!this.listeners.isEmpty()) {
			final var typedIngredients = validIngredients.stream()
				.map(i -> TypedIngredient.createUnvalidated(ingredientType, i))
				.toList();
			
			this.listeners.forEach(listener -> listener.onIngredientsAdded(ingredientHelper, typedIngredients));
		}
	}
	
	@Override
	public <V> void removeIngredientsAtRuntime(IIngredientType<V> ingredientType, Collection<V> ingredients) {
		if (locked)
			throw new IllegalStateException();
		
		//noinspection unchecked
		final var ingredientInfo = (IngredientInfo<V>) typeInfoMap.get(ingredientType);
		final var ingredientHelper = ingredientInfo.getIngredientHelper();
		
		ingredientInfo.removeIngredients(ingredients);
		for (final var ingredient : ingredients) {
			final var emiStack = JemiUtil.getStack(ingredientType, ingredient);
			if (!emiStack.isEmpty())
				runtime.emiRegistry.removeEmiStacks(emiStack);
		}
		
		if (!this.listeners.isEmpty()) {
			final var typedIngredients = TypedIngredient.createAndFilterInvalidNonnullList(this, ingredientType, ingredients, false);
			this.listeners.forEach(listener -> listener.onIngredientsRemoved(ingredientHelper, typedIngredients));
		}
	}
	
	@Override
	public @Nullable <V> IIngredientType<V> getIngredientType(V ingredient) {
		//noinspection unchecked
		return (IIngredientType<V>) classTypeMap.get(ingredient.getClass());
	}
	
	@Override
	public <V> Optional<IIngredientType<V>> getIngredientTypeChecked(V ingredient) {
		return Optional.ofNullable(getIngredientType(ingredient));
	}
	
	@Override
	public <B, I> Optional<IIngredientTypeWithSubtypes<B, I>> getIngredientTypeWithSubtypesFromBase(B baseIngredient) {
		//noinspection unchecked
		return Optional.ofNullable((IIngredientTypeWithSubtypes<B, I>) baseClassTypeMap.get(baseIngredient.getClass()));
	}
	
	@Override
	public <V> Optional<IIngredientType<V>> getIngredientTypeChecked(Class<? extends V> ingredientClass) {
		//noinspection unchecked
		return Optional.ofNullable((IIngredientType<V>) classTypeMap.get(ingredientClass));
	}
	
	@Override
	public <V> Optional<ITypedIngredient<V>> createTypedIngredient(IIngredientType<V> ingredientType, V ingredient) {
		ITypedIngredient<V> result = TypedIngredient.createAndFilterInvalid(this, ingredientType, ingredient, false);
		return Optional.ofNullable(result);
	}
	
	@Override
	public <V> ITypedIngredient<V> normalizeTypedIngredient(ITypedIngredient<V> typedIngredient) {
		IIngredientType<V> type = typedIngredient.getType();
		IIngredientHelper<V> ingredientHelper = getIngredientHelper(type);
		return TypedIngredient.normalize(typedIngredient, ingredientHelper);
	}
	
	@Override
	public <V> Optional<IClickableIngredient<V>> createClickableIngredient(IIngredientType<V> ingredientType, V ingredient, Rect2i area, boolean normalize) {
		ITypedIngredient<V> typedIngredient = TypedIngredient.createAndFilterInvalid(this, ingredientType, ingredient, normalize);
		if (typedIngredient == null) {
			return Optional.empty();
		}
		ImmutableRect2i slotArea = new ImmutableRect2i(area);
		ClickableIngredient<V> clickableIngredient = new ClickableIngredient<>(typedIngredient, slotArea);
		return Optional.of(clickableIngredient);
	}
	
	@SuppressWarnings("removal")
	@Override
	public <V> Optional<V> getIngredientByUid(IIngredientType<V> ingredientType, String uid) {
		//noinspection unchecked
		return ((IngredientInfo<V>) typeInfoMap.get(ingredientType))
			.getIngredientByLegacyUid(uid);
	}
	
	@SuppressWarnings("removal")
	@Override
	public <V> Optional<ITypedIngredient<V>> getTypedIngredientByUid(IIngredientType<V> ingredientType, String uid) {
		//noinspection unchecked
		return ((IngredientInfo<V>) typeInfoMap.get(ingredientType))
			.getIngredientByLegacyUid(uid)
			.flatMap(i -> {
				ITypedIngredient<V> typedIngredient = TypedIngredient.createAndFilterInvalid(this, ingredientType, i, true);
				return Optional.ofNullable(typedIngredient);
			});
	}
	
	@Override
	public Collection<String> getIngredientAliases(ITypedIngredient<?> typedIngredient) {
		//noinspection rawtypes,unchecked
		return typeInfoMap.get(typedIngredient.getType()).getIngredientAliases((ITypedIngredient) typedIngredient);
	}
	
	@Override
	public void registerIngredientListener(IIngredientListener listener) {
		listeners.add(listener);
	}
	//endregion
	
	//region IModIngredientRegistration
	@Override
	public ISubtypeManager getSubtypeManager() {
		return runtime.subtypeManager;
	}
	
	@Override
	public IColorHelper getColorHelper() {
		return runtime.colorHelper;
	}
	
	@Override
	public <V> void register(IIngredientType<V> ingredientType, Collection<V> ingredients, IIngredientHelper<V> ingredientHelper, IIngredientRenderer<V> ingredientRenderer, Codec<V> codec) {
		registerIngredientType(new IngredientInfo<>(ingredientType, ingredients, ingredientHelper, ingredientRenderer, codec));
	}
	
	@SuppressWarnings("removal")
	@Override
	public <V> void register(IIngredientType<V> ingredientType, Collection<V> ingredients, IIngredientHelper<V> ingredientHelper, IIngredientRenderer<V> ingredientRenderer) {
		registerIngredientType(new IngredientInfo<>(ingredientType, ingredients, ingredientHelper, ingredientRenderer, null));
	}
	//endregion
	
	//region IExtraIngredientRegistration
	@Override
	public <V> void addExtraIngredients(IIngredientType<V> ingredientType, Collection<V> ingredients) {
		if (!typeInfoMap.containsKey(ingredientType))
			throw new IllegalArgumentException();
		
		registerIngredients(ingredientType, ingredients);
		//noinspection unchecked
		((IngredientInfo<V>) typeInfoMap.get(ingredientType)).addIngredients(ingredients);
	}
	//endregion
	
	//region IIngredientAliasRegistration
	private volatile boolean aliasesLocked = false;
	private final List<Pair<EmiStack, String>> aliases = new ArrayList<>();
	
	public synchronized List<Pair<EmiStack, String>> getAliasesAndLock() {
		if (aliasesLocked)
			throw new IllegalStateException();
		
		aliasesLocked = true;
		return aliases;
	}
	
	@Override
	public <I> void addAlias(IIngredientType<I> type, I ingredient, String alias) {
		if (aliasesLocked)
			throw new IllegalStateException();
		
		if (typeInfoMap.containsKey(type))
			//noinspection unchecked
			((IngredientInfo<I>) typeInfoMap.get(type)).addIngredientAlias(ingredient, alias);
		
		final var stack = JemiUtil.getStack(type, ingredient);
		aliases.add(Pair.of(stack, alias));
	}
	
	@Override
	public <I> void addAlias(ITypedIngredient<I> typedIngredient, String alias) {
		addAlias(typedIngredient.getType(), typedIngredient.getIngredient(), alias);
	}
	
	@Override
	public <I> void addAliases(Collection<ITypedIngredient<I>> typedIngredients, Collection<String> aliases) {
		for (final var typedIngredient : typedIngredients) {
			for (final var alias : aliases) {
				addAlias(typedIngredient, alias);
			}
		}
	}
	
	@Override
	public <I> void addAliases(Collection<ITypedIngredient<I>> typedIngredients, String alias) {
		for (final var typedIngredient : typedIngredients) {
			addAlias(typedIngredient, alias);
		}
	}
	
	@Override
	public <I> void addAliases(IIngredientType<I> type, Collection<I> ingredients, Collection<String> aliases) {
		for (final var ingredient : ingredients) {
			for (final var alias : aliases) {
				addAlias(type, ingredient, alias);
			}
		}
	}
	
	@Override
	public <I> void addAliases(IIngredientType<I> type, Collection<I> ingredients, String alias) {
		for (final var ingredient : ingredients) {
			addAlias(type, ingredient, alias);
		}
	}
	
	@Override
	public <I> void addAliases(IIngredientType<I> type, I ingredient, Collection<String> aliases) {
		for (final var alias : aliases) {
			addAlias(type, ingredient, alias);
		}
	}
	
	@Override
	public <I> void addAliases(ITypedIngredient<I> typedIngredient, Collection<String> aliases) {
		for (final var alias : aliases) {
			addAlias(typedIngredient, alias);
		}
	}
	//endregion
	
	private <V> void registerIngredients(IIngredientType<V> ingredientType, Collection<V> ingredients) {
		if (locked)
			throw new IllegalStateException();
		
		for (final var ingredient : ingredients) {
			final var emiStack = JemiUtil.getStack(ingredientType, ingredient);
			if (!emiStack.isEmpty())
				runtime.emiRegistry.addEmiStack(emiStack);
		}
	}
	
	private <V> void registerIngredientType(IngredientInfo<V> ingredientInfo) {
		if (locked)
			throw new IllegalStateException();
		
		final var ingredientType = ingredientInfo.getIngredientType();
		
		if (typeInfoMap.containsKey(ingredientType))
			throw new IllegalStateException();
		typeInfoMap.put(ingredientType, ingredientInfo);
		classTypeMap.put(ingredientType.getIngredientClass(), ingredientType);
		if (ingredientType instanceof IIngredientTypeWithSubtypes<?, ?> ingredientTypeWithSubtypes)
			baseClassTypeMap.put(ingredientTypeWithSubtypes.getIngredientBaseClass(), ingredientTypeWithSubtypes);
		
		if (ingredientType == VanillaTypes.ITEM_STACK ||
			ingredientType == fluidHelper.getFluidIngredientType())
			return;
		
		registerIngredients(ingredientType, ingredientInfo.getAllIngredients());
	}
	
	private void registerItemStackDefaultComparison() {
		for (final var item : EmiPort.getItemRegistry()) {
			if (runtime.subtypeManager.hasSubtypes(VanillaTypes.ITEM_STACK, item.getDefaultInstance())) {
				//noinspection removal
				runtime.emiRegistry.setDefaultComparison(item, Comparison.compareData(stack ->
					runtime.subtypeManager.getSubtypeInfo(stack.getItemStack(), UidContext.Recipe)));
			}
		}
	}
	
	private void registerFluidDefaultComparison() {
		for (final var fluid : EmiPort.getFluidRegistry()) {
			//noinspection unchecked
			final var type = (IIngredientTypeWithSubtypes<Object, Object>) JemiUtil.getFluidType();
			//noinspection deprecation
			if (runtime.subtypeManager.hasSubtypes(type, fluidHelper.create(fluid.builtInRegistryHolder(), 1000L))) {
				runtime.emiRegistry.setDefaultComparison(fluid, Comparison.compareData(stack -> {
					final var typed = JemiUtil.getTyped(stack).orElse(null);
					if (typed != null) {
						return runtime.subtypeManager.getSubtypeInfo(type, typed.getIngredient(), UidContext.Recipe);
					}
					return null;
				}));
			}
		}
	}
	
	private void registerOtherJEIIngredientTypeComparisons() {
		final var jeiIngredientTypes = Lists.newArrayList(runtime.ingredientManager.getRegisteredIngredientTypes());
		for (final var _jeiIngredientType : jeiIngredientTypes) {
			if (_jeiIngredientType == VanillaTypes.ITEM_STACK || _jeiIngredientType == JemiUtil.getFluidType()) {
				continue;
			}
			//noinspection rawtypes
			if (_jeiIngredientType instanceof final IIngredientTypeWithSubtypes jeiIngredientType) {
				final var jeiIngredients = Lists.newArrayList(runtime.ingredientManager.getAllIngredients(_jeiIngredientType));
				for (final var jeiIngredient : jeiIngredients) {
					try {
						//noinspection unchecked
						if (runtime.subtypeManager.hasSubtypes(jeiIngredientType, jeiIngredient)) {
							//noinspection unchecked
							runtime.emiRegistry.setDefaultComparison(jeiIngredientType.getBase(jeiIngredient), Comparison.compareData(stack -> {
								if (stack instanceof final JemiStack<?> jemiStack) {
									//noinspection unchecked
									return runtime.subtypeManager.getSubtypeInfo(jeiIngredientType, jemiStack.ingredient, UidContext.Recipe);
								}
								return null;
							}));
						}
					} catch (Throwable t) {
						LOGGER.error("Exception adding default comparison for JEI ingredient: ", t);
					}
				}
			}
		}
	}
	
	@Override
	public synchronized void lock() throws IllegalStateException {
		if (locked)
			throw new IllegalStateException();
		
		locked = true;
		
		registerItemStackDefaultComparison();
		registerFluidDefaultComparison();
		registerOtherJEIIngredientTypeComparisons();
	}
	
}
