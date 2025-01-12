package dev.nolij.toomanyrecipeviewers.impl.api.runtime;

import com.mojang.blaze3d.platform.InputConstants;
import dev.emi.emi.input.EmiBind;
import mezz.jei.api.runtime.IJeiKeyMapping;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class JEIKeyMapping implements IJeiKeyMapping {
	
	private final Supplier<EmiBind> delegateGetter;
	
	public JEIKeyMapping(Supplier<EmiBind> delegateGetter) {
		this.delegateGetter = delegateGetter;
	}
	
	@Override
	public boolean isActiveAndMatches(InputConstants.@NotNull Key key) {
		return switch (key.getType()) {
			case KEYSYM -> delegateGetter.get().matchesKey(key.getValue(), 0);
			case SCANCODE -> delegateGetter.get().matchesKey(0, key.getValue());
			case MOUSE -> delegateGetter.get().matchesMouse(key.getValue());
			default -> false;
		};
	}
	
	@Override
	public boolean isUnbound() {
		return !delegateGetter.get().isBound();
	}
	
	@Override
	public @NotNull Component getTranslatedKeyMessage() {
		return delegateGetter.get().getBindText();
	}
	
}
