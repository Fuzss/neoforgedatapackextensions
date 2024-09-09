# NeoForge Data Pack Extensions

A Minecraft mod. Downloads can be found on [CurseForge](https://www.curseforge.com/members/fuzs_/projects) and [Modrinth](https://modrinth.com/user/Fuzs).

![](https://raw.githubusercontent.com/Fuzss/modresources/main/pages/data/neoforgedatapackextensions/banner.png)

## About the project
NeoForge Data Pack Extensions brings some very useful additions to Minecraft's [data packs](https://minecraft.wiki/w/Data_pack) introduced by the [NeoForge](https://neoforged.net/) mod loader to other modding ecosystems, mainly the [Fabric](https://fabricmc.net/) loader.

Most notably this includes a full fletched port of [data maps](https://docs.neoforged.net/docs/resources/server/datamaps/), as well as the ability to remove entries from [tags](https://docs.neoforged.net/docs/resources/server/tags).

## Data maps
A comprehensive guide to data maps is available via the [NeoForge wiki](https://docs.neoforged.net/docs/resources/server/datamaps/).

New data maps should be registered on Fabric in `ModInitializer::onInitialize` via `RegistryManager::registerDataMap` as opposed to `RegisterDataMapTypesEvent` on NeoForge.

Also NeoForge's `DataMapsUpdatedEvent` is implemented in an adapted form as `DataMapsUpdatedCallback`.

## Tags removals
A comprehensive guide to tags on NeoForge is once again available via the [wiki](https://docs.neoforged.net/docs/resources/server/tags).

An additional optional field `remove` is added to tag defintions, following the same format as vanilla's `values` field. Included values are removed from all subsequent tag files for this tag without having to override the whole file via `replace`.
