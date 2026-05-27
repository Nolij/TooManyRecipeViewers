package dev.nolij.toomanyrecipeviewers.impl.jei.api.runtime;

//? if >=21.1 {
import com.mojang.serialization.Codec;
import mezz.jei.api.gui.builder.IClickableIngredientFactory;
import mezz.jei.common.input.ClickableIngredientFactory;
//?}
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.FluidEmiStack;
import dev.emi.emi.api.stack.ItemEmiStack;
import dev.emi.emi.registry.EmiStackList;
import dev.emi.emi.runtime.EmiReloadManager;
import dev.nolij.toomanyrecipeviewers.TooManyRecipeViewers;
import dev.nolij.toomanyrecipeviewers.impl.ingredient.ErrorEmiStack;
import dev.nolij.toomanyrecipeviewers.impl.ingredient.ErrorIngredient;
import dev.nolij.toomanyrecipeviewers.impl.ingredient.TMRVStack;
import dev.nolij.toomanyrecipeviewers.util.IItemStackish;
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
import mezz.jei.library.plugins.vanilla.ingredients.ItemStackHelper;
import mezz.jei.library.plugins.vanilla.ingredients.fluid.FluidIngredientHelper;
import mezz.jei.library.render.FluidTankRenderer;
import mezz.jei.library.render.ItemStackRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static dev.nolij.toomanyrecipeviewers.TooManyRecipeViewers.fluidHelper;
import static dev.nolij.toomanyrecipeviewers.TooManyRecipeViewersMod.LOGGER;

public class IngredientManager implements IIngredientManager, IModIngredientRegistration, IExtraIngredientRegistration, IIngredientAliasRegistration, TooManyRecipeViewers.ILockable, TooManyRecipeViewers.IPostBakeListener {
	
	private final TooManyRecipeViewers runtime;
	
	private final Map<IIngredientType<?>, IngredientInfo<?>> typeInfoMap = new ConcurrentHashMap<>();
	private final Map<Class<?>, IIngredientType<?>> classTypeMap = new ConcurrentHashMap<>();
	private final Map<Class<?>, IIngredientTypeWithSubtypes<?, ?>> baseClassTypeMap = new ConcurrentHashMap<>();
	
	private final WeakList<IIngredientListener> listeners = new WeakList<>();
	
	private @Nullable Collection<ItemStack> itemStacks = new ArrayList<>();
	private @Nullable Collection<FluidStack> fluidStacks = new ArrayList<>();
	private @Nullable Multimap<IIngredientType<?>, ITypedIngredient<?>> typedIngredients = Multimaps.synchronizedMultimap(HashMultimap.create());
	
	private record TypedIngredientUID(IIngredientType<?> type, String uid) {}
	
	private final Map<String, IIngredientType<?>> typeUidLookup = new ConcurrentHashMap<>();
	private final Map<TypedIngredientUID, Object> ingredientUidLookup = new ConcurrentHashMap<>();
	
	private volatile boolean locked = false;
	
