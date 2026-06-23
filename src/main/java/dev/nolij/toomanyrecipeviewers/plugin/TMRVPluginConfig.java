package dev.nolij.toomanyrecipeviewers.plugin;

import dev.nolij.zson.Zson;
import dev.nolij.zson.ZsonField;
import dev.nolij.zson.ZsonValue;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static dev.nolij.toomanyrecipeviewers.TooManyRecipeViewersMod.*;

public class TMRVPluginConfig {
	
	private static final Zson ZSON = new Zson();
	
	public TMRVPluginConfig() {}
	
	@SuppressWarnings({"unchecked", "unused"})
	public TMRVPluginConfig(Map<String, ZsonValue> map) {
		final var typeOverrides_ = map.getOrDefault("typeOverrides", null);
		if (typeOverrides_ != null && typeOverrides_.value instanceof Map<?, ?>) {
			final var typeOverrides = (Map<String, ZsonValue>) typeOverrides_.value;
			for (final var typeOverride : typeOverrides.entrySet()) {
				final var key = typeOverride.getKey();
				final var value = typeOverride.getValue();
				
				try {
					if (!(value.value instanceof String stringValue))
						throw new AssertionError();
					
					final var id = ResourceLocation.parse(key);
					final var type = PluginType.valueOf(stringValue);
					
					this.typeOverrides.put(id, type);
				} catch (Throwable t) {
					LOGGER.error("Error parsing typeOverrides @ { \"{}\": {} }", key, value, t);
				}
			}
		}
		
		final var defaultPhaseOverrides_ = map.getOrDefault("defaultPhaseOverrides", null);
		if (defaultPhaseOverrides_ != null && defaultPhaseOverrides_.value instanceof Map<?, ?>) {
			final var defaultPhaseOverrides = (Map<String, ZsonValue>) defaultPhaseOverrides_.value;
			for (final var defaultPhaseOverride : defaultPhaseOverrides.entrySet()) {
				final var key = defaultPhaseOverride.getKey();
				final var value = defaultPhaseOverride.getValue();
				
				try {
					if (!(value.value instanceof String stringValue))
						throw new AssertionError();
					
					final var dispatchStrategy = DispatchStrategy.valueOf(stringValue);
					
					this.defaultPhaseOverrides.put(key, dispatchStrategy);
				} catch (Throwable t) {
					LOGGER.error("Error parsing defaultPhaseOverrides @ { \"{}\": {} }", key, value, t);
				}
			}
		}
		
		final var pluginPhaseOverrides_ = map.getOrDefault("pluginPhaseOverrides", null);
		if (pluginPhaseOverrides_ != null && pluginPhaseOverrides_.value instanceof Map<?, ?>) {
			final var pluginPhaseOverrides = (Map<String, ZsonValue>) pluginPhaseOverrides_.value;
			for (final var pluginPhaseOverride : pluginPhaseOverrides.entrySet()) {
				final var key = pluginPhaseOverride.getKey();
				final var value = pluginPhaseOverride.getValue();
				
				try {
					if (!(value.value instanceof Map<?, ?>))
						throw new AssertionError();
					
					final var id = ResourceLocation.parse(key);
					final var pluginOverrides = (Map<String, ZsonValue>) value.value;
					
					final var overrides = new HashMap<String, DispatchStrategy>();
					for (final var pluginOverride : pluginOverrides.entrySet()) {
						final var _key = pluginOverride.getKey();
						final var _value = pluginOverride.getValue();
						
						try {
							if (!(_value.value instanceof String _stringValue))
								throw new AssertionError();
							
							final var dispatchStrategy = DispatchStrategy.valueOf(_stringValue);
							
							overrides.put(_key, dispatchStrategy);
						} catch (Throwable t) {
							LOGGER.error("Error parsing pluginPhaseOverrides[\"{}\"] @ { \"{}\": {} }", key, _key, _value, t);
						}
					}
					this.pluginPhaseOverrides.put(id, overrides);
				} catch (Throwable t) {
					LOGGER.error("Error parsing pluginPhaseOverrides @ { \"{}\": {} }", key, value, t);
				}
			}
		}
	}
	
	@ZsonField
	public @Nullable Boolean allowDefaultAsync = null;
	
	@ZsonField
	public boolean defaultDisableDuplicatePlugins = false;
	
	@ZsonField(serializeOnly = true)
	public Map<ResourceLocation, PluginType> typeOverrides = new HashMap<>();
	
	@ZsonField(serializeOnly = true)
	public Map<String, DispatchStrategy> defaultPhaseOverrides = new HashMap<>();
	
	@ZsonField(serializeOnly = true)
	public Map<ResourceLocation, Map<String, DispatchStrategy>> pluginPhaseOverrides = new HashMap<>();
	
	public static @NotNull TMRVPluginConfig readOrCreate(final File file) {
		final var result = read(file);
		
		if (result != null)
			return result;
		
		return new TMRVPluginConfig();
	}
	
	public static @Nullable TMRVPluginConfig read(final File file) {
		if (file == null || !file.exists())
			return null;
		
		return read(new FileReader(file));
	}
	
	public static @Nullable TMRVPluginConfig read(final Reader input) {
		try {
			return Zson.map2Obj(Objects.requireNonNull(Zson.parse(input)), TMRVPluginConfig.class);
		} catch (Throwable t) {
			LOGGER.error("Error reading config: ", t);
			return null;
		}
	}
	
	public void write(final File file) {
		try (final var writer = new FileWriter(file)) {
			ZSON.write(Zson.obj2Map(this), writer);
			writer.flush();
		} catch (Throwable t) {
			LOGGER.error("Error writing config: ", t);
		}
	}
	
}
