package dev.nolij.toomanyrecipeviewers.impl.jei.api.gui.builder;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.nolij.toomanyrecipeviewers.impl.jei.api.gui.OffsetDrawable;
import dev.nolij.toomanyrecipeviewers.impl.jei.api.gui.ingredient.ITMRVSlotWidget;
import dev.nolij.toomanyrecipeviewers.impl.jei.api.gui.ingredient.TMRVSlotWidget;
import dev.nolij.toomanyrecipeviewers.impl.jei.api.gui.ingredient.TMRVTankWidget;
import dev.nolij.toomanyrecipeviewers.impl.jei.api.runtime.IngredientManager;
import dev.nolij.toomanyrecipeviewers.util.FluidRendererParameters;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.library.gui.recipes.layout.builder.LegacyTooltipCallbackAdapter;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static dev.nolij.toomanyrecipeviewers.TooManyRecipeViewers.fluidHelper;

@SuppressWarnings("NonExtendableApiUsage")
public class RecipeSlotBuilder implements IRecipeSlotBuilder {
	
	private final IngredientManager ingredientManager;
	
	public final RecipeIngredientRole role;
	private final TMRVIngredientCollector ingredientCollector;
	
	private boolean visible = true;
	private @Nullable String name;
	private final List<IRecipeSlotRichTooltipCallback> tooltipCallbacks = new ArrayList<>();
	private boolean outputSlotBackground = false;
	private @Nullable OffsetDrawable background = null;
	private @Nullable OffsetDrawable overlay = null;
	private final Map<IIngredientType<?>, IIngredientRenderer<?>> rendererOverrides = new HashMap<>();
	private @Nullable FluidRendererParameters fluidRendererParameters = null;
	private ImmutableRect2i rect = new ImmutableRect2i(0, 0, 16, 16);
	
	RecipeSlotBuilder(IngredientManager ingredientManager, RecipeIngredientRole role) {
		this.ingredientManager = ingredientManager;
		this.role = role;
		this.ingredientCollector = new TMRVIngredientCollector(ingredientManager);
	}
	
	public boolean isVisible() {
		return visible;
	}
	
	public List<ITypedIngredient<?>> getCollectedIngredients() {
		return ingredientCollector.getCollectedIngredients();
	}
	
	public List<EmiStack> getEMIStacks() {
		return ingredientCollector.getEMIStacks();
	}
	
	public EmiIngredient getEMIIngredient() {
		return ingredientCollector.getEMIIngredient();
	}
	
	private @NotNull ITMRVSlotWidget getWidget() {
		final ITMRVSlotWidget widget;
		
		if (fluidRendererParameters == null) {
			widget = new TMRVSlotWidget(ingredientManager, role, rect, rendererOverrides);
		} else {
			for (final var ingredient : getCollectedIngredients()) {
				if (ingredient.getType() != fluidHelper.getFluidIngredientType())
					throw new IllegalStateException("Mixed fluids and non-fluids");
			}
			if (!rendererOverrides.isEmpty())
				throw new IllegalStateException("Renderer override on fluid slot");
			
			widget = new TMRVTankWidget(ingredientManager, role, fluidRendererParameters, rect);
		}
		
		return widget;
	}
	
	public SlotWidget build() {
		if (!visible)
			throw new UnsupportedOperationException();
		
		final ITMRVSlotWidget widget = getWidget();
		
		widget.drawBack(false);
		widget.large(outputSlotBackground);
		widget.setName(name);
		widget.addTooltipCallbacks(tooltipCallbacks);
		widget.setBackground(background);
		widget.setOverlay(overlay);
		widget.getIngredientCollector().copy(ingredientCollector);
		
		return (SlotWidget) widget;
	}
	
	public RecipeSlotBuilder setInvisible() {
		visible = false;
		return this;
	}
	
	//region IRecipeSlotBuilder
	@Override
	public IRecipeSlotBuilder addRichTooltipCallback(IRecipeSlotRichTooltipCallback tooltipCallback) {
		this.tooltipCallbacks.add(tooltipCallback);
		return this;
	}
	
