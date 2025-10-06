package dev.nolij.toomanyrecipeviewers.impl.jei.api.runtime;

//? if >=21.1 {
import com.mojang.serialization.Codec;
import mezz.jei.api.gui.builder.IClickableIngredientFactory;
import mezz.jei.common.input.ClickableIngredientFactory;
//?}
import com.google.common.collect.Lists;
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
import dev.nolij.toomanyrecipeviewers.impl.ingredient.ErrorEmiStack;
import dev.nolij.toomanyrecipeviewers.impl.ingredient.ErrorIngredient;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static dev.nolij.toomanyrecipeviewers.TooManyRecipeViewers.fluidHelper;
import static dev.nolij.toomanyrecipeviewers.TooManyRecipeViewersMod.LOGGER;

public class IngredientManager implements IIngredientManager, IModIngredientRegistration, IExtraIngredientRegistration, IIngredientAliasRegistration, TooManyRecipeViewers.ILockable {
	
	private final TooManyRecipeViewers runtime;
	
	private final Map<IIngredientType<?>, IngredientInfo<?>> typeInfoMap = Collections.synchronizedMap(new HashMap<>());
	private final Map<Class<?>, IIngredientType<?>> classTypeMap = Collections.synchronizedMap(new HashMap<>());
	private final Map<Class<?>, IIngredientTypeWithSubtypes<?, ?>> baseClassTypeMap = Collections.synchronizedMap(new HashMap<>());
	
	private final WeakList<IIngredientListener> listeners = new WeakList<>();
	
	private @Nullable Collection<ItemStack> itemStacks = new ArrayList<>();
	private @Nullable Collection<Object> fluidStacks = new ArrayList<>();
	
	private record TypedIngredientUID(
		String typeUid, 
		Object ingredientUid
		//? if >=21.1
		, boolean legacy
	) {
		//? if >=21.1 {
		private TypedIngredientUID(String typeUid, Object ingredientUid) {
			this(typeUid, ingredientUid, false);
		}
		//?}
	}
	
	private final Map<TypedIngredientUID, ITypedIngredient<?>> uidLookup = new ConcurrentHashMap<>();
	
	private volatile boolean locked = false;
	
