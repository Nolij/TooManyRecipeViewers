package dev.nolij.toomanyrecipeviewers.impl.api.registration;

import com.mojang.datafixers.util.Pair;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.jemi.JemiUtil;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.registration.IIngredientAliasRegistration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class IngredientAliasRegistration implements IIngredientAliasRegistration {
	
	private boolean locked = false;
	private final List<Pair<EmiStack, String>> aliases = new ArrayList<>();
	
	public List<Pair<EmiStack, String>> getAliasesAndLock() {
		if (locked)
			throw new IllegalStateException();
		
		locked = true;
		return aliases;
	}
	
	@Override
	public <I> void addAlias(IIngredientType<I> type, I ingredient, String alias) {
		if (locked)
			throw new IllegalStateException();
		
		final var stack = JemiUtil.getStack(type, ingredient);
		aliases.add(Pair.of(stack, alias));
	}
	
	@Override
	public <I> void addAlias(ITypedIngredient<I> typedIngredient, String alias) {
		if (locked)
			throw new IllegalStateException();
		
		final var stack = JemiUtil.getStack(typedIngredient);
		aliases.add(Pair.of(stack, alias));
	}
	
	@Override
	public <I> void addAliases(Collection<ITypedIngredient<I>> typedIngredients, Collection<String> aliases) {
		for (final var typedIngredient : typedIngredients) {
			for (final var alias : aliases) {
				addAlias(typedIngredient, alias);
			}
		}
	}
	
	@Override
	public <I> void addAliases(Collection<ITypedIngredient<I>> typedIngredients, String alias) {
		for (final var typedIngredient : typedIngredients) {
			addAlias(typedIngredient, alias);
		}
	}
	
	@Override
	public <I> void addAliases(IIngredientType<I> type, Collection<I> ingredients, Collection<String> aliases) {
		for (final var ingredient : ingredients) {
			for (final var alias : aliases) {
				addAlias(type, ingredient, alias);
			}
		}
	}
	
	@Override
	public <I> void addAliases(IIngredientType<I> type, Collection<I> ingredients, String alias) {
		for (final var ingredient : ingredients) {
			addAlias(type, ingredient, alias);
		}
	}
	
	@Override
	public <I> void addAliases(IIngredientType<I> type, I ingredient, Collection<String> aliases) {
		for (final var alias : aliases) {
			addAlias(type, ingredient, alias);
		}
	}
	
	@Override
	public <I> void addAliases(ITypedIngredient<I> typedIngredient, Collection<String> aliases) {
		for (final var alias : aliases) {
			addAlias(typedIngredient, alias);
		}
	}
	
}
