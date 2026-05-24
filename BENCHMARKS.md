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

- [Craftoria 1.30.0](https://legacy.curseforge.com/minecraft/modpacks/craftoria/files/7746142)
- [ATM10 7.0](https://legacy.curseforge.com/minecraft/modpacks/all-the-mods-10/files/8091114)
- [ATM9 1.1.1](https://legacy.curseforge.com/minecraft/modpacks/all-the-mods-9/files/7097953)

With the following modifications:

- [JEI 19.27.0.340](https://legacy.curseforge.com/minecraft/mc-mods/jei/files/7420587) (latest applicable version as of writing) was added to Craftoria for the JEMI benchmark.
- Included TMRV in Craftoria was replaced with latest dev build (for obvious reasons).
- [EMI 1.1.24](https://legacy.curseforge.com/minecraft/mc-mods/emi/files/8081408) ([LexForge link](https://legacy.curseforge.com/minecraft/mc-mods/emi/files/8081375)) was added to ATM10 and ATM9 (because those packs don't include EMI).
- EMI was also updated to 1.1.24 in Craftoria.
- Sodium was removed from ATM10 along with its dependents (Sodium Extra, Iris, Colorwheel, and Colorwheel Patcher) due to ethical concerns.
- Sodium Dynamic Lights and Sodium Options API were removed from ATM9 due to ethical concerns.

All tests were performed using [TMRV 0.8.0-rc.2](https://github.com/Nolij/TooManyRecipeViewers/releases/tag/release/0.8.0-rc.2).

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
8. Collect relevant logs using following RegEx: `/\[ModernFix/\]: (Game took [0-9]+\.[0-9]+ seconds to start|Time from main menu to in-game was [0-9]+\.[0-9]+ seconds|Total time to load game and open world was [0-9]+\.[0-9]+ seconds)|Starting JEI took [0-9]+\.[0-9]+ s|JEI plugins loaded in [0-9]+ms|\[EMI\] ((Reloaded|Initialized) plugin from (jemi|toomanyrecipeviewers) in [0-9]+ms|Baked [0-9]+ recipes in [0-9]+ms|Reloaded EMI in [0-9]+ms|Baked recipes after reload in [0-9]+ms)/g`
9. Determine load times from logs
   - For JEMI:
     - Pre-world-load time: JEI start time (`Starting JEI took {} s`) + `jemi` EMI plugin initialize time (`Initialized plugin from jemi in {}ms`)
     - Post-world-load time: `jemi` EMI plugin register time (`Reloaded plugin from jemi in {}ms`)
   - For TMRV:
     - Pre-world-load time: `toomanyrecipeviewers` EMI plugin initialize time (`Initialized plugin from toomanyrecipeviewers in {}ms`)
     - Post-world-load time: `toomanyrecipeviewers` EMI plugin register time (`Reloaded plugin from toomanyrecipeviewers in {}ms`)

# Results

## Craftoria

#### TMRV

Load time: 2538ms (4ms before world load, 2534ms after world load)
Heap size: 4.301 GiB

Relevant logs:
```
[19:03:34.999] [Render thread/WARN] [ModernFix/]: Game took 110.225 seconds to start
[19:03:40.497] [Thread-118/INFO] [EMI/]: [EMI] Initialized plugin from toomanyrecipeviewers in 4ms
[19:03:47.586] [Render thread/WARN] [ModernFix/]: Time from main menu to in-game was 24.320158 seconds
[19:03:47.586] [Render thread/WARN] [ModernFix/]: Total time to load game and open world was 134.54515 seconds
[19:03:57.395] [Thread-118/INFO] [toomanyrecipeviewers/]: JEI plugins loaded in 2073ms (142ms for partial loads)
[19:03:57.410] [Thread-118/INFO] [EMI/]: [EMI] Reloaded plugin from toomanyrecipeviewers in 2534ms
[19:04:04.495] [Thread-118/INFO] [EMI/]: [EMI] Baked 202358 recipes in 5905ms
[19:04:10.972] [Thread-132/INFO] [EMI/]: [EMI] Baked recipes after reload in 6479ms
[19:04:15.494] [Thread-118/INFO] [EMI/]: [EMI] Reloaded EMI in 35293ms
```

#### JEMI

Load time: 13843ms (12580ms before world load, 1263ms after world load)
Heap size: 4.492 GiB

Relevant logs:
```
[19:00:12.545] [Render thread/WARN] [ModernFix/]: Game took 114.857 seconds to start
[19:00:29.490] [Render thread/INFO] [mezz.jei.core.util.LoggedTimer/]: Starting JEI took 12.58 seconds
[19:00:29.860] [Thread-120/INFO] [EMI/]: [EMI] Initialized plugin from jemi in 0ms
[19:00:33.598] [Render thread/WARN] [ModernFix/]: Time from main menu to in-game was 33.096138 seconds
[19:00:33.599] [Render thread/WARN] [ModernFix/]: Total time to load game and open world was 147.95314 seconds
[19:00:42.135] [Thread-120/INFO] [EMI/]: [EMI] Reloaded plugin from jemi in 1263ms
[19:00:51.508] [Thread-120/INFO] [EMI/]: [EMI] Baked 281365 recipes in 8081ms
[19:01:00.782] [Thread-129/INFO] [EMI/]: [EMI] Baked recipes after reload in 9277ms
[19:01:02.855] [Thread-120/INFO] [EMI/]: [EMI] Reloaded EMI in 33174ms
```

## ATM10

#### TMRV

Load time: 7946ms (2ms before world load, 7944ms after world load)
Heap size: 4.899 GiB

Relevant logs:
```
[19:10:51.453] [Render thread/WARN] [ModernFix/]: Game took 107.789 seconds to start
[19:10:56.665] [Thread-55/INFO] [EMI/]: [EMI] Initialized plugin from toomanyrecipeviewers in 2ms
[19:11:06.614] [Render thread/WARN] [ModernFix/]: Time from main menu to in-game was 28.668043 seconds
[19:11:06.615] [Render thread/WARN] [ModernFix/]: Total time to load game and open world was 136.45705 seconds
[19:11:26.983] [Thread-55/INFO] [toomanyrecipeviewers/]: JEI plugins loaded in 7362ms (391ms for partial loads)
[19:11:27.024] [Thread-55/INFO] [EMI/]: [EMI] Reloaded plugin from toomanyrecipeviewers in 7944ms
[19:11:42.580] [Thread-55/INFO] [EMI/]: [EMI] Baked 386610 recipes in 10478ms
[19:11:53.464] [Thread-58/INFO] [EMI/]: [EMI] Baked recipes after reload in 10887ms
[19:11:56.762] [Thread-55/INFO] [EMI/]: [EMI] Reloaded EMI in 60337ms
```

#### JEMI

Load time: 17919ms (13150ms before world load, 4769ms after world load)
Heap size: 5.282 GiB

Relevant logs:
```
[19:07:05.967] [Render thread/WARN] [ModernFix/]: Game took 110.329 seconds to start
[19:07:23.676] [Render thread/INFO] [mezz.jei.core.util.LoggedTimer/]: Starting JEI took 13.15 seconds
[19:07:23.946] [Thread-55/INFO] [EMI/]: [EMI] Initialized plugin from jemi in 0ms
[19:07:31.957] [Render thread/WARN] [ModernFix/]: Time from main menu to in-game was 39.64188 seconds
[19:07:31.958] [Render thread/WARN] [ModernFix/]: Total time to load game and open world was 149.97089 seconds
[19:07:54.710] [Thread-55/INFO] [EMI/]: [EMI] Reloaded plugin from jemi in 4769ms
[19:08:12.376] [Thread-55/INFO] [EMI/]: [EMI] Baked 540120 recipes in 12729ms
[19:08:24.943] [Thread-59/INFO] [EMI/]: [EMI] Baked recipes after reload in 12573ms
[19:08:29.777] [Thread-55/INFO] [EMI/]: [EMI] Reloaded EMI in 66006ms
```

## ATM9

#### TMRV

Load time: 16271ms (3ms before world load, 16268ms after world load)
Heap size: 5.740 GiB

Relevant logs:
```
[19:22:03.798] [Render thread/WARN] [ModernFix/]: Game took 125.249 seconds to start
[19:22:21.517] [Thread-44/INFO] [EMI/]: [EMI] Initialized plugin from toomanyrecipeviewers in 3ms
[19:22:30.050] [Render thread/WARN] [ModernFix/]: Time from main menu to in-game was 44.7959 seconds
[19:22:30.050] [Render thread/WARN] [ModernFix/]: Total time to load game and open world was 170.04489 seconds
[19:22:52.111] [Thread-44/INFO] [toomanyrecipeviewers/]: JEI plugins loaded in 15545ms (1625ms for partial loads)
[19:22:52.156] [Thread-44/INFO] [EMI/]: [EMI] Reloaded plugin from toomanyrecipeviewers in 16268ms
[19:22:58.361] [Thread-44/INFO] [EMI/]: [EMI] Baked 398943 recipes in 5287ms
[19:23:04.239] [Thread-45/INFO] [EMI/]: [EMI] Baked recipes after reload in 5881ms
[19:23:13.572] [Thread-44/INFO] [EMI/]: [EMI] Reloaded EMI in 52162ms
```

#### JEMI

Load time: 53911ms (39460ms before world load, 14451ms after world load)
Heap size: 6.770 GiB

Relevant logs:
```
[19:16:59.227] [Render thread/WARN] [ModernFix/]: Game took 127.949 seconds to start
[19:17:56.045] [Render thread/INFO] [mezz.jei.core.util.LoggedTimer/]: Starting JEI took 39.46 s
[19:17:56.322] [Thread-44/INFO] [EMI/]: [EMI] Initialized plugin from jemi in 0ms
[19:17:58.582] [Render thread/WARN] [ModernFix/]: Time from main menu to in-game was 78.52919 seconds
[19:17:58.585] [Render thread/WARN] [ModernFix/]: Total time to load game and open world was 206.47818 seconds
[19:18:23.291] [Thread-44/INFO] [EMI/]: [EMI] Reloaded plugin from jemi in 14451ms
[19:18:34.227] [Thread-44/INFO] [EMI/]: [EMI] Baked 498961 recipes in 9742ms
[19:18:44.829] [Thread-45/INFO] [EMI/]: [EMI] Baked recipes after reload in 10605ms
[19:18:47.632] [Thread-44/INFO] [EMI/]: [EMI] Reloaded EMI in 51399ms

```
