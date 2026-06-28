# System Details

All benchmarks were performed in the following environment:

```
CPU: AMD Ryzen 7 7840HS
RAM: 64 GB DDR5-3600
Disk: PCIe Gen 4 NVMe using BtrFS
OS: Arch Linux (Linux 7.0.9-arch2-1)
DE: KDE Plasma 6.6.5 (Wayland)
GLFW: aur/glfw-wayland-minecraft-cursorfix 3.4-6
JVM: aur/jdk21-temurin 21.0.11.u10-1
Launcher: extra/prismlauncher 11.0.2-1
JVM Flags: -XX:+UseZGC -XX:+ZGenerational -XX:+UnlockExperimentalVMOptions -XX:+AlwaysPreTouch -XX:+UseStringDeduplication -Dfml.ignorePatchDiscrepancies=true -Dfml.ignoreInvalidMinecraftCertificates=true -XX:-OmitStackTraceInFastThrow -XX:+OptimizeStringConcat -Dfml.readTimeout=180
```

Three modpacks were tested:

- [Craftoria 1.31.0](https://legacy.curseforge.com/minecraft/modpacks/craftoria/files/8127261) (21.1)
- [ATM10 7.1](https://legacy.curseforge.com/minecraft/modpacks/all-the-mods-10/files/8323938) (21.1)
- [Finality: Omnia 1201.0.3-alpha.36-rc1](https://legacy.curseforge.com/minecraft/modpacks/finality/files/7845475) (20.1)

With the following modifications:

- [JEI 19.27.0.343](https://legacy.curseforge.com/minecraft/mc-mods/jei/files/8292118) (latest applicable version as of writing) was added to Craftoria for the JEMI benchmark.
- Included TMRV in Craftoria was replaced with latest dev build (for obvious reasons).
- [EMI 1.1.24](https://legacy.curseforge.com/minecraft/mc-mods/emi/files/8081408) ([LexForge link](https://legacy.curseforge.com/minecraft/mc-mods/emi/files/8081375)) was added to ATM10 and ATM9 (because those packs don't include EMI).
- EMI was also updated to 1.1.24 in Craftoria.
- Sodium was removed from ATM10 along with its dependents (Sodium Extra, Iris, IrisSearch, Colorwheel, and Colorwheel Patcher) due to ethical concerns.

All tests were performed using [TMRV 0.9.0-rc.1](https://github.com/Nolij/TooManyRecipeViewers/releases/tag/release/0.9.0-rc.1).

Craftoria and ATM10 were tested with an 8 GiB max heap size. Finality: Omnia was tested with a 4 GiB max heap size.

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
8. Collect relevant logs using following RegEx: `/\[ModernFix/\]: (Game took [0-9]+\.[0-9]+ seconds to start|Time from main menu to in-game was [0-9]+\.[0-9]+ seconds|Total time to load game and open world was [0-9]+\.[0-9]+ seconds)|Starting JEI took [0-9]+\.[0-9]+ s|JEI plugins loaded in [0-9]+ms|\[EMI\] ((Reloaded|Initialized) plugin from (jemi|toomanyrecipeviewers) in [0-9]+ms|Baked [0-9]+ recipes in [0-9]+ms|Reloaded EMI in [0-9]+ms|Baked recipes after reload in [0-9]+ms)/g`
9. Determine load times from logs
   - For JEMI:
     - Pre-world-load time: JEI start time (`Starting JEI took {} s`) + `jemi` EMI plugin initialize time (`Initialized plugin from jemi in {}ms`)
     - Post-world-load time: `jemi` EMI plugin register time (`Reloaded plugin from jemi in {}ms`)
   - For TMRV:
     - Pre-world-load time: `toomanyrecipeviewers` EMI plugin initialize time (`Initialized plugin from toomanyrecipeviewers in {}ms`)
     - Post-world-load time: `toomanyrecipeviewers` EMI plugin register time (`Reloaded plugin from toomanyrecipeviewers in {}ms`)

# Results

## Craftoria (21.1)

#### TMRV

Load time: 1582ms (5ms before world load, 1577ms after world load)
Heap size: 4.278 GiB

Relevant logs:
```
[16:05:31.358] [Render thread/WARN] [ModernFix/]: Game took 116.303 seconds to start
[16:05:36.543] [Thread-141/INFO] [EMI/]: [EMI] Initialized plugin from toomanyrecipeviewers in 5ms
[16:05:44.130] [Render thread/WARN] [ModernFix/]: Time from main menu to in-game was 25.481308 seconds
[16:05:44.131] [Render thread/WARN] [ModernFix/]: Total time to load game and open world was 141.7843 seconds
[16:05:56.629] [Thread-141/INFO] [toomanyrecipeviewers/]: JEI plugins loaded in 1276ms
[16:05:56.633] [Thread-141/INFO] [EMI/]: [EMI] Reloaded plugin from toomanyrecipeviewers in 1577ms
[16:06:05.157] [Thread-141/INFO] [EMI/]: [EMI] Baked 299801 recipes in 7789ms
[16:06:14.357] [Thread-170/INFO] [EMI/]: [EMI] Baked recipes after reload in 9203ms
[16:06:15.720] [Thread-141/INFO] [EMI/]: [EMI] Reloaded EMI in 39357ms
```

#### JEMI

Load time: 11036ms (10000ms before world load, 1036ms after world load)
Heap size: 4.386 GiB

Relevant logs:
```
[16:02:03.387] [Render thread/WARN] [ModernFix/]: Game took 120.763 seconds to start
[16:02:17.822] [Render thread/INFO] [mezz.jei.core.util.LoggedTimer/]: Starting JEI took 10.00 seconds
[16:02:18.167] [Thread-142/INFO] [EMI/]: [EMI] Initialized plugin from jemi in 0ms
[16:02:22.376] [Render thread/WARN] [ModernFix/]: Time from main menu to in-game was 31.902885 seconds
[16:02:22.379] [Render thread/WARN] [ModernFix/]: Total time to load game and open world was 152.6659 seconds
[16:02:33.934] [Thread-142/INFO] [EMI/]: [EMI] Reloaded plugin from jemi in 1036ms
[16:02:43.275] [Thread-142/INFO] [EMI/]: [EMI] Baked 300713 recipes in 8642ms
[16:02:51.250] [Thread-157/INFO] [EMI/]: [EMI] Baked recipes after reload in 7978ms
[16:02:53.139] [Thread-142/INFO] [EMI/]: [EMI] Reloaded EMI in 35139ms
```

## ATM10 (21.1)

#### TMRV

Load time: 4951ms (3ms before world load, 4948ms after world load)
Heap size: 4.401 GiB

Relevant logs:
```
[16:13:43.264] [Render thread/WARN] [ModernFix/]: Game took 112.763 seconds to start
[16:13:48.533] [Thread-54/INFO] [EMI/]: [EMI] Initialized plugin from toomanyrecipeviewers in 3ms
[16:13:59.394] [Render thread/WARN] [ModernFix/]: Time from main menu to in-game was 30.935501 seconds
[16:13:59.394] [Render thread/WARN] [ModernFix/]: Total time to load game and open world was 143.6985 seconds
[16:14:21.469] [Thread-54/INFO] [toomanyrecipeviewers/]: JEI plugins loaded in 4430ms
[16:14:21.474] [Thread-54/INFO] [EMI/]: [EMI] Reloaded plugin from toomanyrecipeviewers in 4948ms
[16:14:36.005] [Thread-54/INFO] [EMI/]: [EMI] Baked 494388 recipes in 9604ms
[16:14:48.658] [Thread-58/INFO] [EMI/]: [EMI] Baked recipes after reload in 12658ms
[16:14:51.941] [Thread-54/INFO] [EMI/]: [EMI] Reloaded EMI in 63611ms
```

#### JEMI

Load time: 20132ms (15550ms before world load, 4582ms after world load)
Heap size: 5.273 GiB

Relevant logs:
```
[16:09:55.753] [Render thread/WARN] [ModernFix/]: Game took 114.576 seconds to start
[16:10:16.157] [Render thread/INFO] [mezz.jei.core.util.LoggedTimer/]: Starting JEI took 15.55 seconds
[16:10:16.472] [Thread-55/INFO] [EMI/]: [EMI] Initialized plugin from jemi in 0ms
[16:10:25.693] [Render thread/WARN] [ModernFix/]: Time from main menu to in-game was 44.62507 seconds
[16:10:25.693] [Render thread/WARN] [ModernFix/]: Total time to load game and open world was 159.20107 seconds
[16:10:46.035] [Thread-55/INFO] [EMI/]: [EMI] Reloaded plugin from jemi in 4582ms
[16:11:03.374] [Thread-55/INFO] [EMI/]: [EMI] Baked 542344 recipes in 12358ms
[16:11:15.009] [Thread-62/INFO] [EMI/]: [EMI] Baked recipes after reload in 11640ms
[16:11:19.610] [Thread-55/INFO] [EMI/]: [EMI] Reloaded EMI in 63343ms
```

## Finality: Omnia (20.1)

#### TMRV

Load time: 3010ms (6ms before world load, 3004ms after world load)
Heap size: 1.947 GiB

Relevant logs:
```
[16:21:42.924] [Render thread/WARN] [ModernFix/]: Game took 44.792 seconds to start
[16:21:49.592] [Thread-46/INFO] [EMI/]: [EMI] Initialized plugin from toomanyrecipeviewers in 6ms
[16:21:53.141] [Render thread/WARN] [ModernFix/]: Time from main menu to in-game was 14.053263 seconds
[16:21:53.141] [Render thread/WARN] [ModernFix/]: Total time to load game and open world was 58.84526 seconds
[16:21:55.311] [Thread-46/INFO] [toomanyrecipeviewers/]: JEI plugins loaded in 2612ms
[16:21:55.316] [Thread-46/INFO] [EMI/]: [EMI] Reloaded plugin from toomanyrecipeviewers in 3004ms
[16:21:59.413] [Thread-46/INFO] [EMI/]: [EMI] Baked 92593 recipes in 3517ms
[16:22:03.103] [Thread-51/INFO] [EMI/]: [EMI] Baked recipes after reload in 3692ms
[16:22:05.650] [Thread-46/INFO] [EMI/]: [EMI] Reloaded EMI in 16145ms
```

#### JEMI

Load time: 6354ms (4425ms before world load, 1929ms after world load)
Heap size: 2.065 GiB

Relevant logs:
```
[16:20:08.064] [Render thread/WARN] [ModernFix/]: Game took 45.456 seconds to start
[16:20:19.180] [Render thread/INFO] [mezz.jei.core.util.LoggedTimer/]: Starting JEI took 4.425 s
[16:20:19.246] [Thread-47/INFO] [EMI/]: [EMI] Initialized plugin from jemi in 0ms
[16:20:22.126] [Render thread/WARN] [ModernFix/]: Time from main menu to in-game was 18.10485 seconds
[16:20:22.126] [Render thread/WARN] [ModernFix/]: Total time to load game and open world was 63.560852 seconds
[16:20:23.062] [Thread-47/INFO] [EMI/]: [EMI] Reloaded plugin from jemi in 1929ms
[16:20:27.104] [Thread-47/INFO] [EMI/]: [EMI] Baked 90631 recipes in 3304ms
[16:20:29.206] [Thread-51/INFO] [EMI/]: [EMI] Baked recipes after reload in 2104ms
[16:20:32.767] [Thread-47/INFO] [EMI/]: [EMI] Reloaded EMI in 13585ms
```