	public IngredientManager(TooManyRecipeViewers runtime) {
		runtime.lockAfterRegistration(this);
		runtime.addPostBakeListener(this);
		this.runtime = runtime;
		
		registerIngredientType(new IngredientInfo<>(
			ErrorIngredient.INSTANCE,
			Collections.emptyList(),
			ErrorIngredient.INSTANCE,
			ErrorIngredient.INSTANCE
			//? if >=21.1
			, null
		), Collections.emptyList());
		
		registerIngredientType(new IngredientInfo<>(
			VanillaTypes.ITEM_STACK,
			Collections.emptyList(),
			new ItemStackHelper(
				//? if <21.1
				//runtime.subtypeManager,
				runtime.stackHelper,
				runtime.colorHelper
			),
			new ItemStackRenderer()
			//? if >=21.1
			, ItemStack.STRICT_SINGLE_ITEM_CODEC
		), Collections.emptyList());
		
		//noinspection rawtypes,unchecked
		registerIngredientType(new IngredientInfo(
			fluidHelper.getFluidIngredientType(),
			Collections.emptyList(),
			new FluidIngredientHelper<>(runtime.subtypeManager, runtime.colorHelper, fluidHelper),
			new FluidTankRenderer<>(fluidHelper)
			//? if >=21.1
			, fluidHelper.getCodec()
		), Collections.emptyList());
		
		for (final var emiStack : EmiStackList.stacks) {
			//noinspection rawtypes
			if (!(emiStack instanceof ITypedIngredient typedIngredient))
				continue;
			
			final var type = typedIngredient.getType();
			final var ingredient = typedIngredient.getIngredient();
			
			switch (ingredient) {
				case ItemStack itemStack -> itemStacks.add(itemStack);
				case FluidStack fluidStack -> fluidStacks.add(fluidStack);
				default -> {}
			}
			
			typedIngredients.put(type, typedIngredient);
			//noinspection unchecked
			registerIngredientBaseComparison(type, ingredient);
			
			try {
				//noinspection unchecked
				ingredientUidLookup.put(getLegacyUid(type, ingredient), typedIngredient);
			} catch (Throwable t) {
				LOGGER.error("Error adding UID lookup entry for stack {}", emiStack, t);
			}
		}
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
	
	public <T> EmiStack getEMIStack(IIngredientType<T> jeiType, T ingredient) {
		if (ingredient == null)
			return EmiStack.EMPTY;
		else if (jeiType == null || ingredient == ErrorIngredient.INSTANCE)
			return ErrorEmiStack.INSTANCE;
		else if (ingredient instanceof ItemStack itemStack)
			return getEMIStack(itemStack);
		else if (ingredient instanceof FluidStack fluidStack)
			return getEMIStack(fluidStack);
		
		final var ingredientInfo = getIngredientInfo(jeiType);
		if (ingredientInfo == null)
			return ErrorEmiStack.INSTANCE;
		
		return new TMRVStack<>(jeiType, ingredientInfo.getIngredientHelper(), ingredientInfo.getIngredientRenderer(), ingredient);
	}
	
	@SuppressWarnings("UnstableApiUsage")
	public <T> EmiStack getEMIStack(@Nullable ITypedIngredient<T> typedIngredient) {
		return switch ((Object) typedIngredient) {
			case null -> EmiStack.EMPTY;
			case EmiStack emiStack -> emiStack;
			case IItemStackish<?> typedItemStack ->
				ItemEmiStack.of(typedItemStack.tmrv$getItem(), typedItemStack.tmrv$getDataComponentPatch(), typedItemStack.tmrv$getAmount());
			default -> getEMIStack(typedIngredient.getType(), typedIngredient.getIngredient());
		};
	}
	
	public Optional<ITypedIngredient<?>> getTypedIngredient(EmiStack emiStack) {
		if (emiStack instanceof ITypedIngredient<?> typedIngredient)
			return Optional.of(typedIngredient);
		
		return Optional.empty();
	}
	
	public <T> IIngredientType<T> getIngredientType(String uid) {
		//noinspection unchecked
		return (IIngredientType<T>) typeUidLookup.get(uid);
	}
	
	public <T> IngredientInfo<T> getIngredientInfo(IIngredientType<T> jeiType) {
		//noinspection unchecked
		return (IngredientInfo<T>) typeInfoMap.get(jeiType);
	}
	
	//region IIngredientManager	
	public @Unmodifiable <V> Collection<ITypedIngredient<V>> getAllTypedIngredients(IIngredientType<V> jeiType) {
		if (typedIngredients != null) {
			//noinspection rawtypes,unchecked
			return (Collection) typedIngredients.get(jeiType);
		}
		
		//noinspection unchecked
		return EmiStackList.stacks.stream()
			.filter(ITypedIngredient.class::isInstance)
			.map(ITypedIngredient.class::cast)
			.filter(x -> x.getType() == jeiType)
			.map(x -> (ITypedIngredient<V>) x)
			.toList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public @Unmodifiable <V> Collection<V> getAllIngredients(IIngredientType<V> jeiType) {
		if (jeiType == VanillaTypes.ITEM_STACK && itemStacks != null) {
			return (Collection<V>) itemStacks;
		} else if (jeiType == fluidHelper.getFluidIngredientType() && fluidStacks != null) {
			return (Collection<V>) fluidStacks;
		}
		
		return getAllTypedIngredients(jeiType)
			.stream()
			.map(ITypedIngredient::getIngredient)
			.toList();
	}
	
	@Override
	public <V> IIngredientHelper<V> getIngredientHelper(V ingredient) {
		return getIngredientHelper(getIngredientType(ingredient));
	}
	
	@Override
	public <V> IIngredientHelper<V> getIngredientHelper(IIngredientType<V> jeiType) {
		return getIngredientInfo(jeiType).getIngredientHelper();
	}
	
	@Override
	public <V> IIngredientRenderer<V> getIngredientRenderer(V ingredient) {
		return getIngredientRenderer(getIngredientType(ingredient));
	}
	
	@Override
	public <V> IIngredientRenderer<V> getIngredientRenderer(IIngredientType<V> jeiType) {
		return getIngredientInfo(jeiType).getIngredientRenderer();
	}
	
	//? if >=21.1 {
	@Override
	public <V> Codec<V> getIngredientCodec(IIngredientType<V> jeiType) {
		return getIngredientInfo(jeiType).getIngredientCodec();
	}
	//?}
	
	@Override
	public @Unmodifiable Collection<IIngredientType<?>> getRegisteredIngredientTypes() {
		return classTypeMap.values();
	}
	
	@Override
	public Optional<IIngredientType<?>> getIngredientTypeForUid(String uid) {
		return Optional.ofNullable(typeUidLookup.getOrDefault(uid, null));
	}
	
	@Override
	public <V> void addIngredientsAtRuntime(IIngredientType<V> jeiType, Collection<V> ingredients) {
		if (locked) {
			LOGGER.error(new IllegalStateException("Tried to add ingredients after registry is locked"));
			return;
		}
		
		final var ingredientInfo = getIngredientInfo(jeiType);
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
		
		registerIngredients(jeiType, ingredients);
		
		if (!this.listeners.isEmpty()) {
			final var typedIngredients = validIngredients.stream()
				.map(i -> TypedIngredient.createUnvalidated(jeiType, i))
				.toList();
			
			this.listeners.forEach(listener -> listener.onIngredientsAdded(ingredientHelper, typedIngredients));
		}
	}
	
	private final Set<EmiStack> removedStacks = Collections.synchronizedSet(new HashSet<>());
	
	@Override
	public <V> void removeIngredientsAtRuntime(IIngredientType<V> jeiType, Collection<V> ingredients) {
		if (locked) {
			LOGGER.error(new IllegalStateException("Tried to remove ingredients after registry is locked"));
			return;
		}
		
		final var ingredientInfo = getIngredientInfo(jeiType);
		final var ingredientHelper = ingredientInfo.getIngredientHelper();
		
		for (final var ingredient : ingredients) {
			final var emiStack = getEMIStack(jeiType, ingredient);
			if (!emiStack.isEmpty())
				removedStacks.add(emiStack);
		}
		
		if (!this.listeners.isEmpty()) {
			//? if >=21.1 {
			final var typedIngredients = TypedIngredient.createAndFilterInvalidNonnullList(this, jeiType, ingredients, false);
			//?} else {
			/*final var typedIngredients = ingredients.stream()
				.flatMap(i -> TypedIngredient.createAndFilterInvalid(this, jeiType, i, false).stream())
				.toList();
			*///?}
			this.listeners.forEach(listener -> listener.onIngredientsRemoved(ingredientHelper, typedIngredients));
		}
	}
	
	//? if >=21.1
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
	
	//? if <21.1
	//@SuppressWarnings("UnnecessaryLocalVariable")
	@Override
	public <V> Optional<ITypedIngredient<V>> createTypedIngredient(IIngredientType<V> jeiType, V ingredient /*? if >=21.1 {*/, boolean normalize /*?}*/) {
        //? if <21.1
        //boolean normalize = false;
		final var result = TypedIngredient.createAndFilterInvalid(this, jeiType, ingredient, normalize);
		//? if >=21.1 {
		return Optional.ofNullable(result);
		//?} else
		//return result;
	}
	
	@Override
	public <V> ITypedIngredient<V> normalizeTypedIngredient(ITypedIngredient<V> typedIngredient) {
		final var type = typedIngredient.getType();
		final var ingredientHelper = getIngredientHelper(type);
		return TypedIngredient.normalize(typedIngredient, ingredientHelper);
	}
	
	//? if >=21.1
	@SuppressWarnings("removal")
	@Override
	public <V> Optional<IClickableIngredient<V>> createClickableIngredient(IIngredientType<V> jeiType, V ingredient, Rect2i area, boolean normalize) {
		final var typedIngredient = TypedIngredient.createAndFilterInvalid(this, jeiType, ingredient, normalize)
			//? if <21.1
			//.orElse(null)
			;
		if (typedIngredient == null) {
			return Optional.empty();
		}
		final var slotArea = new ImmutableRect2i(area);
		final var clickableIngredient = new ClickableIngredient<>(typedIngredient, slotArea);
		return Optional.of(clickableIngredient);
	}
	
	//? if >=21.1
	@SuppressWarnings("removal")
	private <V> TypedIngredientUID getLegacyUid(IIngredientType<V> jeiType, V ingredient) {
		final var ingredientInfo = getIngredientInfo(jeiType);
		final var ingredientHelper = ingredientInfo.getIngredientHelper();
		
		try {
			return new TypedIngredientUID(jeiType, ingredientHelper.getUniqueId(ingredient, UidContext.Ingredient));
		} catch (Throwable throwable) {
			LOGGER.error("Failed to get legacy UID for broken ingredient", throwable);
			return null;
		}
	}
	
	@SuppressWarnings({
		//? if >=21.1
		"removal",
		"unchecked"})
	@Override
	public <V> Optional<V> getIngredientByUid(IIngredientType<V> jeiType, String uid) {
		if (jeiType == VanillaTypes.ITEM_STACK || jeiType == fluidHelper.getFluidIngredientType())
			return Optional.ofNullable(((ITypedIngredient<V>) ingredientUidLookup.getOrDefault(new TypedIngredientUID(jeiType, uid), null)).getIngredient());
		
		return Optional.ofNullable((V) ingredientUidLookup.getOrDefault(new TypedIngredientUID(jeiType, uid), null));
	}
	
	@SuppressWarnings({
		//? if >=21.1
		"removal", 
		"unchecked"})
	@Override
	public <V> Optional<ITypedIngredient<V>> getTypedIngredientByUid(IIngredientType<V> jeiType, String uid) {
		if (jeiType == VanillaTypes.ITEM_STACK || jeiType == fluidHelper.getFluidIngredientType())
			return Optional.ofNullable((ITypedIngredient<V>) ingredientUidLookup.getOrDefault(new TypedIngredientUID(jeiType, uid), null));
		
		return Optional.ofNullable((ITypedIngredient<V>) getEMIStack(jeiType, (V) ingredientUidLookup.getOrDefault(new TypedIngredientUID(jeiType, uid), null)));
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
	
	//? if >=21.1 {
	@Override
	public <V> void register(IIngredientType<V> jeiType, Collection<V> ingredients, IIngredientHelper<V> ingredientHelper, IIngredientRenderer<V> ingredientRenderer, Codec<V> codec) {
		registerIngredientType(new IngredientInfo<>(jeiType, Collections.emptyList(), ingredientHelper, ingredientRenderer, codec), ingredients);
	}
	
	@SuppressWarnings("removal") //?}
	@Override
	public <V> void register(IIngredientType<V> jeiType, Collection<V> ingredients, IIngredientHelper<V> ingredientHelper, IIngredientRenderer<V> ingredientRenderer) {
		registerIngredientType(new IngredientInfo<>(jeiType, Collections.emptyList(), ingredientHelper, ingredientRenderer
			//? if >=21.1
			, null
		), ingredients);
	}
	//endregion
	
	//region IExtraIngredientRegistration
	@Override
	public <V> void addExtraIngredients(IIngredientType<V> jeiType, Collection<V> ingredients) {
		registerIngredients(jeiType, ingredients);
	}
	//endregion
	
	//region IIngredientAliasRegistration
	@Override
	public <I> void addAlias(IIngredientType<I> type, I ingredient, String alias) {
		if (locked)
			throw new IllegalStateException("Tried to add ingredient alias after registry is locked");
		
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

    //? if >=21.1 {
    @Override
    public IClickableIngredientFactory getClickableIngredientFactory() {
        return new ClickableIngredientFactory(this);
    }
    //?}

    private <V> void registerIngredients(IIngredientType<V> jeiType, Collection<V> ingredients) {
		if (locked)
			throw new IllegalStateException("Tried to add ingredients after registry is locked");
		
		if (itemStacks != null && jeiType == VanillaTypes.ITEM_STACK)
			//noinspection unchecked
			itemStacks.addAll((Collection<ItemStack>) ingredients);
		else if (fluidStacks != null && jeiType == fluidHelper.getFluidIngredientType())
			//noinspection unchecked
			fluidStacks.addAll((Collection<FluidStack>) ingredients);
		
		for (final var ingredient : ingredients) {
			final var emiStack = getEMIStack(jeiType, ingredient);
			
			if (jeiType == VanillaTypes.ITEM_STACK || jeiType == fluidHelper.getFluidIngredientType())
				ingredientUidLookup.put(getLegacyUid(jeiType, ingredient), emiStack);
			else
				ingredientUidLookup.put(getLegacyUid(jeiType, ingredient), ingredient);
			
			if (typedIngredients != null) {
				//noinspection unchecked
				typedIngredients.put(jeiType, (ITypedIngredient<V>) emiStack);
			}
			
			registerIngredientBaseComparison(jeiType, ingredient);
			
			if (!emiStack.isEmpty())
				runtime.emiRegistry.addEmiStack(emiStack);
		}
	}
	
	private <V> void registerIngredientBaseComparison(IIngredientType<V> jeiType, V ingredient) {
		if (!(jeiType instanceof IIngredientTypeWithSubtypes<?, V> jeiTypeWithSubtypes &&
			runtime.subtypeManager.hasSubtypes(jeiTypeWithSubtypes, ingredient)))
			return;
		
		runtime.emiRegistry.setDefaultComparison(jeiTypeWithSubtypes.getBase(ingredient), Comparison.compareData(stack -> {
			try {
				//noinspection rawtypes
				if (stack instanceof ITypedIngredient typedIngredient) {
					//? if >=21.1 {
					//noinspection unchecked
					return runtime.subtypeManager.getSubtypeData(jeiTypeWithSubtypes, typedIngredient, UidContext.Ingredient);
					//?} else {
					/*//noinspection unchecked
					return runtime.subtypeManager.getSubtypeInfo(jeiTypeWithSubtypes, (V) typedIngredient.getIngredient(), UidContext.Ingredient);
					*///?}
				}
			} catch (Throwable t) {
				LOGGER.error("Exception thrown getting subtype data for stack: {}", stack, t);
			}
			return null;
		}));
	}
	
	private <V> void registerIngredientType(IngredientInfo<V> ingredientInfo, Collection<V> ingredients) {
		if (locked)
			throw new IllegalStateException("Tried to add ingredient type after registry is locked");
		
		final var jeiType = ingredientInfo.getIngredientType();
		
		if (typeInfoMap.containsKey(jeiType))
			throw new IllegalStateException();
		typeInfoMap.put(jeiType, ingredientInfo);
		classTypeMap.put(jeiType.getIngredientClass(), jeiType);
		if (jeiType instanceof IIngredientTypeWithSubtypes<?, ?> jeiTypeWithSubtypes)
			baseClassTypeMap.put(jeiTypeWithSubtypes.getIngredientBaseClass(), jeiTypeWithSubtypes);
		typeUidLookup.put(jeiType.getUid(), jeiType);
		
		if (jeiType == VanillaTypes.ITEM_STACK ||
			jeiType == fluidHelper.getFluidIngredientType())
			return;
		
		registerIngredients(jeiType, ingredients);
	}
	
	@Override
	public synchronized void lock() throws IllegalStateException {
		if (locked)
			throw new IllegalStateException();
		locked = true;
		
		EmiReloadManager.step(Component.literal("[TMRV] Locking JEI Ingredient Registry..."), 100L);
		
		if (!removedStacks.isEmpty()) {
			runtime.emiRegistry.removeEmiStacks(removedStacks::contains);
		}
	}
	
	@Override
	public void recipesBaked() throws IllegalStateException {
		if (!locked)
			throw new IllegalStateException();
		
		removedStacks.clear();
		
		itemStacks = null;
		fluidStacks = null;
		typedIngredients = null;
	}
	
}
