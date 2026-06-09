package dev.nolij.toomanyrecipeviewers.impl.recipe;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public interface IDebuggableRecipe {
	
	record DebugInfo(String type, ResourceLocation id, int hColour, int vColour, List<ClientTooltipComponent> tooltip) {
		public DebugInfo(String type, ResourceLocation id, int colour, List<ClientTooltipComponent> tooltip) {
			this(type, id, colour, colour, tooltip);
		}
		
		public DebugInfo(String type, ResourceLocation id, int hColour, int vColour) {
			this(type, id, hColour, vColour, List.of());
		}
		
		public DebugInfo(String type, ResourceLocation id, int colour) {
			this(type, id, colour, List.of());
		}
	}
	
	DebugInfo getDebugInfo();
	
}
