package dev.nolij.toomanyrecipeviewers.impl.recipe.transfer;

import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferManager;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class RecipeTransferManager implements IRecipeTransferManager {
	
	@Override
	public <C extends AbstractContainerMenu, R> @NotNull Optional<IRecipeTransferHandler<C, R>> getRecipeTransferHandler(@NotNull C c, @NotNull IRecipeCategory<R> iRecipeCategory) {
		return Optional.empty();
	}
	
}
