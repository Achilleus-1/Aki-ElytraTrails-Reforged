# Aki Elytra Trails Reforged

**[Product of Achilleus](https://linktr.ee/achilleus_)** — Created and maintained by [Achilleus (Achilleus-1)](https://github.com/Achilleus-1).

Pretty trails while flying with an elytra, with presets, wingtip settings, twirls, and optional arrow and allay trails.

## Installation

For **Minecraft 1.21.1**, **NeoForge 21.1.255 or newer in the 21.1 series**, and **Java 21**. Place `aki-elytra-trails-reforged-1.21.1-1.4.9-neoforge.3.jar` in your `mods` folder. Remove older copies and the original Fabric Elytra Contrails JAR first. Download it from [Releases](https://github.com/Achilleus-1/Aki-ElytraTrails-Reforged/releases/latest), or build it using the instructions below.

The mod has no required third-party mod dependencies. For the graphical settings screen, optionally install **Cloth Config 15.0.140 or newer for NeoForge / Minecraft 1.21.1**. Open settings through NeoForge's Mods screen or assign **Open Settings** in **Options → Controls → Key Binds → Aki Elytra Trails Reforged Keybinds**. Twirl, toggle, and settings bindings start unbound. Without Cloth Config, edit `config/elytratrails.json` while the game is closed.

Enable the bundled **Arrow Trails** and **Allay Trails** packs through the Resource Packs screen if desired.

Local trails work without a server installation. To share player settings and twirls, install this same port on the server and participating clients. Its synchronization protocol is not interchangeable with the Fabric mod or its server plugins.

## Development

Source code is in `src/main/java/` and `src/client/java/`. Resources, logos, translations, and metadata are in the corresponding resources folders. Mod information and Minecraft/NeoForge versions are configured in `gradle.properties`. Existing internal mod IDs, packet IDs, Java packages, and configuration paths are preserved.

Build with a Java 21 JDK: `./gradlew build` on Linux/macOS or `.\gradlew.bat build` on Windows. The installable JAR is written to `build/libs/`. The first build downloads Gradle, NeoForge, Minecraft development artifacts, and compile-time Cloth Config.

Launch the development client with `./gradlew runClient` or `.\gradlew.bat runClient`. GitHub Actions checks the build on pushes and pull requests.

Iris and EMF detection are optional. Shader packs, Fresh Animations, EMF model variants, startup, and multiplayer require in-game verification. Flashback and Custom Player Models integrations from upstream 1.21.11 are not implemented in this port.

## Attribution

Ported from [Elytra Contrails](https://github.com/dbrighthd/elytratrails) by dbrighthd.

Licensed under **MPL-2.0**; copyright and license notices are included in [LICENSE](LICENSE) and packaged resources. Source is available in [this repository](https://github.com/Achilleus-1/Aki-ElytraTrails-Reforged).
