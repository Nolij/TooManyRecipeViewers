package dev.nolij.toomanyrecipeviewers.impl.decorator;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeDecorator;
import dev.emi.emi.api.render.EmiTooltipComponents;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.nolij.toomanyrecipeviewers.TooManyRecipeViewers;
import dev.nolij.toomanyrecipeviewers.impl.recipe.IDebuggableRecipe;
import dev.nolij.toomanyrecipeviewers.impl.widget.BorderWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

import java.util.ArrayList;

import static dev.nolij.toomanyrecipeviewers.TooManyRecipeViewersMod.shouldShowDebugInfo;

@SuppressWarnings("UnstableApiUsage")
public class TMRVDebugDecorator implements EmiRecipeDecorator {
	
	private final TooManyRecipeViewers runtime;
	
	public TMRVDebugDecorator(TooManyRecipeViewers runtime) {
		this.runtime = runtime;
	}
	
	@Override
	public void decorateRecipe(EmiRecipe emiRecipe, WidgetHolder widgets) {
		if (shouldShowDebugInfo() && emiRecipe instanceof IDebuggableRecipe debuggableRecipe) {
			final var category = runtime.recipeManager.category(emiRecipe.getCategory());
			final var recipe = category.recipe(emiRecipe);
			
			final var categoryPlugin = category.getPlugin();
			final var recipePlugin = recipe.getPlugin();
			
			final var debugInfo = debuggableRecipe.getDebugInfo();
			final var width = emiRecipe.getDisplayWidth();
			final var height = emiRecipe.getDisplayHeight();
			
			widgets.add(new BorderWidget(new Bounds(0, 0, width, height), debugInfo.hColour(), debugInfo.vColour()));
			
			final var tooltip = new ArrayList<ClientTooltipComponent>(4);
			tooltip.add(EmiTooltipComponents.of(EmiPort.literal("ID: " + debugInfo.id(), ChatFormatting.GRAY)));
			tooltip.add(EmiTooltipComponents.of(EmiPort.literal("Type: " + debugInfo.type(), ChatFormatting.GRAY)));
			if (recipePlugin == categoryPlugin) {
				tooltip.add(EmiTooltipComponents.of(EmiPort.literal("Plugin: " + recipePlugin, ChatFormatting.GRAY)));
			} else {
				tooltip.add(EmiTooltipComponents.of(EmiPort.literal("Recipe Plugin: " + recipePlugin, ChatFormatting.GRAY)));
				tooltip.add(EmiTooltipComponents.of(EmiPort.literal("Category Plugin: " + categoryPlugin, ChatFormatting.GRAY)));
			}
			
			tooltip.addAll(debugInfo.tooltip());
			widgets.addTooltip(tooltip, -4, -4, 8, 8);
		}
	}
	
}
