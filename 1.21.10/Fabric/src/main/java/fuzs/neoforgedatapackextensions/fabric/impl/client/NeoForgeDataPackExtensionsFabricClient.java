package fuzs.neoforgedatapackextensions.fabric.impl.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.neoforged.neoforge.network.payload.KnownRegistryDataMapsPayload;
import net.neoforged.neoforge.network.payload.RegistryDataMapSyncPayload;
import net.neoforged.neoforge.client.registries.ClientRegistryManager;

public class NeoForgeDataPackExtensionsFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        registerNetworkMessages();
    }

    private static void registerNetworkMessages() {
        ClientConfigurationNetworking.registerGlobalReceiver(KnownRegistryDataMapsPayload.TYPE,
                ClientRegistryManager::handleKnownDataMaps
        );
        ClientPlayNetworking.registerGlobalReceiver(RegistryDataMapSyncPayload.TYPE,
                ClientRegistryManager::handleDataMapSync
        );
    }
}
