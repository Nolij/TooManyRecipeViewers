package dev.nolij.toomanyrecipeviewers.impl.jei.api.gui.ingredient;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.TankWidget;
import dev.nolij.toomanyrecipeviewers.impl.jei.api.gui.drawable.OffsetDrawable;
import dev.nolij.toomanyrecipeviewers.impl.jei.api.gui.builder.TMRVIngredientCollector;
import dev.nolij.toomanyrecipeviewers.impl.jei.api.runtime.IngredientManager;
import dev.nolij.toomanyrecipeviewers.util.FluidRendererParameters;
import mezz.jei.api.gui.builder.IIngredientConsumer;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.Rect2i;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class TMRVTankWidget extends TankWidget implements ITMRVRecipeSlotDrawable, ITMRVSlotWidget {
	
	private final IngredientManager ingredientManager;
	private final RecipeIngredientRole role;
	private ImmutableRect2i rect;
	private boolean visible = true;
	
	private final TMRVIngredientCollector ingredientCollector;
	private @Nullable TMRVIngredientCollector overrideIngredientCollector = null;
	
	private @Nullable String name = null;
	
	private @Nullable OffsetDrawable background = null;
	private @Nullable OffsetDrawable overlay = null;
	
	private final List<IRecipeSlotRichTooltipCallback> tooltipCallbacks = new ArrayList<>();
	
	public TMRVTankWidget(IngredientManager ingredientManager, RecipeIngredientRole role, FluidRendererParameters fluidRendererParameters, ImmutableRect2i rect) {
		super(EmiStack.EMPTY, rect.x(), rect.y(), rect.width(), rect.height(), fluidRendererParameters.capacity());
		this.ingredientManager = ingredientManager;
		this.role = role;
		this.rect = rect;
		this.ingredientCollector = new TMRVIngredientCollector(ingredientManager);
	}
	
	private TMRVIngredientCollector getActiveIngredientCollector() {
		return overrideIngredientCollector == null ? ingredientCollector : overrideIngredientCollector;
	}
	
	@Override
	public TMRVIngredientCollector getIngredientCollector() {
		return ingredientCollector;
	}
	
	@Override
	public void setName(@Nullable String name) {
		this.name = name;
	}
	
	@Override
	public void setBackground(@Nullable OffsetDrawable background) {
		this.background = background;
	}
	
	@Override
	public void setOverlay(@Nullable OffsetDrawable overlay) {
		this.overlay = overlay;
	}
	
	@Override
	public void addTooltipCallbacks(List<IRecipeSlotRichTooltipCallback> tooltipCallbacks) {
		this.tooltipCallbacks.addAll(tooltipCallbacks);
	}
	
	@Override
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	//region SlotWidget
	@Override
	public void render(GuiGraphics draw, int mouseX, int mouseY, float delta) {
		if (!visible)
			return;
		
		super.render(draw, mouseX, mouseY, delta);
	}
	
	@Override
	public Bounds getBounds() {
		if (!visible)
			return Bounds.EMPTY;
		
		final var rect = this.rect.expandBy(this.output ? 6 : 1);
		return new Bounds(rect.x(), rect.y(), rect.width(), rect.height());
	}
	
	@Override
	public void drawBackground(GuiGraphics draw, int mouseX, int mouseY, float delta) {
		TMRVSlotWidget.drawJEIBackground(background, draw, rect.x(), rect.y());
		
		super.drawBackground(draw, mouseX, mouseY, delta);
	}
	
	@Override
	public void drawOverlay(GuiGraphics draw, int mouseX, int mouseY, float delta) {
		TMRVSlotWidget.drawJEIOverlay(overlay, draw, rect.x(), rect.y());
		
		super.drawOverlay(draw, mouseX, mouseY, delta);
	}
	
	@Override
	public EmiIngredient getStack() {
		return getActiveIngredientCollector().getEMIIngredient();
	}
	
	@Override
	protected void addSlotTooltip(List<ClientTooltipComponent> list) {
		TMRVSlotWidget.applyTooltipCallbacks(list, tooltipCallbacks, this);
		
		super.addSlotTooltip(list);
	}
	//endregion
	
	//region ITMRVRecipeSlotDrawable
	@Override
	public IIngredientConsumer createDisplayOverrides() {
		if (overrideIngredientCollector == null)
			overrideIngredientCollector = new TMRVIngredientCollector(ingredientManager);
		
		return overrideIngredientCollector;
	}
	
	@Override
	public void clearDisplayOverrides() {
		overrideIngredientCollector = null;
	}
	
	@SuppressWarnings("removal")
	@Override
	public Rect2i getRect() {
		return rect.toMutable();
	}
	
	@Override
	public void setPosition(int x, int y) {
		this.rect = new ImmutableRect2i(x, y, rect.width(), rect.height());
	}
	
	@Override
	public Stream<ITypedIngredient<?>> getAllIngredients() {
		return getActiveIngredientCollector().stream();
	}
	
	//? if >=21.1 {
	@Override
	public @Unmodifiable List<@Nullable ITypedIngredient<?>> getAllIngredientsList() {
		return getActiveIngredientCollector().getCollectedIngredients();
	}
	//?}
	
	@Override
	public Optional<ITypedIngredient<?>> getDisplayedIngredient() {
		return getAllIngredients().findFirst();
	}
	
	@Override
	public RecipeIngredientRole getRole() {
		return this.role;
	}
	
	@Override
	public boolean isEmpty() {
		return getActiveIngredientCollector().isEmpty();
	}
	
	@Override
	public Optional<String> getSlotName() {
		return Optional.ofNullable(name);
	}
	//endregion
	
}