	public IngredientManager(TooManyRecipeViewers runtime) {
		runtime.lockAfterRegistration(this);
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
				/*runtime.subtypeManager,*/
				runtime.stackHelper,
				runtime.colorHelper
			),
			new ItemStackRenderer()
			//? if >=21.1
			, ItemStack.STRICT_SINGLE_ITEM_CODEC
		), Collections.emptyList());
		
		//noinspection UnstableApiUsage
		EmiStackList.stacks.stream()
			.filter(ItemEmiStack.class::isInstance)
			.map(ITypedIngredient.class::cast)
			.forEach(x -> {
				final var itemStack = (ItemStack) x.getIngredient();
				
				itemStacks.add(itemStack);
				
				try {
					uidLookup.put(getUid(VanillaTypes.ITEM_STACK, itemStack), x);
					//? if >=21.1
					uidLookup.put(getLegacyUid(VanillaTypes.ITEM_STACK, itemStack), x);
				} catch (Throwable t) {
					LOGGER.error("Broken ItemStack {}", itemStack.toString(), t);
				}
			});
		
		//noinspection rawtypes,unchecked
		registerIngredientType(new IngredientInfo(
			fluidHelper.getFluidIngredientType(),
			Collections.emptyList(),
			new FluidIngredientHelper<>(runtime.subtypeManager, runtime.colorHelper, fluidHelper),
			new FluidTankRenderer<>(fluidHelper)
			//? if >=21.1
			, fluidHelper.getCodec()
		), Collections.emptyList());
		
		//noinspection UnstableApiUsage
		EmiStackList.stacks.stream()
			.filter(FluidEmiStack.class::isInstance)
			.map(ITypedIngredient.class::cast)
			.forEach(x -> {
				//noinspection rawtypes
				final var fluidType = (IIngredientType) fluidHelper.getFluidIngredientType();
				final var fluidStack = (FluidStack) x.getIngredient();
				
				fluidStacks.add(fluidStack);
				
				try {
					//noinspection unchecked
					uidLookup.put(getUid(fluidType, fluidStack), x);
					//? if >=21.1 {
					//noinspection unchecked
					uidLookup.put(getLegacyUid(fluidType, fluidStack), x);
					//?}
				} catch (Throwable t) {
					LOGGER.error("Broken FluidStack {}", fluidStack.toString(), t);
				}
			});
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
		if (ingredient == null)
			return EmiStack.EMPTY;
		else if (ingredientType == null || ingredient == ErrorIngredient.INSTANCE)
			return ErrorEmiStack.INSTANCE;
		else if (ingredient instanceof ItemStack itemStack)
			return getEMIStack(itemStack);
		else if (ingredient instanceof FluidStack fluidStack)
			return getEMIStack(fluidStack);
		
		//noinspection unchecked
		final var ingredientInfo = (IngredientInfo<T>) typeInfoMap.get(ingredientType);
		if (ingredientInfo == null)
			return ErrorEmiStack.INSTANCE;
		
		return new JemiStack<>(ingredientType, ingredientInfo.getIngredientHelper(), ingredientInfo.getIngredientRenderer(), ingredient);
	}
	
	@SuppressWarnings("UnstableApiUsage")
	public <T> EmiStack getEMIStack(@Nullable ITypedIngredient<T> typedIngredient) {
		return switch (typedIngredient) {
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
	
	//region IIngredientManager	
	@SuppressWarnings("unchecked")
	public @Unmodifiable <V> Collection<ITypedIngredient<V>> getAllTypedIngredients(IIngredientType<V> ingredientType) {
		return EmiStackList.stacks.stream()
			.filter(ITypedIngredient.class::isInstance)
			.map(ITypedIngredient.class::cast)
			.filter(x -> x.getType() == ingredientType)
			.map(x -> (ITypedIngredient<V>) x)
			.toList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public @Unmodifiable <V> Collection<V> getAllIngredients(IIngredientType<V> ingredientType) {
		if (ingredientType == VanillaTypes.ITEM_STACK && itemStacks != null) {
			return (Collection<V>) itemStacks;
		} else if (ingredientType == fluidHelper.getFluidIngredientType() && fluidStacks != null) {
			return (Collection<V>) fluidStacks;
		}
		
		return getAllTypedIngredients(ingredientType)
			.stream()
			.map(ITypedIngredient::getIngredient)
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
	
	//? if >=21.1 {
	@Override
	public <V> Codec<V> getIngredientCodec(IIngredientType<V> ingredientType) {
		//noinspection unchecked
		return (Codec<V>) typeInfoMap.get(ingredientType).getIngredientCodec();
	}
	//?}
	
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
		if (locked) {
			LOGGER.error(new IllegalStateException("Tried to add ingredients after registry is locked"));
			return;
		}
		
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
		if (locked) {
			LOGGER.error(new IllegalStateException("Tried to remove ingredients after registry is locked"));
			return;
		}
		
		//noinspection unchecked
		final var ingredientInfo = (IngredientInfo<V>) typeInfoMap.get(ingredientType);
		final var ingredientHelper = ingredientInfo.getIngredientHelper();
		
		for (final var ingredient : ingredients) {
			final var emiStack = getEMIStack(ingredientType, ingredient);
			if (!emiStack.isEmpty())
				removedStacks.add(emiStack);
		}
		
		if (!this.listeners.isEmpty()) {
			//? if >=21.1 {
			final var typedIngredients = TypedIngredient.createAndFilterInvalidNonnullList(this, ingredientType, ingredients, false);
			//?} else {
			/*final var typedIngredients = ingredients.stream()
				.flatMap(i -> TypedIngredient.createAndFilterInvalid(this, ingredientType, i, false).stream())
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
	/*@SuppressWarnings("UnnecessaryLocalVariable")*/
	@Override
	public <V> Optional<ITypedIngredient<V>> createTypedIngredient(IIngredientType<V> ingredientType, V ingredient /*? if >=21.1 {*/, boolean normalize /*?}*/) {
        //? if <21.1
        /*boolean normalize = false;*/
		final var result = TypedIngredient.createAndFilterInvalid(this, ingredientType, ingredient, normalize);
		//? if >=21.1 {
		return Optional.ofNullable(result);
		//?} else
		/*return result;*/
	}
	
	@Override
	public <V> ITypedIngredient<V> normalizeTypedIngredient(ITypedIngredient<V> typedIngredient) {
		final var type = typedIngredient.getType();
		final var ingredientHelper = getIngredientHelper(type);
		return TypedIngredient.normalize(typedIngredient, ingredientHelper);
	}
	
	@Override
	public <V> Optional<IClickableIngredient<V>> createClickableIngredient(IIngredientType<V> ingredientType, V ingredient, Rect2i area, boolean normalize) {
		final var typedIngredient = TypedIngredient.createAndFilterInvalid(this, ingredientType, ingredient, normalize)
			//? if <21.1
			/*.orElse(null)*/
			;
		if (typedIngredient == null) {
			return Optional.empty();
		}
		final var slotArea = new ImmutableRect2i(area);
		final var clickableIngredient = new ClickableIngredient<>(typedIngredient, slotArea);
		return Optional.of(clickableIngredient);
	}
	
	private <V> TypedIngredientUID getUid(IIngredientType<V> ingredientType, V ingredient) {
		//? if <21.1 {
		/*return getLegacyUid(ingredientType, ingredient);
		*///?} else {
		//noinspection unchecked
		final var ingredientInfo = (IngredientInfo<V>) typeInfoMap.get(ingredientType);
		final var ingredientHelper = ingredientInfo.getIngredientHelper();
		final var typeUid = ingredientType.getUid();
		
		try {
			return new TypedIngredientUID(typeUid, ingredientHelper.getUid(ingredient, UidContext.Ingredient));
		} catch (Throwable getUidException) {
			try {
				LOGGER.error("Failed to get UID for broken ingredient {}", ingredientHelper.getErrorInfo(ingredient), getUidException);
			} catch (Throwable getErrorInfoException) {
				LOGGER.error("Failed to get UID for broken ingredient", getErrorInfoException);
			}
			return null;
		}
		//?}
	}
	
	//? if >=21.1
	@SuppressWarnings("removal")
	private <V> TypedIngredientUID getLegacyUid(IIngredientType<V> ingredientType, V ingredient) {
		//noinspection unchecked
		final var ingredientInfo = (IngredientInfo<V>) typeInfoMap.get(ingredientType);
		final var ingredientHelper = ingredientInfo.getIngredientHelper();
		final var typeUid = ingredientType.getUid();
		
		try {
			return new TypedIngredientUID(typeUid, ingredientHelper.getUniqueId(ingredient, UidContext.Ingredient)
				//? if >=21.1
				, true
			);
		} catch (Throwable throwable) {
			LOGGER.error("Failed to get legacy UID for broken ingredient", throwable);
			return null;
		}
	}
	
	//? if >=21.1
	@SuppressWarnings("removal")
	@Override
	public <V> Optional<V> getIngredientByUid(IIngredientType<V> ingredientType, String uid) {
		return getTypedIngredientByUid(ingredientType, uid)
			.map(ITypedIngredient::getIngredient);
	}
	
	//? if >=21.1
	@SuppressWarnings("removal")
	@Override
	public <V> Optional<ITypedIngredient<V>> getTypedIngredientByUid(IIngredientType<V> ingredientType, String uid) {
		//noinspection unchecked
		return Optional.ofNullable(
			(ITypedIngredient<V>) uidLookup.getOrDefault(
				new TypedIngredientUID(ingredientType.getUid(), uid), 
				//? if >=21.1 {
				uidLookup.getOrDefault(new TypedIngredientUID(ingredientType.getUid(), uid, true), null)
				//?} else
				/*null*/
			)
		);
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
	public <V> void register(IIngredientType<V> ingredientType, Collection<V> ingredients, IIngredientHelper<V> ingredientHelper, IIngredientRenderer<V> ingredientRenderer, Codec<V> codec) {
		registerIngredientType(new IngredientInfo<>(ingredientType, Collections.emptyList(), ingredientHelper, ingredientRenderer, codec), ingredients);
	}
	
	@SuppressWarnings("removal") //?}
	@Override
	public <V> void register(IIngredientType<V> ingredientType, Collection<V> ingredients, IIngredientHelper<V> ingredientHelper, IIngredientRenderer<V> ingredientRenderer) {
		registerIngredientType(new IngredientInfo<>(ingredientType, Collections.emptyList(), ingredientHelper, ingredientRenderer
			//? if >=21.1
			, null
		), ingredients);
	}
	//endregion
	
	//region IExtraIngredientRegistration
	@Override
	public <V> void addExtraIngredients(IIngredientType<V> ingredientType, Collection<V> ingredients) {
		if (!typeInfoMap.containsKey(ingredientType))
			throw new IllegalArgumentException();
		
		registerIngredients(ingredientType, ingredients);
		//noinspection unchecked
        var ingredientInfo = ((IngredientInfo<V>) typeInfoMap.get(ingredientType));
        //? if <21.1 {
        /*ingredientInfo.addIngredients(ingredients);
        *///?} else {
        List<ITypedIngredient<V>> typedIngredientList = new ArrayList<>(ingredients.size());
        for (V ingredient : ingredients) {
            var typed = TypedIngredient.createAndFilterInvalid(this, ingredientType, ingredient, false);
            if (typed != null) {
                typedIngredientList.add(typed);
            } else {
                LOGGER.warn("Attempting to add invalid ingredient {}", ingredientInfo.getIngredientHelper().getErrorInfo(ingredient));
            }
        }
        ingredientInfo.addIngredients(typedIngredientList);
        //?}
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

    private <V> void registerIngredients(IIngredientType<V> ingredientType, Collection<V> ingredients) {
		if (locked)
			throw new IllegalStateException("Tried to add ingredients after registry is locked");
		
		if (itemStacks != null && ingredientType == VanillaTypes.ITEM_STACK)
			//noinspection unchecked
			itemStacks.addAll((Collection<ItemStack>) ingredients);
		else if (fluidStacks != null && ingredientType == fluidHelper.getFluidIngredientType())
			fluidStacks.addAll(ingredients);
		
		for (final var ingredient : ingredients) {
			final var emiStack = getEMIStack(ingredientType, ingredient);
			//noinspection unchecked
			final var typedIngredient = (ITypedIngredient<V>) emiStack;
			uidLookup.put(getUid(ingredientType, ingredient), typedIngredient);
			//? if >=21.1
			uidLookup.put(getLegacyUid(ingredientType, ingredient), typedIngredient);
			
			if (!emiStack.isEmpty())
				runtime.emiRegistry.addEmiStack(emiStack);
		}
	}
	
	private <V> void registerIngredientType(IngredientInfo<V> ingredientInfo, Collection<V> ingredients) {
		if (locked)
			throw new IllegalStateException("Tried to add ingredient type after registry is locked");
		
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
	
	//? if >=21.1
	@SuppressWarnings("removal")
	private void registerItemStackDefaultComparison() {
		for (final var item : EmiPort.getItemRegistry()) {
			if (runtime.subtypeManager.hasSubtypes(VanillaTypes.ITEM_STACK, item.getDefaultInstance())) {
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
			if (runtime.subtypeManager.hasSubtypes(type, fluidHelper.create(fluid.builtInRegistryHolder()/*? if <21.1 {*//*.value()*//*?}*/, 1000L))) {
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
		final var jeiIngredientTypes = Lists.newArrayList(getRegisteredIngredientTypes());
		for (final var _jeiIngredientType : jeiIngredientTypes) {
			if (_jeiIngredientType == VanillaTypes.ITEM_STACK || _jeiIngredientType == JemiUtil.getFluidType()) {
				continue;
			}
			//noinspection rawtypes
			if (_jeiIngredientType instanceof final IIngredientTypeWithSubtypes jeiIngredientType) {
				final var jeiIngredients = Lists.newArrayList(getAllIngredients(_jeiIngredientType));
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
		
		if (!removedStacks.isEmpty()) {
			runtime.emiRegistry.removeEmiStacks(removedStacks::contains);
		}
		
		registerItemStackDefaultComparison();
		registerFluidDefaultComparison();
		registerOtherJEIIngredientTypeComparisons();
		
		removedStacks.clear();
		
		itemStacks = null;
		fluidStacks = null;
	}
	
}
