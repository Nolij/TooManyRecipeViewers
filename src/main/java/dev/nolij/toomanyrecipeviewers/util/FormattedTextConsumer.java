package dev.nolij.toomanyrecipeviewers.util;

import net.minecraft.network.chat.FormattedText;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class FormattedTextConsumer implements FormattedText.ContentConsumer<Void> {
	
	public static String fromFormattedText(FormattedText formattedText) {
		final var consumer = new FormattedTextConsumer();
		formattedText.visit(consumer);
		return consumer.getString();
	}
	
	private final StringBuilder builder = new StringBuilder();
	
	@Override
	public @NotNull Optional<Void> accept(@NotNull String string) {
		builder.append(string);
		return Optional.empty();
	}
	
	public String getString() {
		return builder.toString();
	}
	
}
