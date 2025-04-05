import org.taumc.gradle.compression.DeflateAlgorithm
import org.taumc.gradle.compression.task.AdvzipTask
import org.taumc.gradle.minecraft.MinecraftVersion
import org.taumc.gradle.minecraft.ModEnvironment
import org.taumc.gradle.minecraft.ModLoader
import org.taumc.gradle.publishing.api.artifact.Relation
import org.taumc.gradle.publishing.publishing
import xyz.wagyourtail.jvmdg.gradle.task.DowngradeJar
import xyz.wagyourtail.jvmdg.gradle.task.ShadeJar
import xyz.wagyourtail.unimined.api.minecraft.task.RemapJarTask
import xyz.wagyourtail.unimined.api.unimined
import java.nio.file.Files
import java.nio.file.StandardCopyOption

plugins {
	id("java")
	id("idea")
	id("maven-publish")
	id("com.gradleup.shadow")
	id("xyz.wagyourtail.unimined")
	id("com.github.gmazzo.buildconfig")
	id("org.taumc.gradle.versioning")
	id("org.taumc.gradle.compression")
	id("xyz.wagyourtail.jvmdowngrader")
}

operator fun String.invoke(): String = project.properties[this] as? String ?: error("Property $this not found")

idea.module {
	isDownloadJavadoc = true
	isDownloadSources = true
}

project.group = "maven_group"()
project.version = tau.versioning.version("mod_version"(), project.properties["release_channel"], "jei.${"jei_version"()}")

println("TooManyRecipeViewers version: ${project.version}")

buildConfig {
	className("TooManyRecipeViewersConstants")
	packageName("dev.nolij.toomanyrecipeviewers")

	useJavaOutput()

	buildConfigField("MOD_ID", "mod_id"())
}

base {
	archivesName = "mod_id"()
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
	maven("https://maven.parchmentmc.org")
}

dependencies {
	compileOnly("org.jetbrains:annotations:${"jetbrains_annotations_version"()}")
	
	compileOnly("systems.manifold:manifold-rt:${"manifold_version"()}")
	annotationProcessor("systems.manifold:manifold-exceptions:${"manifold_version"()}")
}

val minecraftVersion = MinecraftVersion.get("minecraft_version"()) ?: error("Invalid `minecraft_version`!")
val modLoader = ModLoader.get("mod_loader"()) ?: error("Invalid `mod_loader`!")
val javaVersion = JavaVersion.valueOf("java_version"())

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
	inputs.property("version", tau.versioning.version)
	inputs.property("mod_loader", modLoader.commonName)

	filteringCharset = "UTF-8"

	val props = mutableMapOf<String, String>()
	props.putAll(project.properties
		.filterValues { value -> value is String }
		.mapValues { entry -> entry.value as String })
	props["mod_version"] = tau.versioning.version
	props["mod_loader"] = modLoader.commonName
	props["mixin_java_version"] = (project.properties["java_version"] as String).replace("VERSION_", "")

	filesMatching(listOf("fabric.mod.json", "mcmod.info", "META-INF/mods.toml", "META-INF/neoforge.mods.toml", "*.mixins.json")) {
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
	
	// TODO: yell at wyt to fix Unimined
	includeEmptyDirs = false
	exclude("assets/minecraft/textures/**")
}

tasks.withType<GenerateModuleMetadata> {
	enabled = false
}

val shade: Configuration by configurations.creating {
	configurations.compileClasspath.get().extendsFrom(this)
	configurations.runtimeClasspath.get().extendsFrom(this)
}

unimined.minecraft {
	version(minecraftVersion.mojangName)

	runs {
		config("client") {
			javaVersion = JavaVersion.VERSION_21
			jvmArgs("-Xmx4G")
		}
	}

	when (modLoader) {
		ModLoader.NEOFORGE -> {
			neoForge {
				loader("mod_loader_version"())
				mixinConfig("toomanyrecipeviewers.mixins.json")
			}
		}
		ModLoader.LEXFORGE -> {
			minecraftForge {
				loader("mod_loader_version"())
				mixinConfig("toomanyrecipeviewers.mixins.json")
			}
		}
		else -> error("Invalid `mod_loader`!")
	}

	mappings {
		mojmap()
		parchment(mcVersion = minecraftVersion.mojangName, version = "parchment_version"())
	}
}

