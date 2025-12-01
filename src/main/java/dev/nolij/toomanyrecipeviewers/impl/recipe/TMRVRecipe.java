package dev.nolij.toomanyrecipeviewers.impl.recipe;

import com.mojang.blaze3d.platform.InputConstants;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.jemi.impl.JemiTooltipBuilder;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.EmiScreenManager;
import dev.nolij.toomanyrecipeviewers.TooManyRecipeViewers;
import dev.nolij.toomanyrecipeviewers.impl.jei.api.gui.builder.RecipeLayoutBuilder;
import dev.nolij.toomanyrecipeviewers.impl.widget.DeferredPlaceableWidget;
import dev.nolij.toomanyrecipeviewers.impl.widget.DrawableWidget;
import dev.nolij.toomanyrecipeviewers.impl.jei.api.gui.widgets.ScrollGridWidget;
import dev.nolij.toomanyrecipeviewers.impl.widget.FillingFlameWidget;
import dev.nolij.toomanyrecipeviewers.impl.widget.TextWidget;
import dev.nolij.toomanyrecipeviewers.impl.jei.api.recipe.RecipeManager;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawablesView;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.inputs.IJeiGuiEventListener;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.placement.IPlaceable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.gui.widgets.IScrollBoxWidget;
import mezz.jei.api.gui.widgets.IScrollGridWidget;
import mezz.jei.api.gui.widgets.ISlottedRecipeWidget;
import mezz.jei.api.gui.widgets.ITextWidget;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.gui.input.InputType;
import mezz.jei.gui.input.UserInput;
import mezz.jei.library.focus.FocusGroup;
import mezz.jei.library.gui.widgets.ScrollBoxRecipeWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TMRVRecipe<T> implements EmiRecipe {
	
	private final TooManyRecipeViewers runtime;
	
	private final RecipeManager.Category<T> category;
	private final IRecipeCategory<T> jeiCategory;
	
	private final T jeiRecipe;
	
	public ResourceLocation originalId;
	public ResourceLocation id;
	
	private final List<EmiIngredient> inputs;
	private final List<EmiIngredient> catalysts;
	private final List<EmiStack> outputs;
	private final boolean supportsRecipeTree;
	
	public TMRVRecipe(TooManyRecipeViewers runtime, RecipeManager.Category<T> category, T jeiRecipe, ResourceLocation id) {
		this.runtime = runtime;
		this.category = category;
		this.jeiCategory = Objects.requireNonNull(category.getJEICategory());
		this.jeiRecipe = jeiRecipe;
		
		this.originalId = jeiCategory.getRegistryName(jeiRecipe);
		this.id = id;
		
		final var builder = new RecipeLayoutBuilder(runtime.ingredientManager);
		jeiCategory.setRecipe(builder, jeiRecipe, FocusGroup.EMPTY);
		
		final var recipeData = builder.extractEMIRecipeData();
		inputs = recipeData.inputs();
		catalysts = recipeData.catalysts();
		outputs = recipeData.outputs();
		supportsRecipeTree = recipeData.supportsRecipeTree();
	}
	
	@Override
	public EmiRecipeCategory getCategory() {
		return category.getEMICategory();
	}
	
	@Override
	public @Nullable ResourceLocation getId() {
		return id;
	}
	
	@Override
	public List<EmiIngredient> getInputs() {
		return inputs;
	}
	
	@Override
	public List<EmiIngredient> getCatalysts() {
		return catalysts;
	}
	
	@Override
	public List<EmiStack> getOutputs() {
		return outputs;
	}
	
	@Override
	public int getDisplayWidth() {
		return jeiCategory.getWidth();
	}
	
	@Override
	public int getDisplayHeight() {
		return jeiCategory.getHeight();
	}
	
	@Override
	public boolean supportsRecipeTree() {
		return supportsRecipeTree;
	}
	
	private static class RecipeSlotsView implements IRecipeSlotsView {
		
		private final List<IRecipeSlotView> slots = new ArrayList<>();
		
		@Override
		public @Unmodifiable List<IRecipeSlotView> getSlotViews() {
			return slots;
		}
		
	}
	
	private static class RecipeSlotDrawablesView implements IRecipeSlotDrawablesView {
		
		private final List<IRecipeSlotDrawable> slots = new ArrayList<>();
		
		@Override
		public @Unmodifiable List<IRecipeSlotDrawable> getSlots() {
			return slots;
		}
		
	}
	
	@Override
	public void addWidgets(WidgetHolder widgets) {
		final var builder = new RecipeLayoutBuilder(runtime.ingredientManager);
		jeiCategory.setRecipe(builder, jeiRecipe, FocusGroup.EMPTY);
		
		final var slotsView = new RecipeSlotsView();
		final var slotDrawablesView = new RecipeSlotDrawablesView();
		
		final var rootWidget = widgets.add(new RootWidget(widgets, slotsView, slotDrawablesView));
		
		for (final var slot : builder.getSlots()) {
			if (!slot.isVisible())
				continue;
			
			final var widget = widgets.add(slot.build());
			slotsView.slots.add((IRecipeSlotView) widget);
			slotDrawablesView.slots.add((IRecipeSlotDrawable) widget);
			
			if (slot.role == RecipeIngredientRole.OUTPUT)
				widget.recipeContext(this);
		}
		
		jeiCategory.createRecipeExtras(rootWidget, jeiRecipe, FocusGroup.EMPTY);
	}
	
	private class RootWidget extends Widget implements IRecipeExtrasBuilder {
		
		private final WidgetHolder widgets;
		private final IRecipeSlotsView slotsView;
		private final IRecipeSlotDrawablesView slotDrawablesView;
		private final Bounds bounds;
		
		private final ArrayList<IRecipeWidget> recipeWidgets = new ArrayList<>();
		private final ArrayList<IJeiInputHandler> inputHandlers = new ArrayList<>();
		private final ArrayList<IJeiGuiEventListener> guiEventListeners = new ArrayList<>();
		
		private RootWidget(WidgetHolder widgets, IRecipeSlotsView slotsView, IRecipeSlotDrawablesView slotDrawablesView) {
			this.widgets = widgets;
			this.slotsView = slotsView;
			this.slotDrawablesView = slotDrawablesView;
			this.bounds = new Bounds(0, 0, getDisplayWidth(), getDisplayHeight());
		}
		
		@Override
		public Bounds getBounds() {
			return bounds;
		}
		
		@Override
		public void render(GuiGraphics draw, int mouseX, int mouseY, float delta) {
			final var context = EmiDrawContext.wrap(draw);
			context.push();
			context.matrices().translate(0F, 0F, 0F);
			//noinspection removal
			final var categoryBackground = jeiCategory.getBackground();
			if (categoryBackground != null) {
				categoryBackground.draw(context.raw());
			}
			
			jeiCategory.draw(jeiRecipe, slotsView, context.raw(), mouseX, mouseY);
			
			context.resetColor();
			context.pop();

			for (final var recipeWidget : recipeWidgets) {
				context.push();

				ScreenPosition position = recipeWidget.getPosition();
				context.matrices().translate(position.x(), position.y(), 0F);

				recipeWidget.drawWidget(context.raw(), mouseX - position.x(), mouseY - position.y());

				context.resetColor();
				context.pop();
			}
		}
		
		@Override
		public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
			final var tooltipBuilder = new JemiTooltipBuilder();
			jeiCategory.getTooltip(tooltipBuilder, jeiRecipe, slotsView, mouseX, mouseY);
			for (final var recipeWidget : recipeWidgets) {
				ScreenPosition position = recipeWidget.getPosition();
				recipeWidget.getTooltip(tooltipBuilder, mouseX - position.x(), mouseY - position.y());
			}

			return tooltipBuilder.tooltip;
		}
		
		@Override
		public boolean mouseClicked(int mouseX, int mouseY, int button) {
			return handleInput(mouseX, mouseY, InputConstants.Type.MOUSE.getOrCreate(button), 0);
		}
		
		@Override
		public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
			return handleInput(EmiScreenManager.lastMouseX, EmiScreenManager.lastMouseY, InputConstants.getKey(keyCode, scanCode), modifiers);
		}
		
		private static boolean containsPoint(ScreenRectangle area, int x, int y) {
			return 
				area.top() <= y && y <= area.bottom() &&
				area.left() <= x && x <= area.right();
		}
		
		private boolean handleInput(int mouseX, int mouseY, InputConstants.Key key, int modifiers) {
			final var input = new UserInput(key, mouseX, mouseY, modifiers, InputType.IMMEDIATE);
			
			for (final var inputHandler : inputHandlers) {
				final var area = inputHandler.getArea();
				if (containsPoint(area, mouseX, mouseY)) {
					final var position = area.position();
					if (inputHandler.handleInput(mouseX - position.x(), mouseY - position.y(), input))
						return true;
				}
			}
			for (final var guiEventListener : guiEventListeners) {
				final var area = guiEventListener.getArea();
				if (containsPoint(area, mouseX, mouseY)) {
					final var position = area.position();
					final var x = mouseX - position.x();
					final var y = mouseY - position.y();
					if (switch (key.getType()) {
							case MOUSE -> guiEventListener.mouseReleased(x, y, key.getValue());
							case KEYSYM -> guiEventListener.keyPressed(x, y, key.getValue(), 0, modifiers);
							default -> false;
						})
						return true;
				}
			}
			
			//noinspection removal
			return jeiCategory.handleInput(jeiRecipe, mouseX, mouseY, key);
		}
		
		@Override
		public IRecipeSlotDrawablesView getRecipeSlots() {
			return slotDrawablesView;
		}
		
		@Override
		public void addDrawable(IDrawable drawable, int x, int y) {
			widgets.add(new DrawableWidget(drawable, x, y));
		}
		
		@Override
		public IPlaceable<?> addDrawable(IDrawable drawable) {
			return widgets.add(new DrawableWidget(drawable));
		}
		
		@Override
		public void addWidget(IRecipeWidget recipeWidget) {
			recipeWidgets.add(recipeWidget);
		}
		
		@Override
		public void addSlottedWidget(ISlottedRecipeWidget slottedRecipeWidget, List<IRecipeSlotDrawable> slots) {
			// TODO
			throw new UnsupportedOperationException();
		}
		
		@Override
		public void addInputHandler(IJeiInputHandler inputHandler) {
			inputHandlers.add(inputHandler);
		}
		
		@Override
		public void addGuiEventListener(IJeiGuiEventListener listener) {
			guiEventListeners.add(listener);
		}
		
		@Override
		public IScrollBoxWidget addScrollBoxWidget(int width, int height, int x, int y) {
			final var widget = new ScrollBoxRecipeWidget(width, height, x, y);
			addWidget(widget);
			addInputHandler(widget);
			return widget;
		}
		
		@Override
		public IScrollGridWidget addScrollGridWidget(List<IRecipeSlotDrawable> slots, int columns, int visibleRows) {
			return new ScrollGridWidget(widgets, slots, columns, visibleRows);
		}
		
		private DeferredPlaceableWidget addTexture(EmiTexture texture) {
			return new DeferredPlaceableWidget((x, y) -> widgets.addTexture(texture, x, y), texture.width, texture.height);
		}
		
		@Override
		public IPlaceable<?> addRecipeArrow() {
			return addTexture(EmiTexture.EMPTY_ARROW);
		}
		
		@Override
		public IPlaceable<?> addRecipePlusSign() {
			return addTexture(EmiTexture.PLUS);
		}
		
		@Override
		public IPlaceable<?> addAnimatedRecipeArrow(int cookTime) {
			return new DeferredPlaceableWidget((x, y) -> widgets.addFillingArrow(x, y, cookTime), EmiTexture.EMPTY_ARROW.width, EmiTexture.EMPTY_ARROW.height);
		}
		
		@Override
		public IPlaceable<?> addAnimatedRecipeFlame(int cookTime) {
			return new DeferredPlaceableWidget((x, y) -> widgets.add(new FillingFlameWidget(x, y, cookTime)), EmiTexture.EMPTY_FLAME.width, EmiTexture.EMPTY_FLAME.height);
		}
		
		@Override
		public ITextWidget addText(List<FormattedText> lines, int maxWidth, int maxHeight) {
			return widgets.add(new TextWidget(lines, maxWidth, maxHeight));
		}
		
	}
	
}
