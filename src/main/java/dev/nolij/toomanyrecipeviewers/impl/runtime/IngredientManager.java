package dev.nolij.toomanyrecipeviewers.impl.runtime;

import com.mojang.serialization.Codec;
import mezz.jei.api.ingredients.*;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.client.renderer.Rect2i;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class IngredientManager implements IIngredientManager {
	
	@Override
	public @Unmodifiable <V> Collection<V> getAllIngredients(IIngredientType<V> iIngredientType) {
		return List.of();
	}
	
	@Override
	public <V> IIngredientHelper<V> getIngredientHelper(V v) {
		return null;
	}
	
	@Override
	public <V> IIngredientHelper<V> getIngredientHelper(IIngredientType<V> iIngredientType) {
		return null;
	}
	
	@Override
	public <V> IIngredientRenderer<V> getIngredientRenderer(V v) {
		return null;
	}
	
	@Override
	public <V> IIngredientRenderer<V> getIngredientRenderer(IIngredientType<V> iIngredientType) {
		return null;
	}
	
	@Override
	public <V> Codec<V> getIngredientCodec(IIngredientType<V> iIngredientType) {
		return null;
	}
	
	@Override
	public @Unmodifiable Collection<IIngredientType<?>> getRegisteredIngredientTypes() {
		return List.of();
	}
	
	@Override
	public Optional<IIngredientType<?>> getIngredientTypeForUid(String s) {
		return Optional.empty();
	}
	
	@Override
	public <V> void addIngredientsAtRuntime(IIngredientType<V> iIngredientType, Collection<V> collection) {
		
	}
	
	@Override
	public <V> void removeIngredientsAtRuntime(IIngredientType<V> iIngredientType, Collection<V> collection) {
		
	}
	
	@Override
	public @Nullable <V> IIngredientType<V> getIngredientType(V v) {
		return null;
	}
	
	@Override
	public <V> Optional<IIngredientType<V>> getIngredientTypeChecked(V v) {
		return Optional.empty();
	}
	
	@Override
	public <B, I> Optional<IIngredientTypeWithSubtypes<B, I>> getIngredientTypeWithSubtypesFromBase(B b) {
		return Optional.empty();
	}
	
	@Override
	public <V> Optional<IIngredientType<V>> getIngredientTypeChecked(Class<? extends V> aClass) {
		return Optional.empty();
	}
	
	@Override
	public <V> Optional<ITypedIngredient<V>> createTypedIngredient(IIngredientType<V> iIngredientType, V v) {
		return Optional.empty();
	}
	
	@Override
	public <V> ITypedIngredient<V> normalizeTypedIngredient(ITypedIngredient<V> iTypedIngredient) {
		return null;
	}
	
	@Override
	public <V> Optional<IClickableIngredient<V>> createClickableIngredient(IIngredientType<V> iIngredientType, V v, Rect2i rect2i, boolean b) {
		return Optional.empty();
	}
	
	@SuppressWarnings("removal")
	@Override
	public <V> Optional<V> getIngredientByUid(IIngredientType<V> iIngredientType, String s) {
		return Optional.empty();
	}
	
	@SuppressWarnings("removal")
	@Override
	public <V> Optional<ITypedIngredient<V>> getTypedIngredientByUid(IIngredientType<V> iIngredientType, String s) {
		return Optional.empty();
	}
	
	@Override
	public Collection<String> getIngredientAliases(ITypedIngredient<?> iTypedIngredient) {
		return List.of();
	}
	
	@Override
	public void registerIngredientListener(IIngredientListener iIngredientListener) {
		
	}
	
}
