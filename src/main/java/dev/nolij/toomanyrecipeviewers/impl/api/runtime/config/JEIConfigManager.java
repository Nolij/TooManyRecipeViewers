package dev.nolij.toomanyrecipeviewers.impl.api.runtime.config;

import mezz.jei.api.runtime.config.IJeiConfigFile;
import mezz.jei.api.runtime.config.IJeiConfigManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.List;

public class JEIConfigManager implements IJeiConfigManager {
	
	@Override
	public @Unmodifiable @NotNull Collection<IJeiConfigFile> getConfigFiles() {
		return List.of();
	}
	
}
