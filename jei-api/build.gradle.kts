import org.taumc.gradle.minecraft.MinecraftVersion
import org.taumc.gradle.minecraft.ModLoader
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.Properties

plugins {
	id("java")
	id("com.gradleup.shadow")
	id("xyz.wagyourtail.unimined")
}

val localPropertiesFile = rootDir.resolve("versions/${stonecutter.current.project}/gradle.properties")
if (localPropertiesFile.exists()) {
	val localProperties = Properties()
	localProperties.load(localPropertiesFile.inputStream())
	localProperties.forEach { (k, v) -> if (k is String) project.extra.set(k, v) }
}
operator fun String.invoke(): String = project.properties[this] as? String ?: error("Property $this not found")

project.group = ""

base {
	archivesName = "jei-api"
}

repositories {
	maven("https://repo.spongepowered.org/maven")
	maven("https://jitpack.io/")
	exclusiveContent {
		forRepository { maven("https://api.modrinth.com/maven") }
		filter {
			includeGroup("maven.modrinth")
		}
	}
	exclusiveContent {
		forRepository { maven("https://cursemaven.com") }
		filter {
			includeGroup("curse.maven")
		}
	}
	maven("https://maven.blamejared.com")
	maven("https://maven.taumc.org/releases")
	maven("https://maven.terraformersmc.com/")
	maven("https://maven.blamejared.com/")
	maven("https://maven.parchmentmc.org")
}

dependencies {
	compileOnly("org.jetbrains:annotations:${"jetbrains_annotations_version"()}")

	compileOnly("systems.manifold:manifold-rt:${"manifold_version"()}")
	annotationProcessor("systems.manifold:manifold-exceptions:${"manifold_version"()}")
}

tasks.withType<JavaCompile> {
	if (name !in arrayOf("compileMcLauncherJava", "compilePatchedMcJava")) {
		options.encoding = "UTF-8"
		sourceCompatibility = "21"
		options.release = 21
		javaCompiler = javaToolchains.compilerFor {
			languageVersion = JavaLanguageVersion.of(21)
		}
		options.compilerArgs.addAll(arrayOf("-Xplugin:Manifold no-bootstrap"))
		options.forkOptions.jvmArgs?.add("-XX:+EnableDynamicAgentLoading")
	}
}

tasks.processResources {
	inputs.file(rootDir.resolve("gradle.properties"))
	inputs.property("version", project.version)

	filteringCharset = "UTF-8"

	val props = mutableMapOf<String, String>()
	props.putAll(project.properties
		.filterValues { value -> value is String }
		.mapValues { entry -> entry.value as String })
	props["mod_version"] = project.version as String

	filesMatching(listOf("fabric.mod.json", "mcmod.info", "META-INF/mods.toml", "META-INF/neoforge.mods.toml")) {
		expand(props)
	}

	if (modLoader != ModLoader.LEXFORGE) {
		exclude("pack.mcmeta")
	}

	doLast {
		if (modLoader == ModLoader.LEXFORGE || (modLoader == ModLoader.NEOFORGE && minecraftVersion < "20.5")) {
			fileTree(mapOf("dir" to tasks.processResources.get().outputs.files.asPath, "include" to "META-INF/neoforge.mods.toml")).onEach { file ->
				Files.copy(file.toPath(), kotlin.io.path.Path(outputs.files.asPath).resolve("META-INF/mods.toml"), StandardCopyOption.REPLACE_EXISTING)
				file.delete()
			}
		}
	}
}

