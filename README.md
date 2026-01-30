# Waystones-X
Minecraft Mod. Teleport back to activated waystones. For Survival, Adventure or Servers.
([Waystones](https://github.com/TwelveIterations/Waystones) mod fork.)

![logo_small](src/main/resources/assets/waystones/logo_small.png)

[![hub](images/badges/github.png)](https://github.com/JackOfNoneTrades/WaystonesFork/releases)
![forge](images/badges/forge.png)
<!--![modrinth](images/badges/modrinth.png)
![curse](images/badges/curse.png)-->

![spawn](images/screenshots/2026-01-30_10.24.19.png)

### Fixes:
* Nether portals aren't generated when teleporting to the Nether (@kuzuanpa)
* Fixed some rendering bugs (@kuzuanpa)

### New features:
* Clicking an activated Waystone will open the teleport menu, instead of needing to shift-click it (much more intuitive)
* If configured, Waystones show nametags with their name.
* Duplicate Waystone names are disallowed.
* If a player exits the Waystone creation menu without properly naming it, the creation/naming menu will be shown upon next interaction (instead of creating an empty-named Waystone).
* GUI Configuration.
* Configurable worldgen inside of Villages.
* Automatic activation upon naming.
* Configurable [Village Names](https://modrinth.com/mod/village-names) mod compatibility.
* Configurable teleportation XP level cost. Flat cost, distance cost, flat cross-dim cost.
* Configurable global teleportation cooldown. Cooldown status indicator.
* Waystone list sorting by Name and Distance.
* Waystone list filtering.
* "Undiscovering/Forgetting" Waystones.
* Waystone renaming.
* Pinned Waystones which appear at the top of the list.
* Global Waystones are stored in the save NBT, no more conflicts.

## Dependencies
* [UniMixins](https://modrinth.com/mod/unimixins) [![curse](images/icons/curse.png)](https://www.curseforge.com/minecraft/mc-mods/unimixins)  [![modrinth](images/icons/modrinth.png)](https://modrinth.com/mod/unimixins/versions) [![git](images/icons/git.png)](https://github.com/LegacyModdingMC/UniMixins/releases)


![vn](images/screenshots/2026-01-30_10.13.19.png)
![gui](images/screenshots/2026-01-30_10.22.35.png)
![survival](images/screenshots/2026-01-30_10.23.45.png)

## Building

`./gradlew build`.

## Credits
* BlayTheNinth for the original mod.
* kuzuanpa for some bugfixes.
* Textures and ideas from [BetterWaystonesMenu](https://github.com/Loxoz/BetterWaystonesMenu/tree/1.18.2).
* brandyyn for some fixes.
* Omgise for Chinese translation.
* [GT:NH buildscript](https://github.com/GTNewHorizons/ExampleMod1.7.10).

## License

`LgplV3 + SNEED`, formerly [MIT](https://github.com/TwelveIterations/Waystones/blob/1.7.10/LICENSE) ([archive](https://archive.md/IolUS)).

## Buy me a coffee

* [ko-fi.com](ko-fi.com/jackisasubtlejoke)
* Monero: `893tQ56jWt7czBsqAGPq8J5BDnYVCg2tvKpvwTcMY1LS79iDabopdxoUzNLEZtRTH4ewAcKLJ4DM4V41fvrJGHgeKArxwmJ`

<br>

![license](images/lgplsneed_small.png)
