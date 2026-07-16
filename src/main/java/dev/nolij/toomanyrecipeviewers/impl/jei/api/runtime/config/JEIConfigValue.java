package dev.nolij.toomanyrecipeviewers.impl.jei.api.runtime.config;

//? if >=21.1 {
import mezz.jei.api.runtime.config.IJeiConfigValue;
import mezz.jei.api.runtime.config.IJeiConfigValueSerializer;
import mezz.jei.common.config.file.serializers.DeserializeResult;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public record JEIConfigValue<T>(T value) implements IJeiConfigValue<T> {
	
	@Override
	public String getName() {
		return "";
	}
	
	@SuppressWarnings("removal")
	@Override
	public String getDescription() {
		return "";
	}
	
	@Override
	public Component getLocalizedName() {
		return Component.empty();
	}
	
	@Override
	public Component getLocalizedDescription() {
		return Component.empty();
	}
	
	@Override
	public T getValue() {
		return value;
	}
	
	@Override
	public T getDefaultValue() {
		return value;
	}
	
	@Override
	public boolean set(T t) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public IJeiConfigValueSerializer<T> getSerializer() {
		return new IJeiConfigValueSerializer<T>() {
			@Override
			public String serialize(T t) {
				return "";
			}
			
			@Override
			public IDeserializeResult<T> deserialize(String s) {
				return new DeserializeResult<>(value);
			}
			
			@Override
			public boolean isValid(T t) {
				return t == value;
			}
			
			@Override
			public Optional<Collection<T>> getAllValidValues() {
				return Optional.of(List.of(value));
			}
			
			@Override
			public String getValidValuesDescription() {
				return "";
			}
		};
	}
	
}
//?}