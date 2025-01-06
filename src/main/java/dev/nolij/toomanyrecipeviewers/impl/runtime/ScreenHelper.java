package dev.nolij.toomanyrecipeviewers.impl.runtime;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IScreenHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class ScreenHelper implements IScreenHelper {
	
	@Override
	public Stream<IClickableIngredient<?>> getClickableIngredientUnderMouse(Screen screen, double v, double v1) {
		return Stream.empty();
	}
	
	@Override
	public <T extends Screen> Optional<IGuiProperties> getGuiProperties(T t) {
		return Optional.empty();
	}
	
	@Override
	public Stream<IGuiClickableArea> getGuiClickableArea(AbstractContainerScreen<?> abstractContainerScreen, double v, double v1) {
		return Stream.empty();
	}
	
	@Override
	public Stream<Rect2i> getGuiExclusionAreas(Screen screen) {
		return Stream.empty();
	}
	
	@Override
	public <T extends Screen> List<IGhostIngredientHandler<T>> getGhostIngredientHandlers(T t) {
		return List.of();
	}
	
}
