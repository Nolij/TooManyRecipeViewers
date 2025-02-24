import kotlinx.serialization.encodeToString
import me.modmuss50.mpp.HttpUtils
import me.modmuss50.mpp.PublishModTask
import me.modmuss50.mpp.ReleaseType
import me.modmuss50.mpp.platforms.discord.DiscordAPI
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.eclipse.jgit.lib.Ref
import org.taumc.gradle.compression.DeflateAlgorithm
import org.taumc.gradle.compression.task.AdvzipTask
import org.taumc.gradle.versioning.ReleaseChannel
import xyz.wagyourtail.unimined.api.unimined
import java.nio.file.Files

plugins {
	id("java")
	id("maven-publish")
	id("com.gradleup.shadow")
	id("xyz.wagyourtail.unimined")
	id("com.github.gmazzo.buildconfig")
	id("org.taumc.gradle.versioning")
	id("org.taumc.gradle.compression")
	id("me.modmuss50.mod-publish-plugin")
	id("org.ajoberstar.grgit")
}

operator fun String.invoke(): String = rootProject.properties[this] as? String ?: error("Property $this not found")

rootProject.group = "maven_group"()
rootProject.version = tau.versioning.version("mod_version"(), rootProject.properties["release_channel"], "jei.${"jei_version"()}")

println("TooManyRecipeViewers version: ${rootProject.version}")

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
	inputs.property("version", rootProject.version)

	filteringCharset = "UTF-8"

	val props = mutableMapOf<String, String>()
	props.putAll(rootProject.properties
		.filterValues { value -> value is String }
		.mapValues { entry -> entry.value as String })
	props["mod_version"] = rootProject.version as String

	filesMatching(listOf("fabric.mod.json", "mcmod.info", "META-INF/mods.toml", "META-INF/neoforge.mods.toml")) {
		expand(props)
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
	version("minecraft_version"())

	neoForge {
		loader("neoforge_version"())
		mixinConfig("toomanyrecipeviewers.mixins.json")
	}

	mappings {
		mojmap()
		parchment(mcVersion = "minecraft_version"(), version = "parchment_version"())
	}

	defaultRemapJar = false
}

dependencies {
	val minecraftLibraries by configurations.getting
	minecraftLibraries.isTransitive = true
	
	shade("dev.nolij:libnolij:${"libnolij_version"()}")
	minecraftLibraries("dev.nolij:libnolij:${"libnolij_version"()}")
	
	implementation("dev.emi:emi-neoforge:${"emi_version"()}")
	
	shade(project(":jei-api"))
	
	// for testing purposes
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
	runtimeOnly("curse.maven:ars-nouveau-401955:6123623")
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
}

tasks.jar {
	enabled = false
}

val sourcesJar by tasks.registering(Jar::class) {
	group = "build"

	archiveClassifier = "sources"

	from("LICENSE") {
		rename { "${it}_${"mod_id"()}" }
	}

	listOf(rootProject.sourceSets, project(":jei-api").sourceSets).flatten().forEach {
		from(it.allSource) { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }
	}
}

tasks.shadowJar {
	from("LICENSE") {
		rename { "${it}_${"mod_id"()}" }
	}

	configurations = listOf(shade)
	archiveClassifier = ""

	relocate("dev.nolij.libnolij", "dev.nolij.toomanyrecipeviewers.libnolij")
}

val inputJar = tasks.shadowJar

val compressJar = tau.compression.compress<AdvzipTask>(inputJar, "compressJar") {
	level = DeflateAlgorithm.EXTRA
	throwIfNotInstalled = tau.versioning.isRelease
}

val outputJar = compressJar

tasks.assemble {
	dependsOn(outputJar, sourcesJar)
}

