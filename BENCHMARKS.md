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
- [ATM10 2.34](https://legacy.curseforge.com/minecraft/modpacks/all-the-mods-10/files/6169683)

With the following modifications:

- [EMI 1.1.19](https://legacy.curseforge.com/minecraft/mc-mods/emi/files/6075440) was added to ATM10 (because ATM10 doesn't include EMI yet).
- [Just Enough Mekanism Multiblocks](https://legacy.curseforge.com/projects/898746) was updated to the latest version ([7.5](https://legacy.curseforge.com/minecraft/mc-mods/just-enough-mekanism-multiblocks/files/6170220) as of writing) in both packs (this version fixes compatibility issues with both JEMI **and** TMRV, so I viewed this as a fair comparison).
- Log Begone was disabled in Craftoria (because it hurts performance in all scenarios for no reason and is going to be removed from Craftoria in a future update).
- [Zume](https://legacy.curseforge.com/minecraft/mc-mods/zume) was added to ATM10, and [Just Zoom](https://legacy.curseforge.com/minecraft/mc-mods/just-zoom) was disabled (because I felt like it).

All tests were performed using [TMRV 0.1.0-pre.1](https://github.com/Nolij/TooManyRecipeViewers/releases/tag/release/0.1.0-pre.1).

# Steps

1. Configure instance
   1. Confirm EMI is enabled
   2. For JEMI: Ensure JEI is enabled and TMRV is disabled
   3. For TMRV: Ensure TMRV is enabled and JEI is disabled
2. Launch instance
3. Open world
4. Wait for EMI to finish loading (`Reloaded EMI in ...ms` and `Baked recipes after reload in ...ms` are both in logs)
5. Open inventory
6. Wait for Minecraft to unfreeze
7. Quit game
8. Launch instance
9. Open world
10. Wait for EMI to finish loading (`Reloaded EMI in ...ms` and `Baked recipes after reload in ...ms` are both in logs)
11. Open inventory
12. Wait for Minecraft to unfreeze
13. Quit game
14. Collect relevant logs using following RegEx: `/\[ModernFix\/\]: (Game took \d+\.\d+ seconds to start|Time from main menu to in\-game was \d+\.\d+ seconds|Total time to load game and open world was \d+\.\d+ seconds)|Starting JEI took \d+\.\d+ s|\[EMI\] (Reloaded plugin from (jemi|toomanyrecipeviewers) in \d+ms|Baked \d+ recipes in \d+ms|Reloaded EMI in \d+ms|Baked recipes after reload in \d+ms)|\[toomanyrecipeviewers\/\]: Registered \d+ ingredient aliases from JEI plugins in \d+ms/g`
15. Determine load times from logs
    - For JEMI: JEI start time (`Starting JEI took {} s`) + `jemi` EMI plugin load time (`Reloaded plugin from jemi in {}ms`)
    - For TMRV: `toomanyrecipeviewers` EMI plugin load time (`Reloaded plugin from toomanyrecipeviewers in {}ms`) + ingredient alias registration time (`Registered ... ingredient aliases from JEI plugins in {}ms`) (this one is beyond miniscule, but I want to be as fair as possible)

# Results

## Craftoria

#### TMRV

Load time: 3957ms (0ms before world load, 3957ms after world load)

```
[ModernFix/]: Game took 42.757 seconds to start
[ModernFix/]: Time from main menu to in-game was 24.713524 seconds
[ModernFix/]: Total time to load game and open world was 67.47052 seconds
[EMI] Reloaded plugin from toomanyrecipeviewers in 3954ms
[EMI] Baked 186559 recipes in 6257ms
[toomanyrecipeviewers/]: Registered 253 ingredient aliases from JEI plugins in 3ms
[EMI] Reloaded EMI in 23044ms
[EMI] Baked recipes after reload in 5580ms
```

#### JEMI

Load time: 6061ms (4864ms before world load, 1197ms after world load)

```
[ModernFix/]: Game took 44.499 seconds to start
Starting JEI took 4.864 s
[ModernFix/]: Time from main menu to in-game was 29.854439 seconds
[ModernFix/]: Total time to load game and open world was 74.35344 seconds
[EMI] Reloaded plugin from jemi in 1197ms
[EMI] Baked 185722 recipes in 13190ms
[EMI] Reloaded EMI in 27726ms
[EMI] Baked recipes after reload in 13359ms
```

## ATM10

#### TMRV

Load time: 8908ms (0ms before world load, 8908ms after world load)

```
[ModernFix/]: Game took 50.463 seconds to start
[ModernFix/]: Time from main menu to in-game was 30.587208 seconds
[ModernFix/]: Total time to load game and open world was 81.05021 seconds
[EMI] Reloaded plugin from toomanyrecipeviewers in 8905ms
[EMI] Baked 346710 recipes in 7742ms
[EMI] Baked recipes after reload in 8219ms
[toomanyrecipeviewers/]: Registered 235 ingredient aliases from JEI plugins in 3ms
[EMI] Reloaded EMI in 73433ms
```

#### JEMI

Load time: 16550ms (13580ms before world load, 2970ms after world load)

```
[ModernFix/]: Game took 50.462 seconds to start
Starting JEI took 13.58 s
[ModernFix/]: Time from main menu to in-game was 41.512444 seconds
[ModernFix/]: Total time to load game and open world was 91.97444 seconds
[EMI] Reloaded plugin from jemi in 2970ms
[EMI] Baked 343383 recipes in 13003ms
[EMI] Reloaded EMI in 66541ms
[EMI] Baked recipes after reload in 12414ms
```