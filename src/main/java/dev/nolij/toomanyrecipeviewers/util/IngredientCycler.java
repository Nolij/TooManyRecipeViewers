package dev.nolij.toomanyrecipeviewers.util;

import dev.emi.emi.EmiUtil;
import mezz.jei.api.ingredients.ITypedIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public abstract class IngredientCycler {
	
	private static final double INCREMENT = 1D / 1000;
	private final int unique = EmiUtil.RANDOM.nextInt();
	
	private Random getRandom(long time) {
		return new Random(new Random(time ^ unique).nextInt());
	}
	
	private long lastGenerate = 0L;
	private @Nullable ITypedIngredient<?> displayedIngredient = null;
	
	protected void updateHook() {}
	protected abstract List<ITypedIngredient<?>> getDisplayedIngredients();
	
	public void reset() {
		lastGenerate = 0L;
	}
	
	public boolean isStatic() {
		return getDisplayedIngredients().size() == 1;
	}
	
	public Optional<ITypedIngredient<?>> getDisplayedIngredient() {
		final var time = (long) (System.currentTimeMillis() * INCREMENT);
		
		if (time > lastGenerate) {
			lastGenerate = time;
			updateHook();
			
			final var ingredients = getDisplayedIngredients();
			
			if (ingredients.isEmpty())
				displayedIngredient = null;
			else if (lastGenerate == 0L || isStatic())
				displayedIngredient = ingredients.getFirst();
			else
				displayedIngredient = ingredients.get(getRandom(time).nextInt(ingredients.size()));
		}
		
		return Optional.ofNullable(displayedIngredient);
	}
	
}
