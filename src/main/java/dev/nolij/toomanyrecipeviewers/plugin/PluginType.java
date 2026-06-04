package dev.nolij.toomanyrecipeviewers.plugin;

import org.jetbrains.annotations.Nullable;

public enum PluginType {
	
	NORMAL(null, false),
	DISABLED("⁻", false),
	VANILLA_PLUGIN("⁰", false),
	JEI_INTERNAL_PLUGIN("ⁱ", false),
	DUPLICATE_MODID("¹", true),
	DUPLICATE_NAMESPACE("²", true),
	FORCE_PARTIAL_LOAD("³", true),
	
	;
	
	public final @Nullable String identifier;
	public final boolean partialLoad;
	
	PluginType(@Nullable String identifier, boolean partialLoad) {
		this.identifier = identifier;
		this.partialLoad = partialLoad;
	}
	
}
