# System Details

All benchmarks were performed in the following environment:

```
CPU: AMD Ryzen 9 5950x
RAM: 128 GB DDR4-3600
Disk: PCIe Gen 4 NVMe using BtrFS
OS: Arch Linux (Linux 6.14.5-arch1-1)
DE: KDE Plasma 6.3.5 (Wayland)
GLFW: aur/glfw-wayland-minecraft-cursorfix 3.4-4
JVM: aur/jdk21-temurin 21.0.6.u7-1
Launcher: extra/prismlauncher 9.4-1
JVM Flags: -XX:+UseShenandoahGC -XX:+UnlockExperimentalVMOptions -XX:+AlwaysPreTouch -XX:+UseStringDeduplication -Dfml.ignorePatchDiscrepancies=true -Dfml.ignoreInvalidMinecraftCertificates=true -XX:-OmitStackTraceInFastThrow -XX:+OptimizeStringConcat -Dfml.readTimeout=180
```

Three modpacks were tested:

- [Craftoria 1.21.2](https://legacy.curseforge.com/minecraft/modpacks/craftoria/files/6599850)
- [ATM10 3.2](https://legacy.curseforge.com/minecraft/modpacks/all-the-mods-10/files/6606327)
- [ATM9 1.0.8](https://legacy.curseforge.com/minecraft/modpacks/all-the-mods-9/files/6451428)

With the following modifications:

- [JEI 19.21.1.312](https://www.curseforge.com/minecraft/mc-mods/jei/files/6610838) (latest applicable version as of writing) was added to Craftoria for the JEMI benchmark.
- Included TMRV in Craftoria was replaced with latest dev build (for obvious reasons).
- [EMI 1.1.22](https://legacy.curseforge.com/minecraft/mc-mods/emi/files/6420931) ([LexForge link](https://legacy.curseforge.com/minecraft/mc-mods/emi/files/6420945)) was added to ATM10 and ATM9 (because those packs don't include EMI).
- `earlyWindowControl` was set to `false` in `.minecraft/config/fml.toml` for ATM9 to fix an issue with Wayland.
- Sodium Dynamic Lights and Sodium Options API were removed from ATM9 due to ethical concerns.
- [Zume](https://legacy.curseforge.com/minecraft/mc-mods/zume) was added to ATM10 and ATM9, and [Just Zoom](https://legacy.curseforge.com/minecraft/mc-mods/just-zoom) was disabled (because I felt like it).

All tests were performed using [TMRV 0.5.0-rc.2](https://github.com/Nolij/TooManyRecipeViewers/releases/tag/release/0.5.0-rc.2).

Craftoria and ATM10 were tested with an 8 GiB max heap size. ATM9 was tested with a 16 GiB max heap size.

# Steps

1. Configure instance
   1. Confirm EMI is enabled
   2. For JEMI: Ensure JEI is enabled and TMRV is disabled
   3. For TMRV: Ensure TMRV is enabled and JEI is disabled
2. Launch instance
3. Open world
4. Deselect Minecraft before world finishes loading (this will ensure that the game is paused immediately on load, which is necessary for reliably getting a comparable heap size, as the heap size constantly changes when the game is running)
5. Wait for EMI to finish loading (`Baked recipes after reload in ...ms` and `Reloaded EMI in ...ms` are both in logs)
6. Get memory usage using following shell command: `jmap -histo:live "$MC_PID" | tail -1 | awk '{ print $3; }' | numfmt --to=iec-i --suffix=B --format=%.3f;`
7. Quit game
8. Collect relevant logs using following RegEx: `/\[ModernFix\/\]: (Game took \d+\.\d+ seconds to start|Time from main menu to in\-game was \d+\.\d+ seconds|Total time to load game and open world was \d+\.\d+ seconds)|Starting JEI took \d+\.\d+ s|\[EMI\] ((Reloaded|Initialized) plugin from (jemi|toomanyrecipeviewers) in \d+ms|Baked \d+ recipes in \d+ms|Reloaded EMI in \d+ms|Baked recipes after reload in \d+ms)/g`
9. Determine load times from logs
   - For JEMI:
     - Pre-world-load time: JEI start time (`Starting JEI took {} s`) + `jemi` EMI plugin initialize time (`Reloaded plugin from jemi in {}ms`)
     - Post-world-load time: `jemi` EMI plugin register time (`Reloaded plugin from jemi in {}ms`)
   - For TMRV:
     - Pre-world-load time: `toomanyrecipeviewers` EMI plugin initialize time (`Initialized plugin from toomanyrecipeviewers in {}ms`)
     - Post-world-load time: `toomanyrecipeviewers` EMI plugin register time (`Reloaded plugin from toomanyrecipeviewers in {}ms`)

# Results

## Craftoria

#### TMRV

Load time: 2813ms (3ms before world load, 2810ms after world load)
Heap size: 2.916 GiB

Relevant logs:
```
[Render thread/WARN] [ModernFix/]: Game took 92.601 seconds to start
[Thread-102/INFO] [EMI/]: [EMI] Initialized plugin from toomanyrecipeviewers in 3ms
[Render thread/WARN] [ModernFix/]: Time from main menu to in-game was 24.87534 seconds
[Render thread/WARN] [ModernFix/]: Total time to load game and open world was 117.47633 seconds
[Thread-102/INFO] [EMI/]: [EMI] Reloaded plugin from toomanyrecipeviewers in 2810ms
[Thread-102/INFO] [EMI/]: [EMI] Baked 229568 recipes in 2824ms
[Thread-119/INFO] [EMI/]: [EMI] Baked recipes after reload in 2451ms
[Thread-102/INFO] [EMI/]: [EMI] Reloaded EMI in 28869ms
```

#### JEMI

Load time: 8048ms (6498ms before world load, 1550ms after world load)
Heap size: 3.066 GiB

Relevant logs:
```
[Render thread/WARN] [ModernFix/]: Game took 96.217 seconds to start
[Render thread/INFO] [mezz.jei.core.util.LoggedTimer/]: Starting JEI took 6.498 seconds
[Thread-108/INFO] [EMI/]: [EMI] Initialized plugin from jemi in 0ms
[Render thread/WARN] [ModernFix/]: Time from main menu to in-game was 29.895174 seconds
[Render thread/WARN] [ModernFix/]: Total time to load game and open world was 126.112175 seconds
[Thread-108/INFO] [EMI/]: [EMI] Reloaded plugin from jemi in 1550ms
[Thread-108/INFO] [EMI/]: [EMI] Baked 240506 recipes in 2756ms
[Thread-121/INFO] [EMI/]: [EMI] Baked recipes after reload in 2564ms
[Thread-108/INFO] [EMI/]: [EMI] Reloaded EMI in 26365ms
```

## ATM10

#### TMRV

Load time: 4943ms (2ms before world load, 4941ms after world load)
Heap size: 3.579 GiB

Relevant logs:
```
[Render thread/WARN] [ModernFix/]: Game took 97.878 seconds to start
[Thread-43/INFO] [EMI/]: [EMI] Initialized plugin from toomanyrecipeviewers in 2ms
[Render thread/WARN] [ModernFix/]: Time from main menu to in-game was 33.70959 seconds
[Render thread/WARN] [ModernFix/]: Total time to load game and open world was 131.58759 seconds
[Thread-43/INFO] [EMI/]: [EMI] Reloaded plugin from toomanyrecipeviewers in 4941ms
[Thread-43/INFO] [EMI/]: [EMI] Baked 329862 recipes in 6092ms
[Thread-47/INFO] [EMI/]: [EMI] Baked recipes after reload in 5947ms
[Thread-43/INFO] [EMI/]: [EMI] Reloaded EMI in 55608ms
```

#### JEMI

Load time: 16372ms (12700ms before world load, 3672ms after world load)
Heap size: 4.508 GiB

Relevant logs:
```
[Render thread/WARN] [ModernFix/]: Game took 98.161 seconds to start
[Render thread/INFO] [mezz.jei.core.util.LoggedTimer/]: Starting JEI took 12.70 s
[Thread-47/INFO] [EMI/]: [EMI] Initialized plugin from jemi in 0ms
[Render thread/WARN] [ModernFix/]: Time from main menu to in-game was 45.983204 seconds
[Render thread/WARN] [ModernFix/]: Total time to load game and open world was 144.14421 seconds
[Thread-47/INFO] [EMI/]: [EMI] Reloaded plugin from jemi in 3672ms
[Thread-47/INFO] [EMI/]: [EMI] Baked 411599 recipes in 7283ms
[Thread-54/INFO] [EMI/]: [EMI] Baked recipes after reload in 9847ms
[Thread-47/INFO] [EMI/]: [EMI] Reloaded EMI in 62028ms
```

## ATM9

#### TMRV

Load time: 36973ms (1ms before world load, 36972ms after world load)
Heap size: 4.738 GiB

Relevant logs:
```
[Render thread/WARN] [ModernFix/]: Game took 141.101 seconds to start
[Thread-40/INFO] [EMI/]: [EMI] Initialized plugin from toomanyrecipeviewers in 1ms
[Render thread/WARN] [ModernFix/]: Time from main menu to in-game was 68.5168 seconds
[Render thread/WARN] [ModernFix/]: Total time to load game and open world was 209.6178 seconds
[Thread-40/INFO] [EMI/]: [EMI] Reloaded plugin from toomanyrecipeviewers in 36972ms
[Thread-40/INFO] [EMI/]: [EMI] Baked 442489 recipes in 3994ms
[Thread-43/INFO] [EMI/]: [EMI] Baked recipes after reload in 3794ms
[Thread-40/INFO] [EMI/]: [EMI] Reloaded EMI in 88587ms
```

#### JEMI

Load time: 47790ms (41220ms before world load, 6570ms after world load)
Heap size: 5.627 GiB

Relevant logs:
```
[Render thread/WARN] [ModernFix/]: Game took 142.08 seconds to start
[Render thread/INFO] [mezz.jei.core.util.LoggedTimer/]: Starting JEI took 41.22 s
[Thread-41/INFO] [EMI/]: [EMI] Initialized plugin from jemi in 0ms
[Render thread/WARN] [ModernFix/]: Time from main menu to in-game was 110.990456 seconds
[Render thread/WARN] [ModernFix/]: Total time to load game and open world was 253.07047 seconds
[Thread-41/INFO] [EMI/]: [EMI] Reloaded plugin from jemi in 6570ms
[Thread-41/INFO] [EMI/]: [EMI] Baked 469794 recipes in 12386ms
[Thread-41/INFO] [EMI/]: [EMI] Reloaded EMI in 67885ms
[Thread-44/INFO] [EMI/]: [EMI] Baked recipes after reload in 13345ms
```
