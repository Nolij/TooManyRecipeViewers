# IMPORTANT LICENSE NOTICE

By using this project in any form, you hereby give your "express assent" for the terms of the license of this project (see [License](#license)), and acknowledge that I (the author of this project) have fulfilled my obligation under the license to "make a reasonable effort under the circumstances to obtain the express assent of recipients to the terms of this License".

# TooManyRecipeViewers

**T**oo**M**any**R**ecipe**V**iewers (or TMRV) is a compatibility layer for running [JEI](https://legacy.curseforge.com/minecraft/mc-mods/jei) plugins with [EMI](https://legacy.curseforge.com/minecraft/mc-mods/emi) **without** having to install [JEI](https://legacy.curseforge.com/minecraft/mc-mods/jei), written by Nolij.

You'll need EMI installed to use this mod.

# Why use TMRV over EMI+JEI (AKA JEMI)?

JEMI is a compatibility layer built-in to EMI to get JEI plugins _mostly_ working with EMI by heavily relying on the JEI internals. It is designed to be as simplistic as possible and relies on JEI to process recipe data first before it is imported to EMI.

TMRV is not like JEMI - it aims to completely replace the JEI API (NOTE: TMRV does contain some unmodified JEI internals - see [JEI Code Re-Use](#jei-code-re-use)). TMRV (where feasible) replaces JEI APIs with direct mappers to EMI APIs instead of loading the whole JEI registry and querying it after the fact. This has several advantages, including more efficient use of system resources, but is also a tradeoff, as TMRV's approach makes maintenance far more involved than what JEMI requires. JEMI is intentionally designed this way to allow EMI development to be focused on improving EMI itself, which is an approach I fully support.

TL;DR: **TMRV is much more efficient than JEMI** at the cost of taking **much more effort** to maintain than JEMI does, and **nobody should expect EMI to put this amount of effort** in to supporting an API that was intentionally not referenced during design. Be happy EMI can even load JEI plugins out of the box to begin with - it still took a fair amount of effort.

That being said, TMRV has two primary advantages over JEMI:

### 1. Plugin Compatibility

TMRV has better coverage of JEI's API than JEMI does (with one exception - see [Known API Limitations](#known-api-limitations)). As of writing, this includes:

- Better conversion of built-in recipe types (JEMI only supports crafting and info recipe types; TMRV supports all built-in JEI recipe types)
- Ingredient/search aliases

### 2. Efficiency

With TMRV, you will always load in to the world faster than with JEMI. This is because JEI plugin initialization blocks world load - you can't start playing until all JEI plugins are initialized. EMI loads plugins asynchronously _after_ the world is loaded.

This means that even if TMRV loaded JEI plugins _slower_ than JEI does (not the case, it loads them measurably faster - see [benchmarks](#benchmarks)), worlds will _always_ load faster with TMRV than with JEMI.

As already mentioned, TMRV replaces much of the JEI APIs with mappers to the corresponding EMI APIs - this means entire parts of the JEI internals can be outright removed. There's no need to initialize and store a whole JEI recipe registry - TMRV just converts JEI API calls to EMI ones, and converts the responses to the JEI format. Think of TMRV like Wine or Proton, and JEMI like a VM. JEMI uses real JEI, so there will be some scenarios where TMRV will error where JEMI won't (note that this doesn't necessarily mean that JEMI properly supports a scenario, it just means it looks like it does), but TMRV will generally be more efficient than JEMI.

# Benchmarks

The full results and steps followed to obtain them are documented in [BENCHMARKS.md](https://github.com/Nolij/TooManyRecipeViewers/raw/master/BENCHMARKS.md). These results were not cherry-picked. The instructions were followed exactly as documented in that file. I encourage the community to verify them.

### Load Times

|                             | TMRV                                                                        | JEMI                                                                           | Comparison                                                       |
|-----------------------------|-----------------------------------------------------------------------------|--------------------------------------------------------------------------------|------------------------------------------------------------------|
| Craftoria&nbsp;&nbsp;&nbsp; | 3378ms (53ms before world load, 3325ms after world load)&nbsp;&nbsp;&nbsp;  | 7677ms (6413ms before world load, 1264ms after world load)&nbsp;&nbsp;&nbsp;   | -4299ms (-6360ms before world load, +2061ms after world load)    |
| ATM10&nbsp;&nbsp;&nbsp;     | 9875ms (1ms before world load, 9874ms after world load)&nbsp;&nbsp;&nbsp;   | 19936ms (16350ms before world load, 3586ms after world load)&nbsp;&nbsp;&nbsp; | -10061ms (-16349ms before world load, +6288ms after world load)  |
| ATM9&nbsp;&nbsp;&nbsp;      | 25319ms (1ms before world load, 25318ms after world load)&nbsp;&nbsp;&nbsp; | 38785ms (34610ms before world load, 4175ms after world load)&nbsp;&nbsp;&nbsp; | -13466ms (-34609ms before world load, +21143ms after world load) |

### Memory Usage

|                             | TMRV                        | JEMI                        | Comparison               |
|-----------------------------|-----------------------------|-----------------------------|--------------------------|
| Craftoria&nbsp;&nbsp;&nbsp; | 2.820 GiB&nbsp;&nbsp;&nbsp; | 2.874 GiB&nbsp;&nbsp;&nbsp; | -55.3 MiB (approximate)  |
| ATM10&nbsp;&nbsp;&nbsp;     | 3.910 GiB&nbsp;&nbsp;&nbsp; | 4.394 GiB&nbsp;&nbsp;&nbsp; | -495.6 MiB (approximate) |
| ATM9&nbsp;&nbsp;&nbsp;      | 4.520 GiB&nbsp;&nbsp;&nbsp; | 5.246 GiB&nbsp;&nbsp;&nbsp; | -743.4 MiB (approximate) |

# Known API Limitations

### Scroll Widget

Neither TMRV nor JEMI currently render this JEI widget properly. This limitation will eventually be addressed, but for now, recipes using it (such as Chipped's Workbench) will not render properly.

### JEI Config Files

`.minecraft/config/jei/blacklist.json` is the only JEI config file that TMRV even reads. This file _should_ work fine for vanilla ingredient types and for modded ingredient types added by a JEI plugin (this does not include mods that support both JEI and EMI natively, such as Mekanism). This is meant to be a stop-gap for packs switching over from JEI. JEMI had a similar flaw. EMI has its own config for hiding ingredients - please use that instead. All other JEI config files are completely ignored by TMRV, and there are no plans to support them.

### Recipe Manager Plugins

The JEI API supports "Recipe Manager Plugins". These plugins allow mods to control their own recipe registries and handle recipe lookups themselves at runtime.

TMRV will attempt to extract recipes from these plugins, but this does not work for most plugins, and by no means provides proper support for the feature. Support beyond this is not planned. Recipe Manager plugins are an outdated concept that very few plugins still use, and they aren't possible to properly support without very invasive EMI mixins - something I do not intend to use in this project.

### Vanilla Recipe Category Extensions

The JEI API supports "extensions" to the vanilla Crafting and Smithing recipe categories. Sufficient inspection (that would be necessary to determine feasibility of adding support) of how this part of the JEI API works is yet to be done. For now, though, these are unsupported by TMRV.

### Runtime Registry Changes

The JEI API supports modifying the recipe and ingredient registries at runtime (ie after plugin registration is complete). This concept is not compatible with EMI, and it is not a practice I want to support. As such, after `IModPlugin.onRuntimeAvailable` has been invoked, all APIs for runtime registry modifications will throw an `IllegalStateException` if invoked to avoid potential confusion.

# JEI Code Re-use

The plan is to replace more of the JEI internals in future updates. However, some parts of JEI simply aren't worth re-implementing for various reasons. Regardless, enough of JEI has already been replaced in TMRV that I can confidently say:

1. It wouldn't be feasible to achieve the same improvements over JEMI with mixins (at least sanely), and
2. Enough of the JEI internals have been replaced or removed that I don't consider this unfair to JEI, especially given the fact that [JEI's license](https://github.com/mezz/JustEnoughItems/blob/d4ea796eb319efff2ff209f50c053c2a5a1dec05/LICENSE.txt) explicitly allows doing this.

# License

This project is licensed under OSL-3.0. For more information, see [LICENSE](https://github.com/Nolij/TooManyRecipeViewers/raw/master/LICENSE).

Some code was copied from [EMI](https://github.com/emilyploszaj/emi) and [JEI](https://github.com/mezz/JustEnoughItems) in compliance with their copyright licenses. All modifications present in this project are licensed under the same license as the rest of this project, OSL-3.0.