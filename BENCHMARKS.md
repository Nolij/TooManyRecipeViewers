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

- [Craftoria 1.14.0](https://legacy.curseforge.com/minecraft/modpacks/craftoria/files/6117372)
- [ATM10 2.36](https://legacy.curseforge.com/minecraft/modpacks/all-the-mods-10/files/6201429)

With the following modifications:

- [EMI 1.1.20](https://legacy.curseforge.com/minecraft/mc-mods/emi/files/6205506) was added to ATM10 (because ATM10 doesn't include EMI yet).
- [Just Enough Mekanism Multiblocks](https://legacy.curseforge.com/projects/898746) was updated to the latest version ([7.5](https://legacy.curseforge.com/minecraft/mc-mods/just-enough-mekanism-multiblocks/files/6170220) as of writing) in both packs (this version fixes compatibility issues with both JEMI **and** TMRV, so I viewed this as a fair comparison).
- Log Begone was disabled in Craftoria (because it hurts performance in all scenarios for no reason and is going to be removed from Craftoria in a future update).
- [Zume](https://legacy.curseforge.com/minecraft/mc-mods/zume) was added to ATM10, and [Just Zoom](https://legacy.curseforge.com/minecraft/mc-mods/just-zoom) was disabled (because I felt like it).

All tests were performed using [TMRV 0.2.0-rc.2](https://github.com/Nolij/TooManyRecipeViewers/releases/tag/release/0.2.0-rc.2).

# Steps

1. Configure instance
   1. Confirm EMI is enabled
   2. For JEMI: Ensure JEI is enabled and TMRV is disabled
   3. For TMRV: Ensure TMRV is enabled and JEI is disabled
2. Launch instance
3. Open world
4. Deselect Minecraft before world finishes loading (this will ensure that the game is paused immediately on load, which is necessary for reliably getting a comparable heap size, as the heap size constantly changes when the game is running)
5. Wait for EMI to finish loading (`Baked recipes after reload in ...ms` and `Reloaded EMI in ...ms` are both in logs)
6. Take heap dump using your preferred profiler (I used VisualVM)
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

Load time: 3858ms (0ms before world load, 3858ms after world load)
Heap size: 2,480,544,152 B

Relevant logs:
```
[ModernFix/]: Game took 46.368 seconds to start
[EMI] Initialized plugin from toomanyrecipeviewers in 0ms
[ModernFix/]: Time from main menu to in-game was 28.567137 seconds
[ModernFix/]: Total time to load game and open world was 74.935135 seconds
[EMI] Reloaded plugin from toomanyrecipeviewers in 3858ms
[EMI] Baked 187208 recipes in 2194ms
[EMI] Baked recipes after reload in 1811ms
[EMI] Reloaded EMI in 21525ms
```

#### JEMI

Load time: 5712ms (4909ms before world load, 803ms after world load)
Heap size: 2,537,532,376 B

Relevant logs:
```
[ModernFix/]: Game took 44.919 seconds to start
Starting JEI took 4.909 s
[EMI] Initialized plugin from jemi in 0ms
[ModernFix/]: Time from main menu to in-game was 32.27889 seconds
[ModernFix/]: Total time to load game and open world was 77.19789 seconds
[EMI] Reloaded plugin from jemi in 803ms
[EMI] Baked 185722 recipes in 2504ms
[EMI] Baked recipes after reload in 2101ms
[EMI] Reloaded EMI in 16154ms
```

## ATM10

#### TMRV

Load time: 9660ms (0ms before world load, 9660ms after world load)
Heap size: 3,792,172,208 B

Relevant logs:
```
[ModernFix/]: Game took 51.638 seconds to start
[EMI] Initialized plugin from toomanyrecipeviewers in 0ms
[ModernFix/]: Time from main menu to in-game was 30.616863 seconds
[ModernFix/]: Total time to load game and open world was 82.25487 seconds
[EMI] Reloaded plugin from toomanyrecipeviewers in 9660ms
[EMI] Baked 352219 recipes in 3471ms
[EMI] Baked recipes after reload in 3056ms
[EMI] Reloaded EMI in 43189ms
```

#### JEMI

Load time: 16713ms (14190ms before world load, 2523ms after world load)
Heap size: 4,065,737,480 B

Relevant logs:
```
[ModernFix/]: Game took 53.027 seconds to start
Starting JEI took 14.19 s
[EMI] Initialized plugin from jemi in 0ms
[ModernFix/]: Time from main menu to in-game was 41.428333 seconds
[ModernFix/]: Total time to load game and open world was 94.45534 seconds
[EMI] Reloaded plugin from jemi in 2523ms
[EMI] Baked 344880 recipes in 5830ms
[EMI] Baked recipes after reload in 4218ms
[EMI] Reloaded EMI in 37059ms
```