	@SuppressWarnings("removal")
	@Override
	public IRecipeSlotBuilder addTooltipCallback(mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback tooltipCallback) {
		return addRichTooltipCallback(new LegacyTooltipCallbackAdapter(tooltipCallback));
	}
	
	@Override
	public IRecipeSlotBuilder setSlotName(String name) {
		this.name = name;
		return this;
	}
	
	@Override
	public IRecipeSlotBuilder setStandardSlotBackground() {
		outputSlotBackground = false;
		return this;
	}
	
	@Override
	public IRecipeSlotBuilder setOutputSlotBackground() {
		outputSlotBackground = true;
		return this;
	}
	
	@Override
	public IRecipeSlotBuilder setBackground(IDrawable background, int xOffset, int yOffset) {
		this.background = new OffsetDrawable(background, xOffset, yOffset);
		return this;
	}
	
	@Override
	public IRecipeSlotBuilder setOverlay(IDrawable overlay, int xOffset, int yOffset) {
		this.overlay = new OffsetDrawable(overlay, xOffset, yOffset);
		return this;
	}
	
	@Override
	public <T> IRecipeSlotBuilder setCustomRenderer(IIngredientType<T> type, IIngredientRenderer<T> renderer) {
		synchronized (rendererOverrides) {
			if (rendererOverrides.isEmpty()) {
				rect = new ImmutableRect2i(rect.x(), rect.y(), renderer.getWidth(), renderer.getHeight());
			} else if (
				renderer.getWidth() != rect.width() || 
				renderer.getHeight() != rect.height()) {
				throw new IllegalStateException("Size mismatch");
			}
			
			this.rendererOverrides.put(type, renderer);
			
			return this;
		}
	}
	
	@Override
	public IRecipeSlotBuilder setFluidRenderer(long capacity, boolean showCapacity, int width, int height) {
		fluidRendererParameters = new FluidRendererParameters(capacity, showCapacity, width, height);
		rect = new ImmutableRect2i(rect.x(), rect.y(), width, height);
		return this;
	}
	//endregion
	
	//region IPlaceable
	@Override
	public IRecipeSlotBuilder setPosition(int x, int y) {
		this.rect = this.rect.setPosition(x, y);
		return this;
	}
	
	@Override
	public int getWidth() {
		return this.rect.width();
	}
	
	@Override
	public int getHeight() {
		return this.rect.height();
	}
	//endregion
	
	//region IIngredientAcceptor
	@Override
	public <I> IRecipeSlotBuilder addIngredients(IIngredientType<I> type, List<@Nullable I> ingredients) {
		ingredientCollector.addIngredients(type, ingredients);
		return this;
	}
	
	@Override
	public <I> IRecipeSlotBuilder addIngredient(IIngredientType<I> type, I ingredient) {
		ingredientCollector.addIngredient(type, ingredient);
		return this;
	}
	
	@Override
	public IRecipeSlotBuilder addIngredientsUnsafe(List<?> ingredients) {
		ingredientCollector.addIngredientsUnsafe(ingredients);
		return this;
	}
	
	@Override
	public IRecipeSlotBuilder addTypedIngredients(List<ITypedIngredient<?>> ingredients) {
		ingredientCollector.addTypedIngredients(ingredients);
		return this;
	}
	
	@Override
	public IRecipeSlotBuilder addOptionalTypedIngredients(List<Optional<ITypedIngredient<?>>> ingredients) {
		ingredientCollector.addOptionalTypedIngredients(ingredients);
		return this;
	}
	
	@Override
	public IRecipeSlotBuilder addFluidStack(Fluid fluid) {
		ingredientCollector.addFluidStack(fluid);
		return this;
	}
	
	@Override
	public IRecipeSlotBuilder addFluidStack(Fluid fluid, long amount) {
		ingredientCollector.addFluidStack(fluid, amount);
		return this;
	}
	
	@Override
	public IRecipeSlotBuilder addFluidStack(Fluid fluid, long amount, DataComponentPatch dataComponentPatch) {
		ingredientCollector.addFluidStack(fluid, amount, dataComponentPatch);
		return this;
	}
	//endregion
	
}
