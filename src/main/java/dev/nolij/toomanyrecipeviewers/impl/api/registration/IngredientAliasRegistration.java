package dev.nolij.toomanyrecipeviewers.impl.api.registration;

import dev.emi.emi.jemi.JemiUtil;
import dev.emi.emi.search.EmiSearch;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.registration.IIngredientAliasRegistration;

import java.util.Collection;

public class IngredientAliasRegistration implements IIngredientAliasRegistration {
	
	@Override
	public <I> void addAlias(IIngredientType<I> type, I ingredient, String alias) {
		final var stack = JemiUtil.getStack(type, ingredient);
		EmiSearch.aliases.add(stack, alias);
	}
	
	@Override
	public <I> void addAlias(ITypedIngredient<I> typedIngredient, String alias) {
		final var stack = JemiUtil.getStack(typedIngredient);
		EmiSearch.aliases.add(stack, alias);
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
