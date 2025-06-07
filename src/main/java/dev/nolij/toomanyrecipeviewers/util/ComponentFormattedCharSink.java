package dev.nolij.toomanyrecipeviewers.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import org.jetbrains.annotations.NotNull;

public class ComponentFormattedCharSink implements FormattedCharSink {
	
	public static MutableComponent fromSequence(FormattedCharSequence sequence) {
		final var sink = new ComponentFormattedCharSink();
		sequence.accept(sink);
		return sink.component;
	}
	
	public MutableComponent component = Component.empty();
	
	@Override
	public boolean accept(int width, @NotNull Style style, int codePoint) {
		component.append(Component.literal(new String(Character.toChars(codePoint))).withStyle(style));
		return true;
	}
	
}
