package dev.nolij.toomanyrecipeviewers.impl.api.runtime;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import dev.emi.emi.EmiPort;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.FluidEmiStack;
import dev.emi.emi.api.stack.ItemEmiStack;
import dev.emi.emi.jemi.JemiStack;
import dev.emi.emi.jemi.JemiUtil;
import dev.emi.emi.registry.EmiStackList;
import dev.emi.emi.runtime.EmiReloadManager;
import dev.nolij.toomanyrecipeviewers.TooManyRecipeViewers;
import dev.nolij.toomanyrecipeviewers.util.IJEITypedItemStack;
import dev.nolij.toomanyrecipeviewers.util.IJEMIStack;
import dev.nolij.toomanyrecipeviewers.util.LazyMappedCollection;
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
import mezz.jei.library.ingredients.itemStacks.TypedItemStack;
import mezz.jei.library.ingredients.itemStacks.TypedItemStackExtension;
import mezz.jei.library.plugins.vanilla.ingredients.ItemStackHelper;
import mezz.jei.library.plugins.vanilla.ingredients.fluid.FluidIngredientHelper;
import mezz.jei.library.render.FluidTankRenderer;
import mezz.jei.library.render.ItemStackRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

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
		
		registerIngredientType(new IngredientInfo<>(
			VanillaTypes.ITEM_STACK,
			Collections.emptyList(),
			new ItemStackHelper(runtime.stackHelper, runtime.colorHelper),
			new ItemStackRenderer(),
			ItemStack.STRICT_SINGLE_ITEM_CODEC
		), Collections.emptyList());
		
		//noinspection UnstableApiUsage
		EmiStackList.stacks.stream()
			.filter(ItemEmiStack.class::isInstance)
			.map(ItemEmiStack.class::cast)
			.map(ItemEmiStack::getItemStack)
			.forEach(itemStacks::add);
		
		//noinspection rawtypes,unchecked
		registerIngredientType(new IngredientInfo(
			fluidHelper.getFluidIngredientType(),
			Collections.emptyList(),
			new FluidIngredientHelper<>(runtime.subtypeManager, runtime.colorHelper, fluidHelper),
			new FluidTankRenderer<>(fluidHelper),
			fluidHelper.getCodec()
		), Collections.emptyList());
		
		//noinspection UnstableApiUsage
		EmiStackList.stacks.stream()
			.filter(FluidEmiStack.class::isInstance)
			.map(FluidEmiStack.class::cast)
			.map(this::getRawIngredient)
			.forEach(fluidStacks::add);
	}
	
	public EmiIngredient getEMIIngredient(Stream<ItemStack> itemStackStream) {
		return EmiIngredient.of(itemStackStream.map(EmiStack::of).toList());
	}
	
	@SuppressWarnings("UnstableApiUsage")
	public EmiStack getEMIStack(ItemStack stack) {
		if (stack == null || stack.isEmpty())
			return ItemEmiStack.EMPTY;
		
		return ItemEmiStack.of(stack);
	}
	
	@SuppressWarnings("UnstableApiUsage")
	public EmiStack getEMIStack(FluidStack stack) {
		if (stack == null || stack.isEmpty())
			return FluidEmiStack.EMPTY;
		
		return FluidEmiStack.of(stack.getFluid(), stack.getComponentsPatch(), stack.getAmount());
	}
	
	public <T> EmiStack getEMIStack(IIngredientType<T> ingredientType, T ingredient) {
		//noinspection IfCanBeSwitch
		if (ingredient == null)
			return EmiStack.EMPTY;
		
		if (ingredient instanceof ItemStack itemStack)
			return getEMIStack(itemStack);
		else if (ingredient instanceof FluidStack fluidStack)
			return getEMIStack(fluidStack);
		
		//noinspection unchecked
		final var ingredientInfo = (IngredientInfo<T>) typeInfoMap.get(ingredientType);
		if (ingredientInfo == null)
			return EmiStack.EMPTY;
		
		return new JemiStack<>(ingredientType, ingredientInfo.getIngredientHelper(), ingredientInfo.getIngredientRenderer(), ingredient);
	}
	
	@SuppressWarnings("UnstableApiUsage")
	public <T> EmiStack getEMIStack(ITypedIngredient<T> typedIngredient) {
		if (typedIngredient == null)
			return EmiStack.EMPTY;
			
		if (typedIngredient instanceof IJEITypedItemStack typedItemStack)
			return ItemEmiStack.of(typedItemStack.tmrv$getItem(), typedItemStack.tmrv$getDataComponentPatch(), typedItemStack.tmrv$getCount());
		
		return getEMIStack(typedIngredient.getType(), typedIngredient.getIngredient());
	}
	
	@SuppressWarnings({"UnstableApiUsage"})
	public Object getRawIngredient(EmiStack emiStack) {
		if (emiStack instanceof ItemEmiStack itemEmiStack) {
			return itemEmiStack.getItemStack();
		} else if (emiStack instanceof FluidEmiStack fluidEmiStack) {
			var amount = fluidEmiStack.getAmount();
			if (amount == 0L)
				amount = 1000L;
			return fluidHelper.create(
				Holder.direct((Fluid) fluidEmiStack.getKey()),
				amount,
				fluidEmiStack.getComponentChanges()
			);
		} else if (emiStack instanceof IJEMIStack<?> jemiStack) {
			return jemiStack.tmrv$getIngredient();
		}
		
		final var itemStack = emiStack.getItemStack();
		if (itemStack == null || itemStack.isEmpty())
			return null;
		
		return itemStack;
	}
	
	@SuppressWarnings({"UnstableApiUsage", "rawtypes"})
	public Optional<ITypedIngredient<?>> getTypedIngredient(EmiStack emiStack) {
		try {
			if (emiStack instanceof ItemEmiStack itemEmiStack) {
				return Optional.of(TypedItemStackExtension.create(
					(Item) itemEmiStack.getKey(),
					(int) itemEmiStack.getAmount(),
					itemEmiStack.getComponentChanges()
				));
			} else if (emiStack instanceof FluidEmiStack fluidEmiStack) {
				var amount = fluidEmiStack.getAmount();
				if (amount == 0L)
					amount = 1000L;
				//noinspection unchecked
				return Optional.of(TypedIngredient.createUnvalidated(
					(IIngredientType) fluidHelper.getFluidIngredientType(),
					fluidHelper.create(
						Holder.direct((Fluid) fluidEmiStack.getKey()),
						amount,
						fluidEmiStack.getComponentChanges()
					)
				));
			} else if (emiStack instanceof IJEMIStack<?> jemiStack) {
				//noinspection unchecked
				return Optional.of(TypedIngredient.createUnvalidated(
					(IIngredientType) jemiStack.tmrv$getType(),
					jemiStack.tmrv$getIngredient()
				));
			}
			
			final var itemStack = emiStack.getItemStack();
			if (itemStack == null || itemStack.isEmpty())
				return Optional.empty();
			
			return Optional.of(TypedItemStack.create(itemStack));
		} catch (Throwable t) {
			LOGGER.error("Error converting EmiStack to JEI ITypedIngredient: ", t);
			return Optional.empty();
		}
	}
	
	//region IIngredientManager
	private @Nullable Collection<ItemStack> itemStacks = new ArrayList<>();
	private @Nullable Collection<Object> fluidStacks = new ArrayList<>();
	
	@SuppressWarnings({"unchecked", "UnstableApiUsage", "ReplaceNullCheck"})
	@Override
	public @Unmodifiable <V> Collection<V> getAllIngredients(IIngredientType<V> ingredientType) {
		if (ingredientType == VanillaTypes.ITEM_STACK) {
			if (itemStacks != null) {
				return (Collection<V>) itemStacks;
			} else {
				return (Collection<V>) new LazyMappedCollection<>(EmiStackList.stacks.stream()
					.filter(ItemEmiStack.class::isInstance)
					.map(ItemEmiStack.class::cast)
					.toList(), this::getRawIngredient);
			}
		} else if (ingredientType == fluidHelper.getFluidIngredientType()) {
			if (fluidStacks != null) {
				return (Collection<V>) fluidStacks;
			} else {
				return (Collection<V>) new LazyMappedCollection<>(EmiStackList.stacks.stream()
					.filter(FluidEmiStack.class::isInstance)
					.map(FluidEmiStack.class::cast)
					.toList(), this::getRawIngredient);
			}
		}
		
		return (Collection<V>) EmiStackList.stacks.stream()
			.filter(JemiStack.class::isInstance)
			.map(JemiStack.class::cast)
			.map(x -> x.ingredient)
			.filter(ingredientType.getIngredientClass()::isInstance)
			.toList();
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
		
		if (!this.listeners.isEmpty()) {
			final var typedIngredients = validIngredients.stream()
				.map(i -> TypedIngredient.createUnvalidated(ingredientType, i))
				.toList();
			
			this.listeners.forEach(listener -> listener.onIngredientsAdded(ingredientHelper, typedIngredients));
		}
	}
	
	private final Set<EmiStack> removedStacks = Collections.synchronizedSet(new HashSet<>());
	
	@Override
	public <V> void removeIngredientsAtRuntime(IIngredientType<V> ingredientType, Collection<V> ingredients) {
		if (locked)
			throw new IllegalStateException();
		
		//noinspection unchecked
		final var ingredientInfo = (IngredientInfo<V>) typeInfoMap.get(ingredientType);
		final var ingredientHelper = ingredientInfo.getIngredientHelper();
		
		for (final var ingredient : ingredients) {
			final var emiStack = getEMIStack(ingredientType, ingredient);
			if (!emiStack.isEmpty())
				removedStacks.add(emiStack);
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
	
	private <V> Object getUid(IIngredientType<V> ingredientType, V ingredient) {
		//noinspection unchecked
		final var ingredientInfo = (IngredientInfo<V>) typeInfoMap.get(ingredientType);
		final var ingredientHelper = ingredientInfo.getIngredientHelper();
		
		try {
			return ingredientHelper.getUid(ingredient, UidContext.Ingredient);
		} catch (Throwable getUidException) {
			try {
				LOGGER.error("Failed to get UID for broken ingredient {}", ingredientHelper.getErrorInfo(ingredient), getUidException);
			} catch (Throwable getErrorInfoException) {
				LOGGER.error("Failed to get UID for broken ingredient", getErrorInfoException);
			}
			return null;
		}
	}
	
	private <V> Object getLegacyUid(IIngredientType<V> ingredientType, V ingredient) {
		//noinspection unchecked
		final var ingredientInfo = (IngredientInfo<V>) typeInfoMap.get(ingredientType);
		final var ingredientHelper = ingredientInfo.getIngredientHelper();
		
		try {
			//noinspection removal
			return ingredientHelper.getUniqueId(ingredient, UidContext.Ingredient);
		} catch (Throwable throwable) {
			LOGGER.error("Failed to get legacy UID for broken ingredient", throwable);
			return null;
		}
	}
	
	@SuppressWarnings("removal")
	@Override
	public <V> Optional<V> getIngredientByUid(IIngredientType<V> ingredientType, String uid) {
		return getAllIngredients(ingredientType).stream()
			.filter(x -> uid.equals(getUid(ingredientType, x)) || uid.equals(getLegacyUid(ingredientType, x)))
			.findFirst();
	}
	
	@SuppressWarnings("removal")
	@Override
	public <V> Optional<ITypedIngredient<V>> getTypedIngredientByUid(IIngredientType<V> ingredientType, String uid) {
		return getIngredientByUid(ingredientType, uid)
			.flatMap(i -> {
				ITypedIngredient<V> typedIngredient = TypedIngredient.createAndFilterInvalid(this, ingredientType, i, true);
				return Optional.ofNullable(typedIngredient);
			});
	}
	
	@Override
	public Collection<String> getIngredientAliases(ITypedIngredient<?> typedIngredient) {
		final var emiStack = getEMIStack(typedIngredient);
		final var normalizedEMIStack = emiStack.getEmiStacks().getFirst();
		final var registeredAlias = EmiStackList.registryAliases.stream()
			.filter(x -> 
				x.stacks().stream().anyMatch(normalizedEMIStack::equals))
			.findFirst();
		
		return registeredAlias.<Collection<String>>map(x -> x.text().stream()
				.map(Component::getString)
				.toList())
			.orElse(Collections.emptyList());
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
		registerIngredientType(new IngredientInfo<>(ingredientType, Collections.emptyList(), ingredientHelper, ingredientRenderer, codec), ingredients);
	}
	
	@SuppressWarnings("removal")
	@Override
	public <V> void register(IIngredientType<V> ingredientType, Collection<V> ingredients, IIngredientHelper<V> ingredientHelper, IIngredientRenderer<V> ingredientRenderer) {
		registerIngredientType(new IngredientInfo<>(ingredientType, Collections.emptyList(), ingredientHelper, ingredientRenderer, null), ingredients);
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
	@Override
	public <I> void addAlias(IIngredientType<I> type, I ingredient, String alias) {
		if (locked)
			throw new IllegalStateException();
		
		final var emiStack = getEMIStack(type, ingredient);
		final var normalizedEMIStack = emiStack.getEmiStacks().getFirst();
		//noinspection UnstableApiUsage
		runtime.emiRegistry.addAlias(normalizedEMIStack, Component.translatable(alias));
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
		
		if (itemStacks != null && ingredientType == VanillaTypes.ITEM_STACK)
			//noinspection unchecked
			itemStacks.addAll((Collection<ItemStack>) ingredients);
		else if (fluidStacks != null && ingredientType == fluidHelper.getFluidIngredientType())
			fluidStacks.addAll(ingredients);
			
		for (final var ingredient : ingredients) {
			final var emiStack = getEMIStack(ingredientType, ingredient);
			if (!emiStack.isEmpty())
				runtime.emiRegistry.addEmiStack(emiStack);
		}
	}
	
	private <V> void registerIngredientType(IngredientInfo<V> ingredientInfo, Collection<V> ingredients) {
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
		
		registerIngredients(ingredientType, ingredients);
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
					final var typed = getTypedIngredient(stack).orElse(null);
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
		
		EmiReloadManager.step(Component.literal("[TMRV] Locking JEI Ingredient Registry..."), 100L);
		
		if (!removedStacks.isEmpty())
			runtime.emiRegistry.removeEmiStacks(removedStacks::contains);
		
		registerItemStackDefaultComparison();
		registerFluidDefaultComparison();
		registerOtherJEIIngredientTypeComparisons();
		
		removedStacks.clear();
		
		itemStacks = null;
		fluidStacks = null;
	}
	
}