dependencies {
	val minecraftLibraries by configurations.getting
	minecraftLibraries.isTransitive = true
	val modImplementation by configurations.getting
	val include by configurations.getting
	
	shade("dev.nolij:libnolij:${"libnolij_version"()}")
	minecraftLibraries("dev.nolij:libnolij:${"libnolij_version"()}")
	
	if (minecraftVersion >= "20.2")
		implementation("dev.emi:emi-${modLoader.commonName}:${"emi_version"()}")
	else
		modImplementation("dev.emi:emi-${modLoader.commonName}:${"emi_version"()}")
	
	shade(project(stonecutter.node.sibling("jei-api")!!.project.path, configuration = "jeiAPIJar")) { isTransitive = false }
	
	// for testing purposes
	if (minecraftVersion.equals("21.1") && modLoader == ModLoader.NEOFORGE) {
		runtimeOnly("maven.modrinth:just-enough-professions-jep:4.0.3")
		runtimeOnly("maven.modrinth:justenoughbreeding:mxmXy9Cs")
		runtimeOnly("maven.modrinth:just-enough-effect-descriptions-jeed:m7gSD9ey")
		runtimeOnly("maven.modrinth:mekanism:10.7.8.70")
		runtimeOnly("maven.modrinth:mekanism-generators:10.7.8.70")
		runtimeOnly("maven.modrinth:mekanism-additions:10.7.8.70")
		runtimeOnly("maven.modrinth:mekanism-tools:10.7.8.70")
		runtimeOnly("maven.modrinth:mekanism_extra:1.21.1-1.0.5")
		runtimeOnly("curse.maven:mekanism-weapons-929829:5906398")
		runtimeOnly("maven.modrinth:just-enough-mekanism-multiblocks:7.2")
		runtimeOnly("maven.modrinth:actually-additions:1.3.12")
		runtimeOnly("curse.maven:placebo-283644:6068449")
		runtimeOnly("curse.maven:apotheosis-313970:6078226")
		runtimeOnly("curse.maven:apothic-attributes-898963:6060907")
		runtimeOnly("curse.maven:apothic-enchanting-1063926:6084297")
		runtimeOnly("curse.maven:apothic-spawners-986583:6058055")
		runtimeOnly("maven.modrinth:c2me-neoforge:0.3.0+alpha.0.47+1.21.1")
		runtimeOnly("curse.maven:sophisticated-core-618298:6218335")
		runtimeOnly("curse.maven:sophisticated-storage-619320:6217937")
		runtimeOnly("curse.maven:sophisticated-backpacks-422301:6218333")
		runtimeOnly("curse.maven:sophisticated-storage-in-motion-1166930:6202352")
		runtimeOnly("curse.maven:moderately-enough-effect-descriptions-meed-918638:6100615")
		runtimeOnly("maven.modrinth:geckolib:oNBe6h9g")
		runtimeOnly("maven.modrinth:curios:9.2.2+1.21.1")
		runtimeOnly("curse.maven:ars-nouveau-401955:6228434")
		runtimeOnly("curse.maven:polymorph-388800:5995380")
		runtimeOnly("curse.maven:corail-tombstone-243707:6171577")
		runtimeOnly("curse.maven:laserio-626839:5730007")
		runtimeOnly("curse.maven:chipped-456956:5813117")
		runtimeOnly("curse.maven:resourceful-lib-570073:5793500")
		runtimeOnly("curse.maven:athena-841890:5629395")
		runtimeOnly("curse.maven:farmers-delight-398521:6154807")
		runtimeOnly("curse.maven:fruits-delight-943774:6095147")
		runtimeOnly("curse.maven:puzzles-lib-495476:6095894")
		runtimeOnly("curse.maven:visual-workbench-500273:5714956")
		runtimeOnly("curse.maven:immersive-engineering-231951:5828000")
		runtimeOnly("curse.maven:guideme-1173950:6223759")
		runtimeOnly("curse.maven:applied-energistics-2-223794:6225422")
		runtimeOnly("curse.maven:ae2-jei-integration-1074338:5748513")
		runtimeOnly("curse.maven:blockui-522992:6150484")
		runtimeOnly("curse.maven:domum-ornamentum-527361:5764083")
		runtimeOnly("curse.maven:multi-piston-303278:5783614")
		runtimeOnly("curse.maven:structurize-298744:6220899")
		runtimeOnly("curse.maven:towntalk-900364:5653504")
		runtimeOnly("curse.maven:minecolonies-245506:6195917")
		runtimeOnly("curse.maven:playeranimator-658587:6024462")
		runtimeOnly("curse.maven:irons-spells-n-spellbooks-855414:6197625")
		runtimeOnly("maven.modrinth:jei-multiblocks:1.21.1-1.0.4")
	} else if (minecraftVersion.equals("20.1") && modLoader == ModLoader.LEXFORGE) {
		compileOnly("io.github.llamalad7:mixinextras-common:${"mixinextras_version"()}")
		include("io.github.llamalad7:mixinextras-forge:${"mixinextras_version"()}")
		implementation("io.github.llamalad7:mixinextras-forge:${"mixinextras_version"()}")
	}
}