tasks.withType<AbstractArchiveTask>().configureEach {
	isPreserveFileTimestamps = false
	isReproducibleFileOrder = true
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<GenerateModuleMetadata> {
	enabled = false
}

val shade: Configuration by configurations.creating {
	configurations.compileClasspath.get().extendsFrom(this)
	configurations.runtimeClasspath.get().extendsFrom(this)
}

val minecraftVersion = MinecraftVersion.get("minecraft_version"()) ?: error("Invalid `minecraft_version`!")
val modLoader = ModLoader.get("mod_loader"()) ?: error("Invalid `mod_loader`!")

unimined.minecraft {
	version(minecraftVersion.mojangName)

	runs.off = true
	
	when (modLoader) {
		ModLoader.NEOFORGE -> {
			neoForge {
				loader("mod_loader_version"())
			}
		}
		ModLoader.LEXFORGE -> {
			minecraftForge {
				loader("mod_loader_version"())
			}
		}
		else -> error("Invalid `mod_loader`!")
	}

	mappings {
		mojmap()
		parchment(mcVersion = minecraftVersion.mojangName, version = "parchment_version"())
	}
	
	mods {
		remap(shade)
	}

	defaultRemapJar = false
}

dependencies {
	shade("mezz.jei:jei-${minecraftVersion.mojangName}-${modLoader.commonName}:${"jei_version"()}")
}

tasks.jar {
	enabled = false
}

tasks.shadowJar {	
	configurations = emptyList()
	archiveClassifier = ""
	
	shade.resolve().forEach { file ->
		from(zipTree(file.absolutePath))
	}
	
	val included = listOf(
		"LICENSE_jei",
		"assets/jei/**",
		"META-INF/accesstransformer.cfg",
		"META-INF/mods.toml",
		"META-INF/neoforge.mods.toml",
		"mezz/jei/api/**",
		"mezz/jei/common/codecs/**/*.class", //
		"mezz/jei/common/config/BookmarkTooltipFeature.class",
		"mezz/jei/common/config/ClientToggleState.class",
		"mezz/jei/common/config/DebugConfig.class",
		"mezz/jei/common/config/IClientConfig.class",
		"mezz/jei/common/config/IClientToggleState*",
		"mezz/jei/common/config/IIngredientFilterConfig.class",
		"mezz/jei/common/config/IIngredientGridConfig.class",
		"mezz/jei/common/config/IJeiClientConfigs.class",
		"mezz/jei/common/config/file/**/*.class", //
		"mezz/jei/common/config/IngredientSortStage.class",
		"mezz/jei/common/config/GiveMode.class",
		"mezz/jei/common/config/RecipeSorterStage.class",
		"mezz/jei/common/Constants.class",
		"mezz/jei/common/gui/**/*.class",
		"mezz/jei/common/input/**/I*.class",
		"mezz/jei/common/Internal.class",
		"mezz/jei/common/input/ClickableIngredient.class",
		"mezz/jei/common/JeiFeatures.class",
		"mezz/jei/common/network/codecs/*.class",
		"mezz/jei/common/network/packets/*.class",
		"mezz/jei/common/network/I*.class",
		"mezz/jei/common/network/ServerPacketContext.class",
		"mezz/jei/common/platform/*.class",
		"mezz/jei/common/transfer/**/*.class",
		"mezz/jei/common/util/ErrorUtil.class",
		"mezz/jei/common/util/ExpandNewLineTextAcceptor.class",
		"mezz/jei/common/util/Immutable*2i.class",
		"mezz/jei/common/util/HorizontalAlignment.class",
		"mezz/jei/common/util/VerticalAlignment.class",
		"mezz/jei/common/util/NavigationVisibility.class",
		"mezz/jei/common/util/RegistryUtil.class",
		"mezz/jei/common/util/SafeIngredientUtil.class",
		"mezz/jei/common/util/StackHelper.class",
		"mezz/jei/common/util/StringUtil.class",
		"mezz/jei/common/util/TickTimer.class",
		"mezz/jei/common/util/Translator.class",
		"mezz/jei/core/collect/*MultiMap.class",
		"mezz/jei/core/collect/Table.class",
		"mezz/jei/core/search/SearchMode.class",
		"mezz/jei/core/util/function/LazySupplier.class",
		"mezz/jei/core/util/Pair.class",
		"mezz/jei/core/util/WeakList.class",
		"mezz/jei/library/color/**/*.class",
		"mezz/jei/library/config/ColorNameConfig.class",
		"mezz/jei/library/config/EditModeConfig*.class",
		"mezz/jei/library/config/*ModIdFormatConfig.class",
		"mezz/jei/library/focus/**/*.class",
		"mezz/jei/library/gui/**/*.class", //
//		"mezz/jei/library/gui/helpers/GuiHelper.class",
//		"mezz/jei/library/gui/ingredients/CycleTimer.class",
//		"mezz/jei/library/gui/ingredients/ICycler.class",
//		"mezz/jei/library/gui/recipes/layout/builder/RecipeSlotBuilder.class",
//		"mezz/jei/library/gui/recipes/layout/RecipeLayoutDrawableErrored.class",
//		"mezz/jei/library/gui/recipes/RecipeLayout.class",
		"mezz/jei/library/helpers/**/*.class",
		"mezz/jei/library/ingredients/**/*.class", //
		"mezz/jei/library/load/registration/*.class",
		"mezz/jei/library/plugins/jei/**/*.class",
		"mezz/jei/library/plugins/vanilla/**/*.class",
		"mezz/jei/library/render/**/*.class",
		"mezz/jei/library/recipes/CraftingExtensionHelper*.class",
		"mezz/jei/library/recipes/ExtendableRecipeCategoryHelper*.class",
		"mezz/jei/library/recipes/RecipeTransferManager.class",
		"mezz/jei/library/recipes/UniversalRecipeTransferHandlerAdapter.class",
		"mezz/jei/library/runtime/**/*.class",
		"mezz/jei/library/transfer/**/*.class",
		"mezz/jei/library/util/**/*.class",
		"mezz/jei/neoforge/platform/**/*.class",
		"mezz/jei/forge/platform/**/*.class",
		"mezz/jei/forge/ingredients/**/*.class",
	)
	val excluded = listOf(
		"mezz/jei/gui/plugins/**",
		"mezz/jei/**/package-info.*",
	)
	
	inputs.property("included", included.sorted().fold("") { out, value -> "${out}${value};" })
	inputs.property("excluded", excluded.sorted().fold("") { out, value -> "${out}${value};" })
	include(included)
	exclude(excluded)
}

tasks.assemble {
	dependsOn(tasks.shadowJar)
}

val jeiAPIJar: Configuration by configurations.creating {
	isCanBeConsumed = true
	isCanBeResolved = false
}

artifacts {
	add(jeiAPIJar.name, tasks.shadowJar)
}