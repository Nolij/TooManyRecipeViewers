package dev.nolij.toomanyrecipeviewers.impl.ingredient;

//? if >=21.1 {
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
//?} else {
/*import mezz.jei.api.ingredients.subtypes.UidContext;
*///?}
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer;
import dev.nolij.toomanyrecipeviewers.TooManyRecipeViewers;
import net.minecraft.util.GsonHelper;

import static dev.nolij.toomanyrecipeviewers.TooManyRecipeViewersMod.LOGGER;

@SuppressWarnings({"rawtypes", "unchecked"})
public class TMRVStackSerializer implements EmiIngredientSerializer<TMRVStack> {

	private static final String TYPE = "type";
	private static final String TYPE_UID = "type_uid";
	//? if >=21.1 {
	private static final String DATA = "data";
	//?} else
	//private static final String UID = "uid";
	private static final String AMOUNT = "amount";
	private static final String CHANCE = "chance";
	private static final String REMAINDER = "remainder";

	private final TooManyRecipeViewers runtime;

	public TMRVStackSerializer(TooManyRecipeViewers runtime) {
		this.runtime = runtime;
	}

	@Override
	public String getType() {
		return "tmrv";
	}

	@Override
	public EmiIngredient deserialize(JsonElement element) {
		final var object = element.getAsJsonObject();
		final var typeUID = GsonHelper.getAsString(object, TYPE_UID);
		final var type = runtime.ingredientManager.getIngredientType(typeUID);
		final var info = runtime.ingredientManager.getIngredientInfo(type);

		//? if >=21.1 {
		final var data = GsonHelper.getNonNull(object, DATA);
		final var codec = info.getIngredientCodec();
		final var decode = codec.parse(JsonOps.INSTANCE, data);
		final var ingredient = decode.resultOrPartial(LOGGER::error).orElseThrow();
		//?} else {
		/*final var uid = GsonHelper.getAsString(object, UID);
		final var ingredient = runtime.ingredientManager.getIngredientByUid(type, uid);
		*///?}

		final var stack = runtime.ingredientManager.getEMIStack(type, ingredient);
		
		stack.setAmount(GsonHelper.getAsLong(object, AMOUNT, 1L));
		stack.setChance(GsonHelper.getAsFloat(object, CHANCE, 1F));
		if (GsonHelper.isValidNode(object, REMAINDER)) {
			final var remainderIngredient = EmiIngredientSerializer.getDeserialized(GsonHelper.getNonNull(object, REMAINDER));
			if (remainderIngredient instanceof EmiStack remainderStack) {
				stack.setRemainder(remainderStack);
			} else {
				LOGGER.error("Failure deserializing remainder for stack: {}", element.getAsString());
			}
		}
		
		return stack;
	}

	@Override
	public JsonElement serialize(TMRVStack stack) {
		final var result = new JsonObject();
		result.addProperty(TYPE, getType());
		result.addProperty(TYPE_UID, stack.type.getUid());

		//? if >=21.1 {
		final var codec = runtime.ingredientManager.getIngredientCodec(stack.type);
		final DataResult<JsonElement> encode = codec.encodeStart(JsonOps.INSTANCE, stack.ingredient);
		result.add(DATA, encode.resultOrPartial(LOGGER::error).orElseThrow());
		//?} else {
		/*final var helper = runtime.ingredientManager.getIngredientHelper(stack.type);
		result.addProperty(UID, helper.getUniqueId(stack.ingredient, UidContext.Ingredient));
		*///?}
		
		if (stack.getAmount() != 1L)
			result.addProperty(AMOUNT, stack.getAmount());
		if (stack.getChance() != 1F)
			result.addProperty(CHANCE, stack.getChance());
		if (!stack.getRemainder().isEmpty())
			result.add(REMAINDER, EmiIngredientSerializer.getSerialized(stack.getRemainder()));

		return result;
	}

}
