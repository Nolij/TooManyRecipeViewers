package dev.nolij.toomanyrecipeviewers.impl.ingredient;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.library.ingredients.TypedIngredient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ErrorIngredient implements IIngredientType<ErrorIngredient>, IIngredientHelper<ErrorIngredient>, IIngredientRenderer<ErrorIngredient> {
	
	public static final ErrorIngredient INSTANCE = new ErrorIngredient();
	public static final ITypedIngredient<ErrorIngredient> TYPED_INSTANCE = TypedIngredient.createUnvalidated(INSTANCE, INSTANCE);
	
	private ErrorIngredient() {}
	
	//region IIngredientType
	@Override
	public String getUid() {
		return ErrorEmiStack.INSTANCE.getId().toString();
	}
	
	@Override
	public Class<? extends ErrorIngredient> getIngredientClass() {
		return ErrorIngredient.class;
	}
	//endregion
	
	//region IIngredientHelper
	@Override
	public IIngredientType<ErrorIngredient> getIngredientType() {
		return this;
	}
	
	@Override
	public String getDisplayName(ErrorIngredient errorIngredient) {
		return "ERROR";
	}
	
	@SuppressWarnings("removal")
	@Override
	public String getUniqueId(ErrorIngredient errorIngredient, UidContext uidContext) {
		return ErrorEmiStack.INSTANCE.getId().toString();
	}
	
	@Override
	public ResourceLocation getResourceLocation(ErrorIngredient errorIngredient) {
		return ErrorEmiStack.INSTANCE.getId();
	}
	
	@Override
	public ErrorIngredient copyIngredient(ErrorIngredient errorIngredient) {
		return this;
	}
	
	@Override
	public String getErrorInfo(@Nullable ErrorIngredient errorIngredient) {
		return "";
	}
	//endregion
	
	//region IIngredientRenderer
	@Override
	public void render(GuiGraphics guiGraphics, ErrorIngredient errorIngredient) {
		ErrorEmiStack.render(guiGraphics);
	}
	
	@SuppressWarnings("removal")
	@Override
	public List<Component> getTooltip(ErrorIngredient errorIngredient, TooltipFlag tooltipFlag) {
		return ErrorEmiStack.INSTANCE.getTooltipText();
	}
	//endregion
	
}
