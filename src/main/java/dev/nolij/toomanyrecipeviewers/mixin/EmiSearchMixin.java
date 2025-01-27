package dev.nolij.toomanyrecipeviewers.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.search.EmiSearch;
import dev.nolij.toomanyrecipeviewers.TooManyRecipeViewers;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.searchtree.SuffixArray;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.nolij.toomanyrecipeviewers.TooManyRecipeViewersMod.LOGGER;

@Mixin(value = EmiSearch.class, remap = false)
public class EmiSearchMixin {
	
	@Inject(method = "bake", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/searchtree/SuffixArray;generate()V", ordinal = 3, remap = true))
	private static void tmrv$bake$SuffixArray$generate$4(CallbackInfo ci, @Local(ordinal = 3) SuffixArray<EmiStack> emiAliases) {
		final var ingredientAliasRegistration = TooManyRecipeViewers.runtime.ingredientAliasRegistration;
		if (ingredientAliasRegistration == null) {
			LOGGER.warn("Failed to register ingredient aliases from JEI plugins!");
			return;
		}
		
		final var jeiAliases = ingredientAliasRegistration.getAliasesAndLock();
		for (final var pair : jeiAliases) {
			if (!I18n.exists(pair.getSecond()))
				LOGGER.warn("Untranslated alias {}", pair.getSecond());
			
			final var alias = I18n.get(pair.getSecond()).toLowerCase();
			emiAliases.add(pair.getFirst(), alias);
		}
		LOGGER.info("Registered {} ingredient aliases from JEI plugins", jeiAliases.size());
	}
	
}