afterEvaluate {
	publishing {
		repositories {
			if (!System.getenv("local_maven_url").isNullOrEmpty())
				maven(System.getenv("local_maven_url"))
		}

		publications {
			create<MavenPublication>("mod_id"()) {
				artifact(tasks.shadowJar.get().archiveFile)
				artifact(sourcesJar)
			}
		}
	}

	tasks.withType<AbstractPublishToMaven> {
		dependsOn(outputJar, sourcesJar)
	}

	fun getChangelog(): String {
		return file("CHANGELOG.md").readText()
	}

	publishMods {
		file = outputJar.archiveFile
		additionalFiles.from(sourcesJar.get().archiveFile)
		type = if (tau.versioning.releaseChannel == ReleaseChannel.RELEASE) ReleaseType.STABLE else ReleaseType.ALPHA
		displayName = tau.versioning.versionNoMetadata
		version = tau.versioning.version
		changelog = getChangelog()

		modLoaders.addAll("neoforge")
		dryRun = !tau.versioning.isRelease

		val branchName = grgit.branch.current().name!!

		github {
			accessToken = providers.environmentVariable("GITHUB_TOKEN")
			repository = "Nolij/TooManyRecipeViewers"
			commitish = branchName
			tagName = tau.versioning.releaseTag
		}

		if (dryRun.get() || tau.versioning.releaseChannel == ReleaseChannel.RELEASE) {
			curseforge {
				val cfAccessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
				accessToken = cfAccessToken
				projectId = "1194921"
				projectSlug = "tmrv"

				minecraftVersions.add("1.21.1")
				
				requires("emi")
				incompatible("jei")
			}

			discord {
				webhookUrl = providers.environmentVariable("DISCORD_WEBHOOK").orElse("")

				username = "TooManyRecipeViewers Releases"

				avatarUrl = "https://github.com/Nolij/TooManyRecipeViewers/raw/master/src/main/resources/icon.png"

				content = changelog.map { changelog ->
					"# TooManyRecipeViewers ${tau.versioning.version} has been released!\nChangelog: ```md\n${changelog}\n```"
				}

				setPlatforms(platforms["github"], platforms["curseforge"])
			}
		}
	}

	tasks.withType<PublishModTask> {
		dependsOn(outputJar, sourcesJar)
	}

	tasks.publishMods {
		doLast {
			if (!publishMods.dryRun.get() && tau.versioning.releaseChannel != ReleaseChannel.RELEASE) {
				val http = HttpUtils()

				val currentTag: Ref? = tau.versioning.releaseTags.firstOrNull()
				val buildChangeLog =
					grgit.log {
						if (currentTag != null)
							excludes = listOf(currentTag.name)
						includes = listOf("HEAD")
					}.joinToString("\n") { commit ->
						val id = commit.abbreviatedId
						val message = commit.fullMessage.substringBefore('\n').trim()
						val author = commit.author.name
						"- [${id}] $message (${author})"
					}

				val compareStart = currentTag?.name ?: grgit.log().minBy { it.dateTime }.id
				val compareEnd = tau.versioning.releaseTag
				val compareLink = "https://github.com/Nolij/TooManyRecipeViewers/compare/${compareStart}...${compareEnd}"

				val webhookUrl = providers.environmentVariable("DISCORD_WEBHOOK")
				val releaseChangeLog = getChangelog()
				val file = publishMods.file.asFile.get()

				var content = "# [TooManyRecipeViewers Test Build ${publishMods.displayName.get()}]" +
						"(<https://github.com/Nolij/TooManyRecipeViewers/releases/tag/${tau.versioning.releaseTag}>) has been released!\n" +
						"Changes since last build: <${compareLink}>"

				if (buildChangeLog.isNotBlank())
					content += " ```\n${buildChangeLog}\n```"
				content += "\nChanges since last release: ```md\n${releaseChangeLog}\n```"

				val webhook = DiscordAPI.Webhook(
					content,
					"TooManyRecipeViewers Test Builds",
					"https://github.com/Nolij/TooManyRecipeViewers/raw/master/src/main/resources/icon.png"
				)

				val bodyBuilder = MultipartBody.Builder()
					.setType(MultipartBody.FORM)
					.addFormDataPart("payload_json", http.json.encodeToString(webhook))
					.addFormDataPart("files[0]", file.name, file.asRequestBody("application/java-archive".toMediaTypeOrNull()))

				var fileIndex = 1
				for (additionalFile in publishMods.additionalFiles) {
					bodyBuilder.addFormDataPart(
						"files[${fileIndex++}]",
						additionalFile.name,
						additionalFile.asRequestBody(Files.probeContentType(additionalFile.toPath()).toMediaTypeOrNull())
					)
				}

				val requestBuilder = Request.Builder()
					.url(webhookUrl.get())
					.post(bodyBuilder.build())
					.header("Content-Type", "multipart/form-data")

				val request = requestBuilder.build()
				val call = http.httpClient.newCall(request)
				val response = call.execute()
				if (!response.isSuccessful)
					error(response.toString())
				response.close()
			}
		}
	}
}