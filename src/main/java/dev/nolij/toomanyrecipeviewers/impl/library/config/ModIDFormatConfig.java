package dev.nolij.toomanyrecipeviewers.impl.library.config;

import mezz.jei.library.config.IModIdFormatConfig;
import mezz.jei.library.config.ModIdFormatConfig;
import org.jetbrains.annotations.NotNull;

public class ModIDFormatConfig implements IModIdFormatConfig {
	
	@Override
	public @NotNull String getModNameFormat() {
		return ModIdFormatConfig.MOD_NAME_FORMAT_CODE;
	}
	
	@Override
	public boolean isModNameFormatOverrideActive() {
		return false;
	}
	
}
