plugins {
    id("fuzs.multiloader.multiloader-convention-plugins-fabric")
}

dependencies {
    modApi(libs.fabricapi.fabric)
}

multiloader {
    modFile {
        packagePrefix.set("impl")
        library.set(true)
        json {
            customData.put(
                "loom:injected_interfaces",
                mapOf(
                    "net/minecraft/class_6880" to listOf("net/neoforged/neoforge/registries/datamaps/IWithData<TT;>"),
                    "net/minecraft/class_2378" to listOf("net/neoforged/neoforge/registries/IRegistryExtension<TT;>"),
                    "net/minecraft/class_7225\$class_7226" to listOf("net/neoforged/neoforge/registries/datamaps/ILookupWithData<TT;>")
                )
            )
        }
    }

    mixins {
        mixin(
            "HolderFabricMixin",
            "HolderLookup\$RegistryLookup\$DelegateFabricMixin",
            "HolderLookup\$RegistryLookupFabricMixin",
            "Holder\$ReferenceFabricMixin",
            "MappedRegistry\$LookupFabricMixin",
            "MappedRegistryFabricMixin",
            "RegistryFabricMixin",
            "ReloadableServerResourcesFabricMixin",
            "TagLoaderFabricMixin"
        )
    }
}
