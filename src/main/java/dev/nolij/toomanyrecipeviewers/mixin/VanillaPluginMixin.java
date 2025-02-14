package dev.nolij.toomanyrecipeviewers.mixin;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.registry.EmiStackList;
import mezz.jei.common.util.StackHelper;
import mezz.jei.library.plugins.vanilla.VanillaPlugin;
import mezz.jei.library.plugins.vanilla.ingredients.ItemStackHelper;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(value = VanillaPlugin.class, remap = false)
public class VanillaPluginMixin {
	
	@Redirect(method = "registerIngredients", at = @At(value = "INVOKE", target =
			"Lmezz/jei/library/plugins/vanilla/ingredients/ItemStackListFactory;create(Lmezz/jei/common/util/StackHelper;"
			//? if >=1.21.1
			+ "Lmezz/jei/library/plugins/vanilla/ingredients/ItemStackHelper;"
			+ ")Ljava/util/List;"))
	public List<ItemStack> tmrv$registerIngredients$ItemStackListFactory$create(StackHelper stackHelper/*? if >=1.21.1 {*/, ItemStackHelper itemStackHelper/*?}*/) {
		return EmiStackList.stacks
			.stream()
			.map(EmiStack::getItemStack)
			.filter(x -> x != ItemStack.EMPTY)
			.toList();
	}
	
}
