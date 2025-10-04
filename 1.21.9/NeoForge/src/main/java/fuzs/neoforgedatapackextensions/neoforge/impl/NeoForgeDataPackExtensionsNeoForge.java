package fuzs.neoforgedatapackextensions.neoforge.impl;

import fuzs.neoforgedatapackextensions.impl.NeoForgeDataPackExtensions;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@Mod(NeoForgeDataPackExtensions.MOD_ID)
public class NeoForgeDataPackExtensionsNeoForge {

    public NeoForgeDataPackExtensionsNeoForge(ModContainer modContainer) {
        registerLoadingHandlers(modContainer.getEventBus());
    }

    private static void registerLoadingHandlers(IEventBus eventBus) {
        eventBus.addListener((final GatherDataEvent.Client event) -> {
            event.getGenerator()
                    .addProvider(true,
                            PackMetadataGenerator.forFeaturePack(event.getGenerator().getPackOutput(),
                                    Component.literal(event.getModContainer().getModInfo().getDescription())));
        });
    }

    public static boolean isDevelopmentEnvironment(String modId) {
        if (FMLEnvironment.isProduction()) {
            return false;
        } else {
            return Boolean.getBoolean(modId + ".isDevelopmentEnvironment");
        }
    }
}
