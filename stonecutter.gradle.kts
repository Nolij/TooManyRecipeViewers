plugins {
    id("dev.kikugie.stonecutter")
}
stonecutter active "1.21.1-neoforge" /* [SC] DO NOT EDIT */

stonecutter registerChiseled tasks.register("chiseledBuild", stonecutter.chiseled) { 
    group = "project"
    ofTask("build")
}

stonecutter.parameters {
    val modloader = ModLoader.fromProjectName(metadata.project)
    const("forge", modloader == ModLoader.FORGE)
    const("neoforge", modloader == ModLoader.NEOFORGE)

    replacement(eval(metadata.version, ">=1.21"), "mezz.jei.forge", "mezz.jei.neoforge")
    // RecipeManager.java
    replacement(eval(metadata.version, ">=1.21"), "jemiRecipe.recipe", "jemiRecipe.recipe.value()")
    arrayOf("CraftingRecipe", "SmeltingRecipe", "BlastingRecipe", "SmokingRecipe", "CampfireCookingRecipe", "StonecutterRecipe", "SmithingRecipe").forEach {
        replacement(eval(metadata.version, ">=1.21"), "JemiRecipe<${it}>", "JemiRecipe<RecipeHolder<${it}>>")
    }
}
