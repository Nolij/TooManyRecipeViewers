package dev.nolij.toomanyrecipeviewers.impl.recipe;

import com.mojang.blaze3d.platform.InputConstants;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.jemi.impl.JemiTooltipBuilder;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.EmiScreenManager;
import dev.nolij.toomanyrecipeviewers.impl.jei.api.gui.builder.RecipeLayoutBuilder;
import dev.nolij.toomanyrecipeviewers.impl.jei.api.gui.widgets.ScrollGridWidget;
import dev.nolij.toomanyrecipeviewers.impl.widget.DeferredPlaceableWidget;
import dev.nolij.toomanyrecipeviewers.impl.widget.DrawableWidget;
import dev.nolij.toomanyrecipeviewers.impl.widget.FillingFlameWidget;
import dev.nolij.toomanyrecipeviewers.impl.widget.TextWidget;
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
import mezz.jei.library.gui.widgets.ScrollBoxRecipeWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.FormattedText;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;

class TMRVRecipeWidget<T> extends Widget implements IRecipeExtrasBuilder {
	
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
	
	private final WidgetHolder widgets;
	private final RecipeSlotsView slotsView;
	private final RecipeSlotDrawablesView slotDrawablesView;
	private final Bounds bounds;
	private final IRecipeCategory<T> jeiCategory;
	private final T jeiRecipe;
	
	private final ArrayList<IRecipeWidget> recipeWidgets = new ArrayList<>();
	private final ArrayList<IJeiInputHandler> inputHandlers = new ArrayList<>();
	private final ArrayList<IJeiGuiEventListener> guiEventListeners = new ArrayList<>();
	
	TMRVRecipeWidget(WidgetHolder widgets, int width, int height, IRecipeCategory<T> jeiCategory, T jeiRecipe) {
		this.widgets = widgets;
		this.slotsView = new RecipeSlotsView();
		this.slotDrawablesView = new RecipeSlotDrawablesView();
		this.bounds = new Bounds(0, 0, width, height);
		this.jeiCategory = jeiCategory;
		this.jeiRecipe = jeiRecipe;
	}
	
	void addSlotWidgets(RecipeLayoutBuilder builder, EmiRecipe emiRecipe) {
		for (final var slot : builder.getSlots()) {
			if (!slot.isVisible())
				continue;
			
			final var widget = widgets.add(slot.build());
			slotsView.slots.add((IRecipeSlotView) widget);
			slotDrawablesView.slots.add((IRecipeSlotDrawable) widget);
			
			if (slot.role == RecipeIngredientRole.OUTPUT)
				widget.recipeContext(emiRecipe);
		}
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
			
			final var position = recipeWidget.getPosition();
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
			final var position = recipeWidget.getPosition();
			recipeWidget.getTooltip(tooltipBuilder, mouseX - position.x(), mouseY - position.y());
		}
		
		return tooltipBuilder.tooltip;
	}
	
	@Override
	public boolean mouseClicked(int mouseX, int mouseY, int button) {
		return
			handleInput(mouseX, mouseY, InputConstants.Type.MOUSE.getOrCreate(button), 0, InputType.SIMULATE) &&
				handleInput(mouseX, mouseY, InputConstants.Type.MOUSE.getOrCreate(button), 0, InputType.EXECUTE);
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return handleInput(EmiScreenManager.lastMouseX, EmiScreenManager.lastMouseY, InputConstants.getKey(keyCode, scanCode), modifiers, InputType.IMMEDIATE);
	}
	
	private static boolean containsPoint(ScreenRectangle area, int x, int y) {
		return
			area.top() <= y && y <= area.bottom() &&
				area.left() <= x && x <= area.right();
	}
	
	private boolean handleInput(int mouseX, int mouseY, InputConstants.Key key, int modifiers, InputType inputType) {
		final var input = new UserInput(key, mouseX, mouseY, modifiers, inputType);
		
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
		addWidget(slottedRecipeWidget);
		if (slottedRecipeWidget instanceof IJeiInputHandler inputHandler)
			addInputHandler(inputHandler);
		if (slottedRecipeWidget instanceof IJeiGuiEventListener guiEventListener)
			addGuiEventListener(guiEventListener);
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
