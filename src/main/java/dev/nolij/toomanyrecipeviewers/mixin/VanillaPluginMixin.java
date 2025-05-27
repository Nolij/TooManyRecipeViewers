package dev.nolij.toomanyrecipeviewers.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.emi.emi.VanillaPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

import static dev.nolij.toomanyrecipeviewers.TooManyRecipeViewers.runtime;

@Mixin(value = VanillaPlugin.class, remap = false)
public class VanillaPluginMixin {
	
	@Inject(method = "addRecipeSafe(Ldev/emi/emi/api/EmiRegistry;Ljava/util/function/Supplier;Lnet/minecraft/world/item/crafting/Recipe;)V", at = @At("HEAD"))
	private static void tmrv$addRecipeSafe$HEAD(EmiRegistry registry, Supplier<EmiRecipe> supplier, Recipe<?> recipe, CallbackInfo ci) {
		runtime.ignoredRecipes.add(recipe);
	}
	
	@Inject(method = "register", at = @At(value = "INVOKE", target = "Ldev/emi/emi/VanillaPlugin;addRecipeSafe(Ldev/emi/emi/api/EmiRegistry;Ljava/util/function/Supplier;)V", ordinal = 0))
	public void tmrv$register$addRecipeSafe(EmiRegistry registry, CallbackInfo ci, @Local CraftingRecipe recipe) {
		runtime.ignoredRecipes.add(recipe);
	}
	
}
