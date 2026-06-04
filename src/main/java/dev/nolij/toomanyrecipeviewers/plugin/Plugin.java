package dev.nolij.toomanyrecipeviewers.plugin;

import mezz.jei.api.IModPlugin;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record Plugin(ResourceLocation id, PluginType type, boolean hasOverrides, IModPlugin object) {
	
	@Override
	public @NotNull String toString() {
		final var result = new StringBuilder(id.toString());
		
		if (type.identifier != null)
			result.append(type.identifier);
		if (hasOverrides)
			result.append("*");
		
		return result.toString();
	}
	
}
