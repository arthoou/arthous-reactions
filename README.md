# Arthou's Reactions 🎉

Arthou's Reactions gives players a fun, lightweight way to celebrate together in-game. It started life on Forge 1.20.1 as "Reaction Wheel," a small wheel of pixelated emoji reactions, and grew into a full Cheer reaction for NeoForge 1.21.1 — complete with a happy face, a cheering pose, and a burst of Create-style confetti.

> 📦 Also available on [CurseForge](https://www.curseforge.com/minecraft/mc-mods/arthous-reactions).

## Versions

| Loader | Minecraft | Mod version | JDK | Source |
|---|---|---|---|---|
| forge | 1.20.1 | 1.1 | 17 | [forge/1.20.1](forge/1.20.1) |
| neoforge | 1.21.1 | 2.0.0 | 21 | [neoforge/1.21.1](neoforge/1.21.1) |

## Building from source

Install the JDK listed above for the version you want, then build from inside that folder:

```sh
cd neoforge/1.21.1
./gradlew build
```

On Windows, use `gradlew.bat build` instead. The finished jar lands in `build/libs/` for that version. The very first build will take a little longer, since Gradle needs to download itself and every dependency the project declares.

## About this repository

This repo keeps the source code and resources for every published version, each in its own folder. Local caches, test worlds, compiled output and backup copies are intentionally left out — only the real, published code lives here.

## License & credits

Every license, credit and notice file that shipped with each version has been kept as-is. Check the metadata for the version you're looking at before redistributing — publishing the source here doesn't change any declared license.
