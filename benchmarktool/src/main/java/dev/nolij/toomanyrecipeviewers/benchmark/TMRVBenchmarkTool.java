package dev.nolij.toomanyrecipeviewers.benchmark;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mizosoft.methanol.Methanol;
import org.taumc.launcher.core.launch.RuntimeInstance;
import org.taumc.launcher.core.meta.json.MMCPack;
import org.taumc.launcher.core.meta.json.PatchedPrismMetaRepository;
import org.taumc.launcher.core.mods.curseforge.CurseForgeAPI;
import org.taumc.launcher.core.mods.curseforge.CurseForgeInstanceCreator;
import org.taumc.launcher.core.mods.curseforge.File;
import org.taumc.launcher.core.nio.PathUtils;
import org.taumc.launcher.core.progress.ProgressProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class TMRVBenchmarkTool {
    public record LogString(String pattern, StatType stat) {
        enum StatType {
            MAIN_MENU_LOAD_TIME,
            WORLD_LOAD_TIME,
            TOTAL_LOAD_TIME,
            RECIPE_VIEWER_START_TIME,
            JEI_COMPAT_PLUGIN_RELOAD_TIME,
            JEI_COMPAT_PLUGIN_INIT_TIME;

            public static final int NUM_STAT_TYPES = StatType.values().length;
        }
    }
    private static final List<LogString> LOG_STRINGS = List.of(
            new LogString("[ModernFix/]: Game took", LogString.StatType.MAIN_MENU_LOAD_TIME),
            new LogString("Time from main menu to in-game", LogString.StatType.WORLD_LOAD_TIME),
            new LogString("Total time to load game and open world was", LogString.StatType.TOTAL_LOAD_TIME),
            new LogString("Starting JEI took", LogString.StatType.RECIPE_VIEWER_START_TIME),
            new LogString("Reloaded EMI in", LogString.StatType.RECIPE_VIEWER_START_TIME),
            new LogString("Baked recipes after reload in", null),
            new LogString("Reloaded plugin from jemi", LogString.StatType.JEI_COMPAT_PLUGIN_RELOAD_TIME),
            new LogString("Reloaded plugin from toomanyrecipeviewers", LogString.StatType.JEI_COMPAT_PLUGIN_RELOAD_TIME),
            new LogString("Initialized plugin from jemi", LogString.StatType.JEI_COMPAT_PLUGIN_INIT_TIME),
            new LogString("Initialized plugin from toomanyrecipeviewers", LogString.StatType.JEI_COMPAT_PLUGIN_INIT_TIME)
    );

    public static void main(String[] args) throws Exception {
        Path path = Files.createTempDirectory("tmrv-benchmark");
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    PathUtils.deleteRecursively(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        var creator = new CurseForgeInstanceCreator(CurseForgeAPI.INSTANCE, new CurseForgeInstanceCreator.ManualDownloadService() {
            @Override
            public void trackFileForManualDownload(Download download) {
                throw new UnsupportedOperationException();
            }

            @Override
            public CompletableFuture<Void> downloadManualFiles() {
                return CompletableFuture.completedFuture(null);
            }
        });
        if (true) {
            // ATM10
            creator.createInstance(path, 925200, 6696915, ProgressProvider.LOGGING);
        } else {
            buildSimpleInstance(path);
        }
        customizeInstance(path);
        RuntimeInstance instance = new RuntimeInstance();
        instance.getMetadataService().addRepository(new PatchedPrismMetaRepository());
        instance.getMetadataService().updateIndex();
        var mmcPack = MMCPack.read(path.resolve("mmc-pack.json"));
        instance.addComponents(mmcPack.components());
        instance.setInstancePath(path.resolve("minecraft"));
        instance.setMinimumMemoryMB(4096);
        instance.setMaximumMemoryMB(8192);
        instance.launch();
        var process = instance.getCurrentProcess();
        try {
            runBenchmarking(process);
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }

    private static void buildSimpleInstance(Path path) throws Exception {
        MMCPack pack = new MMCPack(1, List.of(new MMCPack.Component("net.minecraft", "1.21.1"), new MMCPack.Component("net.neoforged", "21.1.186")));
        new ObjectMapper().writeValue(path.resolve("mmc-pack.json").toFile(), pack);
        // ModernFix
        injectMod(path, CurseForgeAPI.INSTANCE.getModFile(790626, 6609557).join());
        // JEI
        injectMod(path, CurseForgeAPI.INSTANCE.getModFile(238222, 5846880).join());
    }

    private static void customizeInstance(Path path) throws Exception {
        // Add EMI
        injectMod(path, CurseForgeAPI.INSTANCE.getModFile(580555, 6420931).join());
    }

    private static void injectMod(Path path, File cfFile) throws Exception {
        Path fileDest = path.resolve("minecraft").resolve("mods").resolve(cfFile.fileName());
        Files.createDirectories(fileDest.getParent());
        System.out.println("Download " + cfFile.fileName());
        try (Methanol client = Methanol.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build()) {
            client.send(HttpRequest.newBuilder().uri(URI.create(cfFile.downloadUrl())).build(), HttpResponse.BodyHandlers.ofFile(fileDest));
        }
    }

    private static void runBenchmarking(Process process) throws Exception {
        var stdout = process.getInputStream();
        var lines = new HashMap<LogString.StatType, List<String>>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stdout))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String finalLine = line;
                var opt = LOG_STRINGS.stream().filter(l -> finalLine.contains(l.pattern)).findFirst();
                if (opt.isPresent()) {
                    var logString = opt.get();
                    lines.computeIfAbsent(logString.stat(), $ -> new ArrayList<>()).add(finalLine);
                    if (logString.stat() != null) {
                        System.out.println("Captured stat " + logString.stat().name());
                    }
                    String remainingStats = Arrays.stream(LogString.StatType.values()).filter(s -> !lines.containsKey(s)).map(LogString.StatType::name).collect(Collectors.joining(", "));
                    if (remainingStats.isBlank()) {
                        int expectedRecipeViewerCount = 1;
                        boolean jeiInstalled = lines.get(LogString.StatType.RECIPE_VIEWER_START_TIME).stream().anyMatch(s -> s.contains("JEI"));
                        if (jeiInstalled) {
                            expectedRecipeViewerCount++;
                        }
                        if (lines.get(LogString.StatType.RECIPE_VIEWER_START_TIME).size() == expectedRecipeViewerCount) {
                            break;
                        } else {
                            System.out.println("Waiting for second recipe viewer to finish loading");
                        }
                    } else {
                        System.out.println("Waiting for stats " + remainingStats);
                    }
                }
            }
        }
        System.out.println("Collecting final info");
        long heap = captureHeapUsage(process);
        process.destroyForcibly();
        System.out.println();
        System.out.println("Relevant logs:");
        lines.values().stream().flatMap(Collection::stream).forEachOrdered(System.out::println);
        System.out.println("Heap usage: " + formatBinary(heap));
    }

    private static String formatBinary(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log10(bytes) / Math.log10(1024));
        String prefix = "KMGTPE".charAt(exp - 1) + "i";
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), prefix);
    }

    private static long captureHeapUsage(Process process) throws Exception {
        ProcessBuilder builder = new ProcessBuilder("jmap", "-histo:live", String.valueOf(process.pid()));
        var jcmd = builder.start();
        String lastLine = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(jcmd.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lastLine = line;
            }
        }
        if (jcmd.waitFor() != 0) {
            throw new IllegalStateException("jmap exited with status " + jcmd.exitValue());
        }
        if (lastLine == null) {
            throw new IllegalStateException("Did not see expected output");
        }
        String[] fields = lastLine.split(" ");
        return Long.parseLong(fields[fields.length - 1]);
    }
}
