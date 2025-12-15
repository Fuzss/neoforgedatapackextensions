package fuzs.neoforgedatapackextensions.impl;

import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NeoForgeDataPackExtensions {
    public static final String MOD_ID = "neoforgedatapackextensions";
    public static final String MOD_NAME = "NeoForge Data Pack Extensions";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
