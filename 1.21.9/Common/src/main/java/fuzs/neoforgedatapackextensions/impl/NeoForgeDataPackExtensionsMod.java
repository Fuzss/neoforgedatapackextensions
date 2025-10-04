package fuzs.neoforgedatapackextensions.impl;

import net.minecraft.resources.ResourceLocation;

public class NeoForgeDataPackExtensionsMod extends NeoForgeDataPackExtensions {

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
