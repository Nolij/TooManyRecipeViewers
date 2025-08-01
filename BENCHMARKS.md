# System Details

All benchmarks were performed in the following environment:

```
CPU: AMD Ryzen 9 5950x
RAM: 128 GB DDR4-3600
Disk: PCIe Gen 4 NVMe using BtrFS
OS: Arch Linux (Linux 6.15.2-arch1-1)
DE: KDE Plasma 6.3.5 (Wayland)
GLFW: aur/glfw-wayland-minecraft-cursorfix 3.4-6
JVM: aur/jdk21-temurin 21.0.7.u6-1
Launcher: extra/prismlauncher 9.4-1
JVM Flags: -XX:+UseShenandoahGC -XX:+UnlockExperimentalVMOptions -XX:+AlwaysPreTouch -XX:+UseStringDeduplication -Dfml.ignorePatchDiscrepancies=true -Dfml.ignoreInvalidMinecraftCertificates=true -XX:-OmitStackTraceInFastThrow -XX:+OptimizeStringConcat -Dfml.readTimeout=180
```

Three modpacks were tested:

- [Craftoria 1.22.2](https://legacy.curseforge.com/minecraft/modpacks/craftoria/files/6707705)
- [ATM10 4.2](https://legacy.curseforge.com/minecraft/modpacks/all-the-mods-10/files/6696915)
- [ATM9 1.0.8](https://legacy.curseforge.com/minecraft/modpacks/all-the-mods-9/files/6451428)

With the following modifications:

- [JEI 19.21.2.313](https://legacy.curseforge.com/minecraft/mc-mods/jei/files/6614392) (latest applicable version as of writing) was added to Craftoria for the JEMI benchmark.
- Included TMRV in Craftoria was replaced with latest dev build (for obvious reasons).
- [EMI 1.1.22](https://legacy.curseforge.com/minecraft/mc-mods/emi/files/6420931) ([LexForge link](https://legacy.curseforge.com/minecraft/mc-mods/emi/files/6420945)) was added to ATM10 and ATM9 (because those packs don't include EMI).
- `earlyWindowControl` was set to `false` in `.minecraft/config/fml.toml` for ATM9 to fix an issue with Wayland.
- Sodium Dynamic Lights and Sodium Options API were removed from ATM9 due to ethical concerns.
- [Zume](https://legacy.curseforge.com/minecraft/mc-mods/zume) was added to ATM10 and ATM9, and [Just Zoom](https://legacy.curseforge.com/minecraft/mc-mods/just-zoom) was disabled (because I felt like it).

All tests were performed using [TMRV 0.6.0-rc.2](https://github.com/Nolij/TooManyRecipeViewers/releases/tag/release/0.6.0-rc.2).

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

Load time: 3201ms (2ms before world load, 3199ms after world load)
Heap size: 2.722 GiB

Relevant logs:
```
[Render thread/WARN] [ModernFix/]: Game took 93.907 seconds to start
[Thread-108/INFO] [EMI/]: [EMI] Initialized plugin from toomanyrecipeviewers in 2ms
[Render thread/WARN] [ModernFix/]: Time from main menu to in-game was 26.39167 seconds
[Render thread/WARN] [ModernFix/]: Total time to load game and open world was 120.29867 seconds
[Thread-108/INFO] [EMI/]: [EMI] Reloaded plugin from toomanyrecipeviewers in 3199ms
[Thread-108/INFO] [EMI/]: [EMI] Baked 246939 recipes in 2784ms
[Thread-126/INFO] [EMI/]: [EMI] Baked recipes after reload in 2054ms
[Thread-108/INFO] [EMI/]: [EMI] Reloaded EMI in 27539ms
```

#### JEMI

Load time: 8277ms (6751ms before world load, 1526ms after world load)
Heap size: 2.872 GiB

Relevant logs:
```
[Render thread/WARN] [ModernFix/]: Game took 93.449 seconds to start
[Render thread/INFO] [mezz.jei.core.util.LoggedTimer/]: Starting JEI took 6.751 seconds
[Thread-113/INFO] [EMI/]: [EMI] Initialized plugin from jemi in 0ms
[Render thread/WARN] [ModernFix/]: Time from main menu to in-game was 30.508913 seconds
[Render thread/WARN] [ModernFix/]: Total time to load game and open world was 123.95791 seconds
[Thread-113/INFO] [EMI/]: [EMI] Reloaded plugin from jemi in 1526ms
[Thread-113/INFO] [EMI/]: [EMI] Baked 243195 recipes in 2499ms
[Thread-124/INFO] [EMI/]: [EMI] Baked recipes after reload in 2419ms
[Thread-113/INFO] [EMI/]: [EMI] Reloaded EMI in 24017ms
```

## ATM10

#### TMRV

Load time: 7484ms (2ms before world load, 7482ms after world load)
Heap size: 3.580 GiB

Relevant logs:
```
[Render thread/WARN] [ModernFix/]: Game took 99.155 seconds to start
[Thread-43/INFO] [EMI/]: [EMI] Initialized plugin from toomanyrecipeviewers in 2ms
[Render thread/WARN] [ModernFix/]: Time from main menu to in-game was 35.92763 seconds
[Render thread/WARN] [ModernFix/]: Total time to load game and open world was 135.08263 seconds
[Thread-43/INFO] [EMI/]: [EMI] Reloaded plugin from toomanyrecipeviewers in 7482ms
[Thread-43/INFO] [EMI/]: [EMI] Baked 379846 recipes in 5611ms
[Thread-49/INFO] [EMI/]: [EMI] Baked recipes after reload in 5819ms
[Thread-43/INFO] [EMI/]: [EMI] Reloaded EMI in 61201ms
```

#### JEMI

Load time: 18658ms (14500ms before world load, 4158ms after world load)
Heap size: 4.491 GiB

Relevant logs:
```
[Render thread/WARN] [ModernFix/]: Game took 100.864 seconds to start
[Render thread/INFO] [mezz.jei.core.util.LoggedTimer/]: Starting JEI took 14.50 seconds
[Thread-49/INFO] [EMI/]: [EMI] Initialized plugin from jemi in 0ms
[Render thread/WARN] [ModernFix/]: Time from main menu to in-game was 49.368702 seconds
[Render thread/WARN] [ModernFix/]: Total time to load game and open world was 150.2327 seconds
[Thread-49/INFO] [EMI/]: [EMI] Reloaded plugin from jemi in 4158ms
[Thread-49/INFO] [EMI/]: [EMI] Baked 424928 recipes in 7246ms
[Thread-51/INFO] [EMI/]: [EMI] Baked recipes after reload in 9733ms
[Thread-49/INFO] [EMI/]: [EMI] Reloaded EMI in 65663ms
```

## ATM9

#### TMRV

Load time: 32392ms (2ms before world load, 32390ms after world load)
Heap size: 4.345 GiB

Relevant logs:
```
[Render thread/WARN] [ModernFix/]: Game took 145.889 seconds to start
[Thread-40/INFO] [EMI/]: [EMI] Initialized plugin from toomanyrecipeviewers in 2ms
[Render thread/WARN] [ModernFix/]: Time from main menu to in-game was 73.94757 seconds
[Render thread/WARN] [ModernFix/]: Total time to load game and open world was 219.83658 seconds
[Thread-40/INFO] [EMI/]: [EMI] Reloaded plugin from toomanyrecipeviewers in 32390ms
[Thread-40/INFO] [EMI/]: [EMI] Baked 443238 recipes in 5453ms
[Thread-46/INFO] [EMI/]: [EMI] Baked recipes after reload in 3926ms
[Thread-40/INFO] [EMI/]: [EMI] Reloaded EMI in 91274ms
```

#### JEMI

Load time: 49409ms (42590ms before world load, 6819ms after world load)
Heap size: 5.939 GiB

Relevant logs:
```
[Render thread/WARN] [ModernFix/]: Game took 142.23 seconds to start
[Render thread/INFO] [mezz.jei.core.util.LoggedTimer/]: Starting JEI took 42.59 s
[Thread-41/INFO] [EMI/]: [EMI] Initialized plugin from jemi in 0ms
[Render thread/WARN] [ModernFix/]: Time from main menu to in-game was 113.406425 seconds
[Render thread/WARN] [ModernFix/]: Total time to load game and open world was 255.63641 seconds
[Thread-41/INFO] [EMI/]: [EMI] Reloaded plugin from jemi in 6819ms
[Thread-41/INFO] [EMI/]: [EMI] Baked 469828 recipes in 15147ms
[Thread-41/INFO] [EMI/]: [EMI] Reloaded EMI in 72860ms
```
