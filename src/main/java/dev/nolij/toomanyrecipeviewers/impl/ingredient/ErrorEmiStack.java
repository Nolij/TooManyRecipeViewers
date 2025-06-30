package dev.nolij.toomanyrecipeviewers.impl.ingredient;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

import static dev.nolij.toomanyrecipeviewers.TooManyRecipeViewersConstants.MOD_ID;

public class ErrorEmiStack extends EmiStack {
	
	private static final Font FONT = Minecraft.getInstance().font;
	private static final int Y_OFFSET = 8 - (FONT.lineHeight / 2);
	private static final int BACKGROUND_COLOR = 0xFFAA0000;
	private static final int TEXT_COLOUR = 0xFFDDDD33;
	
	static void render(GuiGraphics draw) {
		draw.fill(0, 0, 16, 16, BACKGROUND_COLOR);
		draw.drawCenteredString(FONT, Component.literal("!?!").withStyle(ChatFormatting.OBFUSCATED), 8, Y_OFFSET, TEXT_COLOUR);
	}
	
	public static final ErrorEmiStack INSTANCE = new ErrorEmiStack();
	
	private ErrorEmiStack() {}
	
	@Override
	public EmiStack copy() {
		return this;
	}
	
	@Override
	public void render(GuiGraphics draw, int x, int y, float delta, int flags) {
		final var context = EmiDrawContext.wrap(draw);
		
		context.push();
		context.matrices().translate(x, y, 0);
		
		render(context.raw());
		
		context.pop();
	}
	
	@Override
	public boolean isEmpty() {
		return false;
	}
	
	@Override
	//? if >=21.1 {
	public DataComponentPatch getComponentChanges() {
		return DataComponentPatch.EMPTY;
	}
	//?} else {
	/*public CompoundTag getNbt() {
		return null;
	}
	*///?}
	
	@Override
	public Object getKey() {
		return ErrorEmiStack.class;
	}
	
	@Override
	public ResourceLocation getId() {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, "error");
	}
	
	@Override
	public List<Component> getTooltipText() {
		return List.of(
			Component.literal("ERROR")
				.withStyle(ChatFormatting.RED)
				.withStyle(ChatFormatting.BOLD),
			Component.literal("This shouldn't be here. It only shows when TooManyRecipeViewers has encountered an error. Please report this via a GitHub issue.\n")
				.withStyle(ChatFormatting.DARK_RED),
			Component.literal("PLEASE INCLUDE WITH YOUR REPORT:")
				.withStyle(ChatFormatting.WHITE)
				.withStyle(ChatFormatting.BOLD)
				.withStyle(ChatFormatting.UNDERLINE),
			Component.literal("""
				- Screenshots (WITHOUT this tooltip; it might cover useful information!)
				- What recipe you were trying to view
				- Which mod adds it (including version)
				- TooManyRecipeViewers version
				- Any other potentially useful information""")
		);
	}
	
	@Override
	public List<ClientTooltipComponent> getTooltip() {
		return getTooltipText().stream()
			.map(Component::getVisualOrderText)
			.map(ClientTooltipComponent::create)
			.toList();
	}
	
	@Override
	public Component getName() {
		return Component.literal("ERROR");
	}
	
}
