package dev.nolij.toomanyrecipeviewers.impl.api.runtime;

import com.mojang.blaze3d.platform.InputConstants;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.input.EmiBind;
import mezz.jei.api.runtime.IJeiKeyMapping;
import mezz.jei.common.input.IInternalKeyMappings;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class JEIKeyMappings implements IInternalKeyMappings {
	
	private record EMIDelegatedJEIKeyMapping(Supplier<EmiBind> delegateGetter) implements IJeiKeyMapping {
		
		@Override
		public boolean isActiveAndMatches(InputConstants.@NotNull Key key) {
			return switch (key.getType()) {
				case KEYSYM -> delegateGetter.get().matchesKey(key.getValue(), 0);
				case SCANCODE -> delegateGetter.get().matchesKey(0, key.getValue());
				case MOUSE -> delegateGetter.get().matchesMouse(key.getValue());
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
	
	private record MinecraftDelegatedJEIKeyMapping(KeyMapping delegate) implements IJeiKeyMapping {
		
		@Override
		public boolean isActiveAndMatches(InputConstants.@NotNull Key key) {
			return delegate.isActiveAndMatches(key);
		}
		
		@Override
		public boolean isUnbound() {
			return delegate.isUnbound();
		}
		
		@Override
		public @NotNull Component getTranslatedKeyMessage() {
			return delegate.getTranslatedKeyMessage();
		}
		
	}
	
	private static class DummyJEIKeyMapping implements IJeiKeyMapping {
		
		@Override
		public boolean isActiveAndMatches(InputConstants.@NotNull Key key) {
			return false;
		}
		
		@Override
		public boolean isUnbound() {
			return true;
		}
		
		@Override
		public @NotNull Component getTranslatedKeyMessage() {
			return Component.translatable("key.keyboard.unknown");
		}
		
	}
	
	private final IJeiKeyMapping dummy = new DummyJEIKeyMapping();
	
	private final IJeiKeyMapping toggleVisibility = new EMIDelegatedJEIKeyMapping(() -> EmiConfig.toggleVisibility);
	private final IJeiKeyMapping focusSearch = new EMIDelegatedJEIKeyMapping(() -> EmiConfig.focusSearch);
	private final IJeiKeyMapping back = new EMIDelegatedJEIKeyMapping(() -> EmiConfig.back);
	private final IJeiKeyMapping viewRecipes = new EMIDelegatedJEIKeyMapping(() -> EmiConfig.viewRecipes);
	private final IJeiKeyMapping viewUses = new EMIDelegatedJEIKeyMapping(() -> EmiConfig.viewUses);
	private final IJeiKeyMapping cheatOneToInventory = new EMIDelegatedJEIKeyMapping(() -> EmiConfig.cheatOneToInventory);
	private final IJeiKeyMapping cheatStackToInventory = new EMIDelegatedJEIKeyMapping(() -> EmiConfig.cheatStackToInventory);
	
	@Override
	public IJeiKeyMapping getToggleOverlay() {
		return toggleVisibility;
	}
	
	@Override
	public IJeiKeyMapping getFocusSearch() {
		return focusSearch;
	}
	
	@Override
	public IJeiKeyMapping getToggleCheatMode() {
		return dummy;
	}
	
	@Override
	public IJeiKeyMapping getToggleEditMode() {
		return dummy;
	}
	
	@Override
	public IJeiKeyMapping getToggleCheatModeConfigButton() {
		return dummy;
	}
	
	@Override
	public IJeiKeyMapping getRecipeBack() {
		return back;
	}
	
	@Override
	public IJeiKeyMapping getPreviousCategory() {
		return dummy;
	}
	
	@Override
	public IJeiKeyMapping getNextCategory() {
		return dummy;
	}
	
	@Override
	public IJeiKeyMapping getPreviousRecipePage() {
		return dummy;
	}
	
	@Override
	public IJeiKeyMapping getNextRecipePage() {
		return dummy;
	}
	
	@Override
	public IJeiKeyMapping getPreviousPage() {
		return dummy;
	}
	
	@Override
	public IJeiKeyMapping getNextPage() {
		return dummy;
	}
	
	@Override
	public IJeiKeyMapping getCloseRecipeGui() {
		return dummy;
	}
	
	@Override
	public IJeiKeyMapping getBookmark() {
		return dummy;
	}
	
	@Override
	public IJeiKeyMapping getToggleBookmarkOverlay() {
		return dummy;
	}
	
	@Override
	public @NotNull IJeiKeyMapping getShowRecipe() {
		return viewRecipes;
	}
	
	@Override
	public @NotNull IJeiKeyMapping getShowUses() {
		return viewUses;
	}
	
	@Override
	public IJeiKeyMapping getTransferRecipeBookmark() {
		return dummy;
	}
	
	@Override
	public IJeiKeyMapping getMaxTransferRecipeBookmark() {
		return dummy;
	}
	
	@Override
	public IJeiKeyMapping getCheatOneItem() {
		return cheatOneToInventory;
	}
	
	@Override
	public IJeiKeyMapping getCheatItemStack() {
		return cheatStackToInventory;
	}
	
	@Override
	public IJeiKeyMapping getToggleHideIngredient() {
		return dummy;
	}
	
	@Override
	public IJeiKeyMapping getToggleWildcardHideIngredient() {
		return dummy;
	}
	
	@Override
	public IJeiKeyMapping getHoveredClearSearchBar() {
		return dummy;
	}
	
	@Override
	public IJeiKeyMapping getPreviousSearch() {
		return dummy;
	}
	
	@Override
	public IJeiKeyMapping getNextSearch() {
		return dummy;
	}
	
	@Override
	public IJeiKeyMapping getCopyRecipeId() {
		return dummy;
	}
	
	@Override
	public IJeiKeyMapping getEscapeKey() {
		return dummy; // TODO
	}
	
	@Override
	public IJeiKeyMapping getLeftClick() {
		return dummy; // TODO
	}
	
	@Override
	public IJeiKeyMapping getRightClick() {
		return dummy; // TODO
	}
	
	@Override
	public IJeiKeyMapping getEnterKey() {
		return dummy; // TODO
	}
	
}
