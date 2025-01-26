package dev.nolij.toomanyrecipeviewers;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSetMultimap;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.recipe.EmiPatternCraftingRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.GeneratedSlotWidget;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.jemi.JemiCategory;
import dev.emi.emi.jemi.JemiRecipe;
import dev.emi.emi.jemi.JemiUtil;
import dev.emi.emi.recipe.EmiBrewingRecipe;
import dev.emi.emi.recipe.EmiCompostingRecipe;
import dev.emi.emi.recipe.EmiCookingRecipe;
import dev.emi.emi.recipe.EmiFuelRecipe;
import dev.emi.emi.recipe.EmiSmithingRecipe;
import dev.emi.emi.recipe.EmiStonecuttingRecipe;
import dev.nolij.toomanyrecipeviewers.impl.api.recipe.RecipeManager;
import dev.nolij.toomanyrecipeviewers.impl.api.runtime.JEIKeyMappings;
import dev.nolij.toomanyrecipeviewers.impl.api.runtime.JEIRuntime;
import dev.nolij.toomanyrecipeviewers.impl.api.runtime.config.JEIConfigManager;
import mezz.jei.api.helpers.ICodecHelper;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferManager;
import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IJeiCompostingRecipe;
import mezz.jei.api.recipe.vanilla.IJeiFuelingRecipe;
import mezz.jei.api.recipe.vanilla.IJeiIngredientInfoRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.runtime.IBookmarkOverlay;
import mezz.jei.api.runtime.IEditModeConfig;
import mezz.jei.api.runtime.IIngredientFilter;
import mezz.jei.api.runtime.IIngredientListOverlay;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.api.runtime.IJeiKeyMappings;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.api.runtime.config.IJeiConfigManager;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.library.config.EditModeConfig;
import mezz.jei.library.gui.helpers.GuiHelper;
import mezz.jei.library.ingredients.IngredientBlacklistInternal;
import mezz.jei.library.ingredients.subtypes.SubtypeManager;
import mezz.jei.library.plugins.vanilla.anvil.SmithingRecipeCategory;
import mezz.jei.library.plugins.vanilla.crafting.CraftingRecipeCategory;
import mezz.jei.library.runtime.JeiHelpers;
import mezz.jei.neoforge.platform.FluidHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public final class TooManyRecipeViewers {
	
	public static volatile TooManyRecipeViewers runtime = null;
	
	//region Storage
	public volatile EmiRegistry emiRegistry = null;
	public volatile SubtypeManager subtypeManager = null;
	public volatile IStackHelper stackHelper = null;
	public volatile IColorHelper colorHelper = null;
	public volatile IIngredientManager ingredientManager = null;
	public volatile GuiHelper guiHelper = null;
	public volatile IFocusFactory focusFactory = null;
	public volatile ICodecHelper codecHelper = null;
	public volatile IVanillaRecipeFactory vanillaRecipeFactory = null;
	public volatile IngredientBlacklistInternal blacklist = null;
	public volatile IClientToggleState clientToggleState = null;
	public volatile EditModeConfig editModeConfig = null;
	public volatile IIngredientVisibility ingredientVisibility = null;
	public volatile ImmutableSetMultimap<String, String> modAliases = null;
	public volatile IModIdHelper modIdHelper = null;
	public volatile JeiHelpers jeiHelpers = null;
	public volatile CraftingRecipeCategory craftingCategory = null;
	public volatile SmithingRecipeCategory smithingCategory = null;
	public volatile @Unmodifiable List<IRecipeCategory<?>> recipeCategories = null;
	public volatile ImmutableListMultimap<RecipeType<?>, ITypedIngredient<?>> recipeCatalysts = null;
	public volatile RecipeManager recipeManager = null;
	public volatile IRecipeTransferManager recipeTransferManager = null;
	public volatile IScreenHelper screenHelper = null;
	public volatile ResourceManagerReloadListener resourceReloadHandler = null;
	public volatile JEIRuntime jeiRuntime = null;
	//endregion
	
	//region Interop
	//region RecipeCategory
	private final Map<RecipeType<?>, RecipeCategory<?>> jeiRecipeTypeMap = Collections.synchronizedMap(new HashMap<>());
	private final Map<EmiRecipeCategory, RecipeCategory<?>> emiCategoryMap = Collections.synchronizedMap(new HashMap<>());
	
	public <T> RecipeCategory<T> recipeCategory(
		@Nullable IRecipeCategory<T> jeiCategory,
		@Nullable RecipeType<T> jeiRecipeType,
		@Nullable EmiRecipeCategory emiCategory
	) {
		if (recipeCategories == null)
			throw new IllegalStateException();
		
		if (jeiCategory == null && jeiRecipeType == null && emiCategory == null)
			throw new IllegalArgumentException();
		
		if (jeiCategory != null && jeiRecipeType == null)
			jeiRecipeType = jeiCategory.getRecipeType();
		
		final RecipeCategory<T> result;
		if (jeiRecipeType != null && jeiRecipeTypeMap.containsKey(jeiRecipeType)) {
			//noinspection unchecked
			result = (RecipeCategory<T>) jeiRecipeTypeMap.get(jeiRecipeType);
		} else if (emiCategory != null && emiCategoryMap.containsKey(emiCategory)) {
			//noinspection unchecked
			result = (RecipeCategory<T>) emiCategoryMap.get(emiCategory);
		} else {
			result = new RecipeCategory<T>();
		}
		
		if (result.jeiCategory == null && jeiCategory != null)
			result.jeiCategory = jeiCategory;
		if (result.jeiRecipeType == null && jeiRecipeType != null)
			result.jeiRecipeType = jeiRecipeType;
		if (result.emiCategory == null && emiCategory != null)
			result.emiCategory = emiCategory;
		
		if (jeiRecipeType != null)
			jeiRecipeTypeMap.put(jeiRecipeType, result);
		if (emiCategory != null)
			emiCategoryMap.put(emiCategory, result);
		
		return result;
	}
	
	public <T> RecipeCategory<T> recipeCategory(IRecipeCategory<T> jeiCategory) {
		return recipeCategory(jeiCategory, null, null);
	}
	public <T> RecipeCategory<T> recipeCategory(RecipeType<T> jeiRecipeType) {
		return recipeCategory(null, jeiRecipeType, null);
	}
	public <T> RecipeCategory<T> recipeCategory(EmiRecipeCategory emiCategory) {
		return recipeCategory(null, null, emiCategory);
	}
	
	public class RecipeCategory<T> {
		
		private @Nullable IRecipeCategory<T> jeiCategory;
		private @Nullable RecipeType<T> jeiRecipeType;
		private @Nullable EmiRecipeCategory emiCategory;
		
		private RecipeCategory() {}
		
		public synchronized @Nullable IRecipeCategory<T> getJEICategory() {
			if (jeiCategory == null) {
				if (jeiRecipeType != null || emiCategory == null)
					throw new IllegalStateException();
				
				return null;
			}
			
			return jeiCategory;
		}
		
		public synchronized @Nullable RecipeType<T> getJEIRecipeType() {
			if (jeiRecipeType == null) {
				if (jeiCategory != null) {
					jeiRecipeType = jeiCategory.getRecipeType();
					jeiRecipeTypeMap.put(jeiRecipeType, this);
				} else if (emiCategory == null) {
					throw new IllegalStateException();
				} else {
					return null;
				}
			}
			
			return jeiRecipeType;
		}
		
		public synchronized @NotNull EmiRecipeCategory getEMICategory() {
			if (emiCategory == null) {
				if (jeiCategory == null || jeiRecipeType == null)
					throw new IllegalStateException();
				
				if (RecipeManager.vanillaJEITypeEMICategoryMap.containsKey(jeiRecipeType)) {
					emiCategory = RecipeManager.vanillaJEITypeEMICategoryMap.get(jeiRecipeType);
				} else {
					emiCategory = new JemiCategory(jeiCategory);
				}
				emiCategoryMap.put(emiCategory, this);
			}
			
			return Objects.requireNonNull(emiCategory);
		}
		
	}
	//endregion
	//region Recipe
	private final Map<Object, Recipe<?>> jeiRecipeMap = Collections.synchronizedMap(new HashMap<>());
	private final Map<EmiRecipe, Recipe<?>> emiRecipeMap = Collections.synchronizedMap(new HashMap<>());
	
	public <T> Recipe<T> recipe(@NotNull RecipeCategory<T> recipeCategory, @Nullable T jeiRecipe, @Nullable EmiRecipe emiRecipe) {
		if (recipeManager == null)
			throw new IllegalStateException();
		
		//noinspection ConstantValue
		if (recipeCategory == null || (jeiRecipe == null && emiRecipe == null))
			throw new IllegalArgumentException();
		
		final Recipe<T> result;
		if (jeiRecipe != null && jeiRecipeMap.containsKey(jeiRecipe)) {
			//noinspection unchecked
			result = (Recipe<T>) jeiRecipeMap.get(jeiRecipe);
		} else if (emiRecipe != null && emiRecipeMap.containsKey(emiRecipe)) {
			//noinspection unchecked
			result = (Recipe<T>) emiRecipeMap.get(emiRecipe);
		} else {
			result = new Recipe<T>(recipeCategory);
		}
		
		if (result.jeiRecipe == null && jeiRecipe != null)
			result.jeiRecipe = jeiRecipe;
		if (result.emiRecipe == null && emiRecipe != null)
			result.emiRecipe = emiRecipe;
		
		if (jeiRecipe != null)
			jeiRecipeMap.put(jeiRecipe, result);
		if (emiRecipe != null)
			emiRecipeMap.put(emiRecipe, result);
		
		return result;
	}
	
	public Recipe<?> recipe(@NotNull RecipeCategory<?> recipeCategory, @Nullable Object recipe) {
		if (recipe instanceof EmiRecipe)
			return recipe((RecipeCategory<?>) recipeCategory, null, (EmiRecipe) recipe);
		//noinspection unchecked,rawtypes
		return recipe((RecipeCategory) recipeCategory, recipe, null);
	}
	
	public class Recipe<T> {
		
		private final @NotNull RecipeCategory<T> recipeCategory;
		
		private @Nullable T jeiRecipe;
		private @Nullable EmiRecipe emiRecipe;
		
		private @Nullable ResourceLocation originalId = null;
		
		private Recipe(@NotNull RecipeCategory<T> recipeCategory) {
			this.recipeCategory = recipeCategory;
		}
		
		public @Nullable ResourceLocation getOriginalID() {
			return originalId;
		}
		
		public synchronized @Nullable T getJEIRecipe() {
			if (jeiRecipe == null) {
				if (emiRecipe == null)
					throw new IllegalStateException();
				
//				//noinspection unchecked
//				jeiRecipe = (T) emiRecipe.getBackingRecipe();
				return null;
			}
			
			return jeiRecipe;
		}
		
		public synchronized @NotNull EmiRecipe getEMIRecipe() {
			if (emiRecipe == null) {
				if (jeiRecipe == null)
					throw new IllegalStateException();
				
				final var jeiCategory = Objects.requireNonNull(recipeCategory.getJEICategory());
				final var jeiRecipeType = Objects.requireNonNull(recipeCategory.getJEIRecipeType());
				final var emiCategory = recipeCategory.getEMICategory();
				final var jemiRecipe = new JemiRecipe<T>(emiCategory, jeiCategory, jeiRecipe);
				if (jemiRecipe.originalId != null) {
					originalId = jemiRecipe.originalId;
				} else {
					final var typeId = jeiRecipeType.getUid();
					jemiRecipe.id = typeId.withSuffix("/%x".formatted(Objects.hash(jemiRecipe.inputs, jemiRecipe.outputs, jemiRecipe.catalysts)));
				}
				if (RecipeManager.vanillaJEITypeEMICategoryMap.containsKey(jeiRecipeType)) {
					if (emiCategory == VanillaEmiRecipeCategories.INFO) {
						emiRecipe = convertEMIInfoRecipe((IJeiIngredientInfoRecipe) jeiRecipe);
					} else if (emiCategory == VanillaEmiRecipeCategories.CRAFTING) {
						//noinspection unchecked
						emiRecipe = convertEMICraftingRecipe((JemiRecipe<RecipeHolder<CraftingRecipe>>) jemiRecipe);
					} else if (emiCategory == VanillaEmiRecipeCategories.SMELTING) {
						//noinspection unchecked
						emiRecipe = convertEMISmeltingRecipe((JemiRecipe<RecipeHolder<SmeltingRecipe>>) jemiRecipe);
					} else if (emiCategory == VanillaEmiRecipeCategories.BLASTING) {
						//noinspection unchecked
						emiRecipe = convertEMIBlastingRecipe((JemiRecipe<RecipeHolder<BlastingRecipe>>) jemiRecipe);
					} else if (emiCategory == VanillaEmiRecipeCategories.SMOKING) {
						//noinspection unchecked
						emiRecipe = convertEMISmokingRecipe((JemiRecipe<RecipeHolder<SmokingRecipe>>) jemiRecipe);
					} else if (emiCategory == VanillaEmiRecipeCategories.CAMPFIRE_COOKING) {
						//noinspection unchecked
						emiRecipe = convertEMICampfireRecipe((JemiRecipe<RecipeHolder<CampfireCookingRecipe>>) jemiRecipe);
					} else if (emiCategory == VanillaEmiRecipeCategories.STONECUTTING) {
						//noinspection unchecked
						emiRecipe = convertEMIStonecuttingRecipe((JemiRecipe<RecipeHolder<StonecutterRecipe>>) jemiRecipe);
					} else if (emiCategory == VanillaEmiRecipeCategories.SMITHING) {
						//noinspection unchecked
						emiRecipe = convertEMISmithingRecipe((JemiRecipe<RecipeHolder<SmithingRecipe>>) jemiRecipe);
					} else if (emiCategory == VanillaEmiRecipeCategories.ANVIL_REPAIRING) {
						//noinspection unchecked
						emiRecipe = convertEMIAnvilRecipe((JemiRecipe<IJeiAnvilRecipe>) jemiRecipe);
					} else if (emiCategory == VanillaEmiRecipeCategories.BREWING) {
						//noinspection unchecked
						emiRecipe = convertEMIBrewingRecipe((JemiRecipe<IJeiBrewingRecipe>) jemiRecipe);
					} else if (emiCategory == VanillaEmiRecipeCategories.FUEL) {
						//noinspection unchecked
						emiRecipe = convertEMIFuelRecipe((JemiRecipe<IJeiFuelingRecipe>) jemiRecipe);
					} else if (emiCategory == VanillaEmiRecipeCategories.COMPOSTING) {
						//noinspection unchecked
						emiRecipe = convertEMICompostingRecipe((JemiRecipe<IJeiCompostingRecipe>) jemiRecipe);
					} else {
						emiRecipe = jemiRecipe;
					}
				} else {
					emiRecipe = jemiRecipe;
				}
				
				emiRecipeMap.put(emiRecipe, this);
			}
			
			return emiRecipe;
		}
		
		private static @NotNull EmiInfoRecipe convertEMIInfoRecipe(IJeiIngredientInfoRecipe jeiRecipe) {
			final var emiIngredients = jeiRecipe
				.getIngredients()
				.stream()
				.map(JemiUtil::getStack)
				.map(EmiIngredient.class::cast)
				.toList();
			
			final var lines = jeiRecipe
				.getDescription()
				.stream()
				.map(formattedText -> {
					if (formattedText instanceof Component component)
						return component;
					
					var result = Component.literal("");
					
					formattedText.visit((style, string) -> {
						result.append(Component.literal(string).withStyle(style));
						
						return Optional.empty();
					}, Style.EMPTY);
					
					return result;
				})
				.toList();
			
			return new EmiInfoRecipe(emiIngredients, lines, null);
		}
		
		private static @NotNull EmiCraftingRecipe convertEMICraftingRecipe(JemiRecipe<RecipeHolder<CraftingRecipe>> jemiRecipe) {
			if (jemiRecipe.outputs.size() == 1) {
				return new EmiCraftingRecipe(jemiRecipe.inputs, jemiRecipe.outputs.getFirst(), jemiRecipe.id, jemiRecipe.builder.shapeless);
			} else {
				return new EmiPatternCraftingRecipe(jemiRecipe.inputs, EmiStack.EMPTY, jemiRecipe.id, jemiRecipe.builder.shapeless) {
					// TODO: supportsRecipeTree?
					
					@Override
					public List<EmiStack> getOutputs() {
						return jemiRecipe.outputs;
					}
					
					@Override
					public SlotWidget getInputWidget(int slot, int x, int y) {
						return new SlotWidget(slot <= jemiRecipe.inputs.size() ? jemiRecipe.inputs.get(slot) : EmiStack.EMPTY, x, y);
					}
					
					@Override
					public SlotWidget getOutputWidget(int x, int y) {
						return new GeneratedSlotWidget(r -> jemiRecipe.outputs.get(r.nextInt(jemiRecipe.outputs.size())), jemiRecipe.hashCode(), x, y);
					}
				};
			}
		}
		
		private static @NotNull EmiCookingRecipe convertEMISmeltingRecipe(JemiRecipe<RecipeHolder<SmeltingRecipe>> jemiRecipe) {
			return new EmiCookingRecipe(jemiRecipe.recipe.value(), VanillaEmiRecipeCategories.SMELTING, 1, false);
		}
		
		private static @NotNull EmiCookingRecipe convertEMIBlastingRecipe(JemiRecipe<RecipeHolder<BlastingRecipe>> jemiRecipe) {
			return new EmiCookingRecipe(jemiRecipe.recipe.value(), VanillaEmiRecipeCategories.BLASTING, 2, false);
		}
		
		private static @NotNull EmiCookingRecipe convertEMISmokingRecipe(JemiRecipe<RecipeHolder<SmokingRecipe>> jemiRecipe) {
			return new EmiCookingRecipe(jemiRecipe.recipe.value(), VanillaEmiRecipeCategories.SMOKING, 2, false);
		}
		
		private static @NotNull EmiCookingRecipe convertEMICampfireRecipe(JemiRecipe<RecipeHolder<CampfireCookingRecipe>> jemiRecipe) {
			return new EmiCookingRecipe(jemiRecipe.recipe.value(), VanillaEmiRecipeCategories.CAMPFIRE_COOKING, 1, true);
		}
		
		private static @NotNull EmiStonecuttingRecipe convertEMIStonecuttingRecipe(JemiRecipe<RecipeHolder<StonecutterRecipe>> jemiRecipe) {
			return new EmiStonecuttingRecipe(jemiRecipe.recipe.value());
		}
		
		private static @NotNull EmiSmithingRecipe convertEMISmithingRecipe(JemiRecipe<RecipeHolder<SmithingRecipe>> jemiRecipe) {
			// TODO: smithing trim recipes?
			// TODO: IExtendableSmithingRecipeCategory?
			return new EmiSmithingRecipe(jemiRecipe.inputs.get(0), jemiRecipe.inputs.get(1), jemiRecipe.inputs.get(2), jemiRecipe.outputs.getFirst(), jemiRecipe.id);
		}
		
		private static @NotNull EmiRecipe convertEMIAnvilRecipe(JemiRecipe<IJeiAnvilRecipe> jemiRecipe) {
			final var leftInputs = jemiRecipe.recipe.getLeftInputs().stream().map(JemiUtil::getStack).toList();
			final var rightInputs = jemiRecipe.recipe.getRightInputs().stream().map(JemiUtil::getStack).toList();
			final var outputs = jemiRecipe.recipe.getOutputs().stream().map(JemiUtil::getStack).toList();
			return new EmiRecipe() {
				private final int uniq = EmiUtil.RANDOM.nextInt();
				
				@Override
				public EmiRecipeCategory getCategory() {
					return VanillaEmiRecipeCategories.ANVIL_REPAIRING;
				}
				
				@Override
				public @Nullable ResourceLocation getId() {
					return jemiRecipe.id;
				}
				
				@Override
				public List<EmiIngredient> getInputs() {
					return Stream.concat(leftInputs.stream().map(EmiIngredient.class::cast), rightInputs.stream()).toList();
				}
				
				@Override
				public List<EmiStack> getOutputs() {
					return outputs;
				}
				
				@Override
				public boolean supportsRecipeTree() {
					return false;
				}
				
				@Override
				public int getDisplayWidth() {
					return 125;
				}
				
				@Override
				public int getDisplayHeight() {
					return 18;
				}
				
				@Override
				public void addWidgets(WidgetHolder widgets) {
					widgets.addTexture(EmiTexture.PLUS, 27, 3);
					widgets.addTexture(EmiTexture.EMPTY_ARROW, 75, 1);
					if (leftInputs.size() == 1) widgets.addSlot(leftInputs.getFirst(), 0, 0);
					else widgets.addGeneratedSlot(r -> leftInputs.get(r.nextInt(leftInputs.size())), uniq, 0, 0);
					if (rightInputs.size() == 1) widgets.addSlot(rightInputs.getFirst(), 49, 0);
					else widgets.addGeneratedSlot(r -> rightInputs.get(r.nextInt(rightInputs.size())), uniq, 49, 0);
					if (outputs.size() == 1) widgets.addSlot(outputs.getFirst(), 107, 0);
					else widgets.addGeneratedSlot(r -> outputs.get(r.nextInt(outputs.size())), uniq, 107, 0).recipeContext(this);
				}
			};
		}
		
		private static @NotNull EmiBrewingRecipe convertEMIBrewingRecipe(JemiRecipe<IJeiBrewingRecipe> jemiRecipe) {
			return new EmiBrewingRecipe(jemiRecipe.inputs.getFirst().getEmiStacks().getFirst(), jemiRecipe.inputs.getLast(), jemiRecipe.outputs.getFirst(), jemiRecipe.id);
		}
		
		private static @NotNull EmiFuelRecipe convertEMIFuelRecipe(JemiRecipe<IJeiFuelingRecipe> jemiRecipe) {
			return new EmiFuelRecipe(jemiRecipe.getInputs().getFirst(), jemiRecipe.recipe.getBurnTime(), jemiRecipe.id);
		}
		
		private static @NotNull EmiCompostingRecipe convertEMICompostingRecipe(JemiRecipe<IJeiCompostingRecipe> jemiRecipe) {
			return new EmiCompostingRecipe(jemiRecipe.inputs.getFirst().getEmiStacks().getFirst(), jemiRecipe.recipe.getChance(), jemiRecipe.id);
		}
		
	}
	//endregion
	//endregion
	
	//region Static Storage
	public static final JEIConfigManager jeiConfigManager = new JEIConfigManager();
	public static final IPlatformFluidHelper<?> fluidHelper = new FluidHelper();
	public static final IInternalKeyMappings jeiKeyMappings = new JEIKeyMappings();
	
	public static final IJeiHelpers staticJEIHelpers = new IJeiHelpers() {
		@Override
		public IGuiHelper getGuiHelper() {
			return runtime.guiHelper;
		}
		
		@Override
		public IStackHelper getStackHelper() {
			return runtime.stackHelper;
		}
		
		@Override
		public IModIdHelper getModIdHelper() {
			return runtime.modIdHelper;
		}
		
		@Override
		public IFocusFactory getFocusFactory() {
			return runtime.focusFactory;
		}
		
		@Override
		public IColorHelper getColorHelper() {
			return runtime.colorHelper;
		}
		
		@Override
		public IPlatformFluidHelper<?> getPlatformFluidHelper() {
			return fluidHelper;
		}
		
		@Override
		public <T> Optional<RecipeType<T>> getRecipeType(ResourceLocation recipeUid, Class<? extends T> recipeClass) {
			if (runtime == null || runtime.recipeManager == null)
				return Optional.empty();
			
			return runtime.recipeManager.getRecipeType(recipeUid, recipeClass);
		}
		
		@Override
		public Optional<RecipeType<?>> getRecipeType(ResourceLocation recipeUid) {
			if (runtime == null || runtime.recipeManager == null)
				return Optional.empty();
			
			return runtime.recipeManager.getRecipeType(recipeUid);
		}
		
		@Override
		public Stream<RecipeType<?>> getAllRecipeTypes() {
			if (runtime == null || runtime.recipeManager == null)
				return Stream.empty();
			
			return runtime.recipeManager.getAllRecipeTypes();
		}
		
		@Override
		public IIngredientManager getIngredientManager() {
			return runtime.ingredientManager;
		}
		
		@Override
		public ICodecHelper getCodecHelper() {
			return runtime.codecHelper;
		}
		
		@Override
		public IVanillaRecipeFactory getVanillaRecipeFactory() {
			return runtime.vanillaRecipeFactory;
		}
		
		@Override
		public IIngredientVisibility getIngredientVisibility() {
			return runtime.ingredientVisibility;
		}
	};
	
	public static final IJeiRuntime staticJEIRuntime = new IJeiRuntime() {
		@Override
		public IRecipeManager getRecipeManager() {
			return runtime.recipeManager;
		}
		
		@Override
		public IRecipesGui getRecipesGui() {
			return runtime.jeiRuntime.getRecipesGui();
		}
		
		@Override
		public IIngredientFilter getIngredientFilter() {
			return runtime.jeiRuntime.getIngredientFilter();
		}
		
		@Override
		public IIngredientListOverlay getIngredientListOverlay() {
			return runtime.jeiRuntime.getIngredientListOverlay();
		}
		
		@Override
		public IBookmarkOverlay getBookmarkOverlay() {
			return runtime.jeiRuntime.getBookmarkOverlay();
		}
		
		@Override
		public IJeiHelpers getJeiHelpers() {
			return staticJEIHelpers;
		}
		
		@Override
		public IIngredientManager getIngredientManager() {
			return runtime.ingredientManager;
		}
		
		@Override
		public IJeiKeyMappings getKeyMappings() {
			return jeiKeyMappings;
		}
		
		@Override
		public IScreenHelper getScreenHelper() {
			return runtime.screenHelper;
		}
		
		@Override
		public IRecipeTransferManager getRecipeTransferManager() {
			return runtime.recipeTransferManager;
		}
		
		@Override
		public IEditModeConfig getEditModeConfig() {
			return runtime.editModeConfig;
		}
		
		@Override
		public IJeiConfigManager getConfigManager() {
			return jeiConfigManager;
		}
	};
	//endregion
	
}
