import org.taumc.gradle.publishing.api.PublishChannel
import org.taumc.gradle.publishing.publishing

plugins {
	id("idea")
	id("dev.kikugie.stonecutter")
	id("org.taumc.gradle.versioning")
	id("org.taumc.gradle.publishing")
}
stonecutter active "21.1-neoforge" /* [SC] DO NOT EDIT */

operator fun String.invoke(): String = project.properties[this] as? String ?: error("Property $this not found")

idea.module {
	isDownloadJavadoc = true
	isDownloadSources = true
}

project.group = "maven_group"()
project.version = tau.versioning.version("mod_version"(), project.properties["release_channel"])

val chiseledBuild = tasks.register("chiseledBuild", stonecutter.chiseled) {
	group = "project"
	ofTask("build")
}
stonecutter registerChiseled chiseledBuild

tasks.register("runClientActive") {
	group = "project"
	dependsOn("${stonecutter.current.project}:runClient")
}

tau.publishing.publish {
	useTauGradleVersioning()
	changelog = rootProject.file("CHANGELOG.md").readText()

	github {
		supportAllChannels()

		accessToken = providers.environmentVariable("GITHUB_TOKEN")
		repository = "Nolij/TooManyRecipeViewers"
		tagName = tau.versioning.releaseTag
	}

	curseforge {
		supportChannels(PublishChannel.RELEASE)

		accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
		projectID = 1194921
		projectSlug = "tmrv"
	}

	val iconURL = "https://github.com/Nolij/TooManyRecipeViewers/raw/master/src/main/resources/icon.png"

	discord {
		supportAllChannelsExcluding(PublishChannel.RELEASE)

		webhookURL = providers.environmentVariable("DISCORD_WEBHOOK")
		avatarURL = iconURL

		testBuildPreset(modName = "TooManyRecipeViewers", repoURL = "https://github.com/Nolij/TooManyRecipeViewers")
	}

	discord {
		supportChannels(PublishChannel.RELEASE)

		webhookURL = providers.environmentVariable("DISCORD_WEBHOOK")
		avatarURL = iconURL

		releasePreset(modName = "TooManyRecipeViewers")
	}
}

stonecutter.parameters {
	replacement(eval(metadata.version, ">=21.1"), "mezz.jei.forge", "mezz.jei.neoforge")
	replacement(eval(metadata.version, ">=21.1"), "mezz.jei.api.forge", "mezz.jei.api.neoforge")
	replacement(eval(metadata.version, ">=21.1"), "net.minecraftforge.client.event", "net.neoforged.neoforge.client.event")
	replacement(eval(metadata.version, ">=21.1"), "net.minecraftforge.fml", "net.neoforged.fml")
	replacement(eval(metadata.version, ">=21.1"), "net.minecraftforge.network", "net.neoforged.neoforge.network")
	replacement(eval(metadata.version, ">=21.1"), "net.minecraftforge.fluids", "net.neoforged.neoforge.fluids")
	replacement(eval(metadata.version, ">=21.1"), "@Nullable CompoundTag dataComponentPatch", "DataComponentPatch dataComponentPatch")
	replacement(eval(metadata.version, ">=21.1"), "(dataComponentPatch == null || dataComponentPatch.isEmpty())", "(dataComponentPatch.isEmpty())")
	replacement(eval(metadata.version, ">=21.1"), "import net.minecraft.nbt.CompoundTag;", "import net.minecraft.core.component.DataComponentPatch;")
	replacement(eval(metadata.version, ">=21.1"), "TypedIngredient.deepCopy", "TypedIngredient.defensivelyCopyTypedIngredientFromApi")
	replacement(eval(metadata.version, ">=21.1"), ".getNbt()", ".getComponentChanges()")
	replacement(eval(metadata.version, ">=21.1"), ".getTag()", ".getComponentsPatch()")
}