val sourcesJar by tasks.registering(Jar::class) {
	group = "build"

	archiveClassifier = "sources"

	from(rootProject.file("LICENSE")) {
		rename { "${it}_${"mod_id"()}" }
	}

	listOf(project.sourceSets, stonecutter.node.sibling("jei-api")!!.project.sourceSets).flatten().forEach {
		from(it.allSource) { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }
	}
}

tasks.shadowJar {
	from(rootProject.file("LICENSE")) {
		rename { "${it}_${"mod_id"()}" }
	}
	
	exclude("mezz/jei/common/platform/JEIAPIStub.class")

	configurations = listOf(shade)
	archiveClassifier = "unremapped"

	relocate("dev.nolij.libnolij", "dev.nolij.toomanyrecipeviewers.libnolij")
}

tasks.named<RemapJarTask>("remapJar") {
	inputFile.set(tasks.shadowJar.get().archiveFile)

	asJar.archiveClassifier = "remapped"

	mixinRemap {
		enableMixinExtra()
		disableRefmap()
	}
}
val inputJar = tasks.getByName("remapJar") as AbstractArchiveTask

val compressionInputJar = if (javaVersion.ordinal < JavaVersion.VERSION_21.ordinal) {
	val downgradeJar = tasks.named<DowngradeJar>("downgradeJar") {
		inputFile = inputJar.archiveFile
		downgradeTo = javaVersion
		archiveClassifier = "downgraded"
	}

	val shadeDowngradedJar by tasks.named<ShadeJar>("shadeDowngradedApi") {
		inputFile = downgradeJar.get().archiveFile
		archiveClassifier = "downgraded-shaded"
	}

	shadeDowngradedJar
} else {
	inputJar
}

// Cannot use CompressionExtension.compress because the finalizedBy/dependency logic makes chiseled builds deadlock
val compressJar = tasks.register<AdvzipTask>("compressJar") {
	inputJar = compressionInputJar.archiveFile
	destinationDirectory = compressionInputJar.destinationDirectory
	level = DeflateAlgorithm.EXTRA
	throwIfNotInstalled = tau.versioning.isRelease
	archiveClassifier = ""
	dependsOn(compressionInputJar)
}

val outputJar = compressJar

tasks.assemble {
	dependsOn(outputJar, sourcesJar)
}

rootProject.tau.publishing.modArtifact {
	files(outputJar.get().archiveFile, provider { sourcesJar.get().archiveFile })

	minecraftVersionRange = minecraftVersion.mojangName
	javaVersions.add(javaVersion)

	environment = ModEnvironment.CLIENT_ONLY
	modLoaders.add(modLoader)

	relations.add(Relation(id = "emi", type = Relation.Type.REQUIRES))
	relations.add(Relation(id = "jei", type = Relation.Type.BREAKS))
}