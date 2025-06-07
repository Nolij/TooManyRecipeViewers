package dev.nolij.toomanyrecipeviewers.impl.jei.api.gui.ingredient;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.emi.emi.api.render.EmiRender;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.jemi.impl.JemiTooltipBuilder;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.nolij.toomanyrecipeviewers.impl.jei.api.gui.drawable.OffsetDrawable;
import dev.nolij.toomanyrecipeviewers.impl.jei.api.gui.builder.TMRVIngredientCollector;
import dev.nolij.toomanyrecipeviewers.impl.jei.api.runtime.IngredientManager;
import dev.nolij.toomanyrecipeviewers.util.OverrideableIngredientCycler;
import mezz.jei.api.gui.builder.IIngredientConsumer;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
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
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static dev.nolij.toomanyrecipeviewers.TooManyRecipeViewersMod.LOGGER;

public class TMRVSlotWidget extends SlotWidget implements ITMRVRecipeSlotDrawable, ITMRVSlotWidget {
	
	static void drawJEIBackground(@Nullable OffsetDrawable background, GuiGraphics draw, int x, int y) {
		if (background == null)
			return;
		
		background.draw(draw, x, y);
	}
	
	static void drawJEIOverlay(@Nullable OffsetDrawable overlay, GuiGraphics draw, int x, int y) {
		if (overlay == null)
			return;
		
		final var context = EmiDrawContext.wrap(draw);
		
		RenderSystem.enableBlend();
		context.push();
		context.matrices().translate(0.0F, 0.0F, 200.0F);
		overlay.draw(context.raw(), x, y);
		context.pop();
	}
	
	static void applyTooltipCallbacks(List<ClientTooltipComponent> list, List<IRecipeSlotRichTooltipCallback> tooltipCallbacks, IRecipeSlotView slotView) {
		final var builder = new JemiTooltipBuilder();
		for (final var tooltipCallback : tooltipCallbacks) {
			try {
				tooltipCallback.onRichTooltip(slotView, builder);
			} catch (Throwable t) {
				LOGGER.error("Error invoking JEI tooltip callback: ", t);
			}
		}
		list.addAll(builder.tooltip);
	}
	
	private final IngredientManager ingredientManager;
	private final RecipeIngredientRole role;
	private ImmutableRect2i rect;
	private boolean visible = true;
	private final Map<IIngredientType<?>, IIngredientRenderer<?>> rendererOverrides;
	
	private final OverrideableIngredientCycler ingredientCycler;
	
	private @Nullable String name = null;
	
	private @Nullable OffsetDrawable background = null;
	private @Nullable OffsetDrawable overlay = null;
	
	private final List<IRecipeSlotRichTooltipCallback> tooltipCallbacks = new ArrayList<>();
	
	public TMRVSlotWidget(IngredientManager ingredientManager, RecipeIngredientRole role, ImmutableRect2i rect, Map<IIngredientType<?>, IIngredientRenderer<?>> rendererOverrides, Consumer<TMRVSlotWidget> updateHook) {
		super(EmiStack.EMPTY, rect.x(), rect.y());
		this.ingredientManager = ingredientManager;
		this.role = role;
		this.rect = rect;
		this.rendererOverrides = rendererOverrides;
		this.ingredientCycler = new OverrideableIngredientCycler(ingredientManager) {
			@Override
			protected void updateHook() {
				updateHook.accept(TMRVSlotWidget.this);
			}
		};
	}
	
	public TMRVSlotWidget(IngredientManager ingredientManager, RecipeIngredientRole role, ImmutableRect2i rect, Map<IIngredientType<?>, IIngredientRenderer<?>> rendererOverrides) {
		this(ingredientManager, role, rect, rendererOverrides, thiz -> {});
	}
	
	public TMRVSlotWidget(IngredientManager ingredientManager, RecipeIngredientRole role, ImmutableRect2i rect, Consumer<TMRVSlotWidget> updateHook) {
		this(ingredientManager, role, rect, Map.of(), updateHook);
	}
	
	public TMRVSlotWidget(IngredientManager ingredientManager, RecipeIngredientRole role, ImmutableRect2i rect) {
		this(ingredientManager, role, rect, Map.of());
	}
	
	@Override
	public TMRVIngredientCollector getIngredientCollector() {
		return ingredientCycler.ingredientCollector;
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
		drawJEIBackground(background, draw, rect.x(), rect.y());
		
		super.drawBackground(draw, mouseX, mouseY, delta);
	}
	
	@Override
	public void drawStack(GuiGraphics draw, int mouseX, int mouseY, float delta) {
		final var optional = getDisplayedIngredient();
		if (optional.isEmpty())
			return;
		
		final var ingredient = optional.get();
		final var type = ingredient.getType();
		
		if (rendererOverrides.containsKey(type)) {
			//noinspection rawtypes
			final var renderer = (IIngredientRenderer) rendererOverrides.get(type);
			final var context = EmiDrawContext.wrap(draw);
			final var bounds = getBounds();
			final var xOff = bounds.x() + (bounds.width() - 16) / 2 + (16 - renderer.getWidth()) / 2;
			final var yOff = bounds.y() + (bounds.height() - 16) / 2 + (16 - renderer.getHeight()) / 2;
			RenderSystem.enableBlend();
			context.push();
			context.matrices().translate((float) xOff, (float) yOff, 0F);
			//noinspection unchecked
			renderer.render(context.raw(), ingredient.getIngredient());
			context.pop();
		} else {
			super.drawStack(draw, mouseX, mouseY, delta);
		}
	}
	
	@Override
	public void drawOverlay(GuiGraphics draw, int mouseX, int mouseY, float delta) {
		drawJEIOverlay(overlay, draw, rect.x(), rect.y());
		
		final var context = EmiDrawContext.wrap(draw);
		
		if (!ingredientCycler.isStatic() && !getStack().isEmpty()) {
			int off = 1;
			
			if (output) {
				off = 5;
			}
			
			EmiRender.renderIngredientIcon(getStack(), context.raw(), x + off, y + off);
		}
		
		super.drawOverlay(context.raw(), mouseX, mouseY, delta);
	}
	
	@Override
	public EmiIngredient getStack() {
		return ingredientManager.getEMIStack(getDisplayedIngredient().orElse(null));
	}
	
	@Override
	protected void addSlotTooltip(List<ClientTooltipComponent> list) {
		applyTooltipCallbacks(list, tooltipCallbacks, this);
		
		super.addSlotTooltip(list);
	}
	//endregion
	
	//region ITMRVRecipeSlotDrawable
	@Override
	public IIngredientConsumer createDisplayOverrides() {
		return ingredientCycler.createDisplayOverrides();
	}
	
	@Override
	public void clearDisplayOverrides() {
		ingredientCycler.clearDisplayOverrides();
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
		return ingredientCycler.ingredientCollector.stream();
	}
	
	//? if >=21.1 {
	@Override
	public @Unmodifiable List<@Nullable ITypedIngredient<?>> getAllIngredientsList() {
		return ingredientCycler.ingredientCollector.getCollectedIngredients();
	}
	//?}
	
	@Override
	public Optional<ITypedIngredient<?>> getDisplayedIngredient() {
		return ingredientCycler.getDisplayedIngredient();
	}
	
	@Override
	public RecipeIngredientRole getRole() {
		return this.role;
	}
	
	@Override
	public boolean isEmpty() {
		return ingredientCycler.ingredientCollector.isEmpty();
	}
	
	@Override
	public Optional<String> getSlotName() {
		return Optional.ofNullable(name);
	}
	//endregion
	
}
