# System Details

All benchmarks were performed in the following environment:

```
CPU: AMD Ryzen 9 5950x
RAM: 128 GB DDR4-3600
Disk: PCIe Gen 4 NVMe using BtrFS
OS: Arch Linux (Linux 6.13.2-arch1-1)
DE: KDE Plasma 6.2.5 (Wayland)
GLFW: aur/glfw-wayland-minecraft-cursorfix 3.4-4
JVM: aur/jdk21-temurin 21.0.6.u7-1
Launcher: extra/prismlauncher 9.2-2
JVM Flags: -XX:+UseShenandoahGC -XX:+UnlockExperimentalVMOptions -XX:+AlwaysPreTouch -XX:+UseStringDeduplication -Dfml.ignorePatchDiscrepancies=true -Dfml.ignoreInvalidMinecraftCertificates=true -XX:-OmitStackTraceInFastThrow -XX:+OptimizeStringConcat -Dfml.readTimeout=180
```

Three modpacks were tested:

- [Craftoria 1.19.0](https://legacy.curseforge.com/minecraft/modpacks/craftoria/files/6422922)
- [ATM10 2.43](https://legacy.curseforge.com/minecraft/modpacks/all-the-mods-10/files/6420479)
- [ATM9 1.0.7](https://legacy.curseforge.com/minecraft/modpacks/all-the-mods-9/files/6322046)

With the following modifications:

- [JEI 19.21.0.247](https://www.curseforge.com/minecraft/mc-mods/jei/files/5846880) (latest applicable version as of writing) was added to Craftoria for the JEMI benchmark.
- Included TMRV in Craftoria was replaced with latest dev build (for obvious reasons).
- [EMI 1.1.22](https://legacy.curseforge.com/minecraft/mc-mods/emi/files/6420931) ([LexForge link](https://legacy.curseforge.com/minecraft/mc-mods/emi/files/6420945)) was added to ATM10 and ATM9 (because those packs don't include EMI).
- ModernFix was updated to [5.21.0](https://legacy.curseforge.com/minecraft/mc-mods/modernfix/files/6392741) on ATM9 (contains a bug fix related to JEI).
- `earlyWindowControl` was set to `false` in `.minecraft/config/fml.toml` for ATM9 to fix an issue with Wayland.
- Sodium Dynamic Lights and Sodium Options API were removed from ATM9 due to ethical concerns.
- [Zume](https://legacy.curseforge.com/minecraft/mc-mods/zume) was added to ATM10 and ATM9, and [Just Zoom](https://legacy.curseforge.com/minecraft/mc-mods/just-zoom) was disabled (because I felt like it).

All tests were performed using [TMRV 0.4.0-rc.5](https://github.com/Nolij/TooManyRecipeViewers/releases/tag/release/0.4.0-rc.5).

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

Load time: 3378ms (53ms before world load, 3325ms after world load)
Heap size: 2.820 GiB

Relevant logs:
```
[Render thread/WARN] [ModernFix/]: Game took 87.233 seconds to start
[Thread-126/INFO] [EMI/]: [EMI] Initialized plugin from toomanyrecipeviewers in 53ms
[Render thread/WARN] [ModernFix/]: Time from main menu to in-game was 22.423174 seconds
[Render thread/WARN] [ModernFix/]: Total time to load game and open world was 109.65617 seconds
[Thread-126/INFO] [EMI/]: [EMI] Reloaded plugin from toomanyrecipeviewers in 3325ms
[Thread-126/INFO] [EMI/]: [EMI] Baked 252184 recipes in 3461ms
[Thread-139/INFO] [EMI/]: [EMI] Baked recipes after reload in 3266ms
[Thread-126/INFO] [EMI/]: [EMI] Reloaded EMI in 28556ms
```

#### JEMI

Load time: 7677ms (6413ms before world load, 1264ms after world load)
Heap size: 2.874 GiB

Relevant logs:
```
[Render thread/WARN] [ModernFix/]: Game took 86.753 seconds to start
[Render thread/INFO] [mezz.jei.core.util.LoggedTimer/]: Starting JEI took 6.413 s
[Thread-130/INFO] [EMI/]: [EMI] Initialized plugin from jemi in 0ms
[Render thread/WARN] [ModernFix/]: Time from main menu to in-game was 26.96288 seconds
[Render thread/WARN] [ModernFix/]: Total time to load game and open world was 113.71588 seconds
[Thread-130/INFO] [EMI/]: [EMI] Reloaded plugin from jemi in 1264ms
[Thread-130/INFO] [EMI/]: [EMI] Baked 240920 recipes in 4055ms
[Thread-139/INFO] [EMI/]: [EMI] Baked recipes after reload in 3484ms
[Thread-130/INFO] [EMI/]: [EMI] Reloaded EMI in 24214ms
```

## ATM10

#### TMRV

Load time: 9875ms (1ms before world load, 9874ms after world load)
Heap size: 3.910 GiB

Relevant logs:
```
[Render thread/WARN] [ModernFix/]: Game took 109.207 seconds to start
[Thread-70/INFO] [EMI/]: [EMI] Initialized plugin from toomanyrecipeviewers in 1ms
[Render thread/WARN] [ModernFix/]: Time from main menu to in-game was 35.34915 seconds
[Render thread/WARN] [ModernFix/]: Total time to load game and open world was 144.55615 seconds
[Thread-70/INFO] [EMI/]: [EMI] Reloaded plugin from toomanyrecipeviewers in 9874ms
[Thread-70/INFO] [EMI/]: [EMI] Baked 415029 recipes in 5990ms
[Thread-72/INFO] [EMI/]: [EMI] Baked recipes after reload in 7077ms
[Thread-70/INFO] [EMI/]: [EMI] Reloaded EMI in 59136ms
```

#### JEMI

Load time: 19936ms (16350ms before world load, 3586ms after world load)
Heap size: 4.394 GiB

Relevant logs:
```
[Render thread/WARN] [ModernFix/]: Game took 102.931 seconds to start
[Render thread/INFO] [mezz.jei.core.util.LoggedTimer/]: Starting JEI took 16.35 s
[Thread-73/INFO] [EMI/]: [EMI] Initialized plugin from jemi in 0ms
[Render thread/WARN] [ModernFix/]: Time from main menu to in-game was 47.634 seconds
[Render thread/WARN] [ModernFix/]: Total time to load game and open world was 150.565 seconds
[Thread-73/INFO] [EMI/]: [EMI] Reloaded plugin from jemi in 3586ms
[Thread-73/INFO] [EMI/]: [EMI] Baked 401736 recipes in 7747ms
[Thread-76/INFO] [EMI/]: [EMI] Baked recipes after reload in 9799ms
[Thread-73/INFO] [EMI/]: [EMI] Reloaded EMI in 51281ms
```

## ATM9

#### TMRV

Load time: 25319ms (1ms before world load, 25318ms after world load)
Heap size: 4.520 GiB

Relevant logs:
```
[Render thread/WARN] [ModernFix/]: Game took 139.524 seconds to start
[Thread-41/INFO] [EMI/]: [EMI] Initialized plugin from toomanyrecipeviewers in 1ms
[Render thread/WARN] [ModernFix/]: Time from main menu to in-game was 66.07776 seconds
[Render thread/WARN] [ModernFix/]: Total time to load game and open world was 205.60176 seconds
[Thread-41/INFO] [EMI/]: [EMI] Reloaded plugin from toomanyrecipeviewers in 25318ms
[Thread-41/INFO] [EMI/]: [EMI] Baked 442634 recipes in 4334ms
[Thread-44/INFO] [EMI/]: [EMI] Baked recipes after reload in 3637ms
[Thread-41/INFO] [EMI/]: [EMI] Reloaded EMI in 75042ms
```

#### JEMI

Load time: 38785ms (34610ms before world load, 4175ms after world load)
Heap size: 5.246 GiB

Relevant logs:
```
[Render thread/WARN] [ModernFix/]: Game took 142.874 seconds to start
[Render thread/INFO] [mezz.jei.core.util.LoggedTimer/]: Starting JEI took 34.61 s
[Thread-40/INFO] [EMI/]: [EMI] Initialized plugin from jemi in 0ms
[Render thread/WARN] [ModernFix/]: Time from main menu to in-game was 104.39899 seconds
[Render thread/WARN] [ModernFix/]: Total time to load game and open world was 247.27298 seconds
[Thread-40/INFO] [EMI/]: [EMI] Reloaded plugin from jemi in 4175ms
[Thread-40/INFO] [EMI/]: [EMI] Baked 434172 recipes in 12834ms
[Thread-40/INFO] [EMI/]: [EMI] Reloaded EMI in 65663ms
[Thread-44/INFO] [EMI/]: [EMI] Baked recipes after reload in 12953ms
```
