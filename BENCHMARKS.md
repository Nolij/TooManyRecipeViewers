# System Details

All benchmarks were performed in the following environment:

```
CPU: AMD Ryzen 9 5950x
RAM: 128 GB DDR4-3600 (8 GB allocated to Minecraft)
Disk: PCIe Gen 4 NVMe using BtrFS
OS: Arch Linux (Linux 6.13.2-arch1-1) (last updated February 10th, 2025)
DE: KDE Plasma 6.2.5 (Wayland)
GLFW: aur/glfw-wayland-minecraft-cursorfix 3.4-4
JVM: aur/jdk21-temurin 21.0.5.u11-1
Launcher: extra/prismlauncher 9.2-2
JVM Flags: -XX:+UseShenandoahGC -XX:+UnlockExperimentalVMOptions -XX:+AlwaysPreTouch -XX:+UseStringDeduplication -Dfml.ignorePatchDiscrepancies=true -Dfml.ignoreInvalidMinecraftCertificates=true -XX:-OmitStackTraceInFastThrow -XX:+OptimizeStringConcat -Dfml.readTimeout=180
```

Two modpacks were tested:

- [Craftoria 1.15.2](https://legacy.curseforge.com/minecraft/modpacks/craftoria/files/6227379)
- [ATM10 2.36](https://legacy.curseforge.com/minecraft/modpacks/all-the-mods-10/files/6201429)

With the following modifications:

- [JEI 19.21.0.247](https://www.curseforge.com/minecraft/mc-mods/jei/files/5846880) (latest applicable version as of writing) was added to Craftoria for the JEMI benchmark.
- Included TMRV in Craftoria was replaced with latest dev build (for obvious reasons).
- [EMI 1.1.20](https://legacy.curseforge.com/minecraft/mc-mods/emi/files/6205506) was added to ATM10 (because ATM10 doesn't include EMI).
- [Just Enough Mekanism Multiblocks](https://legacy.curseforge.com/projects/898746) was updated to the latest version ([7.5](https://legacy.curseforge.com/minecraft/mc-mods/just-enough-mekanism-multiblocks/files/6170220) as of writing) in ATM10 (this version fixes compatibility issues with both JEMI **and** TMRV, so I viewed this as a fair comparison).
- [Zume](https://legacy.curseforge.com/minecraft/mc-mods/zume) was added to ATM10, and [Just Zoom](https://legacy.curseforge.com/minecraft/mc-mods/just-zoom) was disabled (because I felt like it).

All tests were performed using [TMRV 0.3.0-rc.1](https://github.com/Nolij/TooManyRecipeViewers/releases/tag/release/0.3.0-rc.1).

# Steps

1. Configure instance
   1. Confirm EMI is enabled
   2. For JEMI: Ensure JEI is enabled and TMRV is disabled
   3. For TMRV: Ensure TMRV is enabled and JEI is disabled
2. Launch instance
3. Open world
4. Deselect Minecraft before world finishes loading (this will ensure that the game is paused immediately on load, which is necessary for reliably getting a comparable heap size, as the heap size constantly changes when the game is running)
5. Wait for EMI to finish loading (`Baked recipes after reload in ...ms` and `Reloaded EMI in ...ms` are both in logs)
6. Take heap dump using your preferred profiler (I used YourKit)
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

Load time: 2732ms (2ms before world load, 2730ms after world load)
Heap size: 5.28 GiB

Relevant logs:
```
[ModernFix/]: Game took 66.703 seconds to start
[EMI] Initialized plugin from toomanyrecipeviewers in 2ms
[ModernFix/]: Time from main menu to in-game was 22.227667 seconds
[ModernFix/]: Total time to load game and open world was 88.93067 seconds
[EMI] Reloaded plugin from toomanyrecipeviewers in 2730ms
[EMI] Baked 207636 recipes in 3900ms
[EMI] Baked recipes after reload in 3199ms
[EMI] Reloaded EMI in 23521ms
```

#### JEMI

Load time: 6895ms (5559ms before world load, 1336ms after world load)
Heap size: 5.36 GiB

Relevant logs:
```
[ModernFix/]: Game took 67.763 seconds to start
Starting JEI took 5.559 s
[EMI] Initialized plugin from jemi in 0ms
[ModernFix/]: Time from main menu to in-game was 25.390656 seconds
[ModernFix/]: Total time to load game and open world was 93.153656 seconds
[EMI] Reloaded plugin from jemi in 1336ms
[EMI] Baked 205389 recipes in 3446ms
[EMI] Baked recipes after reload in 3233ms
[EMI] Reloaded EMI in 19175ms
```

## ATM10

#### TMRV

Load time: 8214ms (3ms before world load, 8211ms after world load)
Heap size: 5.05 GiB

Relevant logs:
```
[ModernFix/]: Game took 75.413 seconds to start
[EMI] Initialized plugin from toomanyrecipeviewers in 3ms
[ModernFix/]: Time from main menu to in-game was 34.97525 seconds
[ModernFix/]: Total time to load game and open world was 110.38825 seconds
[EMI] Reloaded plugin from toomanyrecipeviewers in 8211ms
[EMI] Baked 352446 recipes in 3482ms
[EMI] Baked recipes after reload in 3262ms
[EMI] Reloaded EMI in 44206ms
```

#### JEMI

Load time: 17468ms (14670ms before world load, 2798ms after world load)
Heap size: 5.3 GiB

Relevant logs:
```
[ModernFix/]: Game took 74.366 seconds to start
Starting JEI took 14.67 s
[EMI] Initialized plugin from jemi in 0ms
[ModernFix/]: Time from main menu to in-game was 46.214664 seconds
[ModernFix/]: Total time to load game and open world was 120.58066 seconds
[EMI] Reloaded plugin from jemi in 2798ms
[EMI] Baked 344655 recipes in 3980ms
[EMI] Baked recipes after reload in 3681ms
[EMI] Reloaded EMI in 36073ms
```