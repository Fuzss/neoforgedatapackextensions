package fuzs.neoforgedatapackextensions.fabric.impl;

import fuzs.neoforgedatapackextensions.impl.NeoForgeDataPackExtensionsMod;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.neoforged.neoforge.network.configuration.RegistryDataMapNegotiation;
import net.neoforged.neoforge.network.payload.KnownRegistryDataMapsPayload;
import net.neoforged.neoforge.network.payload.KnownRegistryDataMapsReplyPayload;
import net.neoforged.neoforge.network.payload.RegistryDataMapSyncPayload;
import net.neoforged.neoforge.registries.DataMapLoader;
import net.neoforged.neoforge.registries.RegistryManager;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;

public class NeoForgeDataPackExtensionsFabric implements ModInitializer {
    @Nullable
    private static DataMapLoader dataMapLoader;

    @Override
    public void onInitialize() {
        registerEventHandlers();
        registerNetworkMessages();
    }

    private static void registerEventHandlers() {
        CommonLifecycleEvents.TAGS_LOADED.register((RegistryAccess registries, boolean client) -> {
            if (!client) {
                Objects.requireNonNull(dataMapLoader, "data map loader is null");
                dataMapLoader.apply();
            }
        });
        ServerConfigurationConnectionEvents.CONFIGURE.register((ServerConfigurationPacketListenerImpl handler, MinecraftServer server) -> {
            // These can always be registered, they detect the listener connection type internally and will skip themselves.
            handler.addTask(new RegistryDataMapNegotiation(handler));
        });
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((ServerPlayer player, boolean joined) -> {
            RegistryManager.getDataMaps().forEach((registry, values) -> {
                final var regOpt = player.level().getServer().overworld().registryAccess().lookup(registry);
                if (regOpt.isEmpty()) return;
                if (!ServerPlayNetworking.canSend(player, RegistryDataMapSyncPayload.TYPE)) {
                    return;
                }
                // Note: don't send data maps over in-memory connections for normal registries, else the client-side handling will wipe non-synced data maps.
                // Sending them for synced datapack registries is fine and required as those registries are recreated on the client
                if (player.connection.connection.isMemoryConnection()
                        && !RegistrySynchronization.isNetworkable(registry)) {
                    return;
                }
                final var playerMaps = player.connection.connection.channel.attr(RegistryManager.ATTRIBUTE_KNOWN_DATA_MAPS)
                        .get();
                if (playerMaps == null) return; // Skip gametest players for instance
                handleSync(player, regOpt.get(), playerMaps.getOrDefault(registry, List.of()));
            });
        });
    }

    public static void onAddDataPackReloadListeners(ReloadableServerResources serverResources, HolderLookup.Provider lookupWithUpdatedTags, BiConsumer<ResourceLocation, PreparableReloadListener> reloadListenerConsumer) {
        reloadListenerConsumer.accept(NeoForgeDataPackExtensionsMod.id(DataMapLoader.PATH),
                dataMapLoader = new DataMapLoader((RegistryAccess) serverResources.fullRegistries().lookup()));
    }

    private static <T> void handleSync(ServerPlayer player, Registry<T> registry, Collection<ResourceLocation> attachments) {
        if (attachments.isEmpty()) return;
        final Map<ResourceLocation, Map<ResourceKey<T>, ?>> att = new HashMap<>();
        attachments.forEach(key -> {
            final var attach = RegistryManager.getDataMap(registry.key(), key);
            if (attach == null || attach.networkCodec() == null) return;
            att.put(key, registry.neoforgedatapackextensions$getDataMap(attach));
        });
        if (!att.isEmpty()) {
            ServerPlayNetworking.send(player, new RegistryDataMapSyncPayload<>(registry.key(), att));
        }
    }

    private static void registerNetworkMessages() {
        PayloadTypeRegistry.configurationC2S()
                .register(KnownRegistryDataMapsReplyPayload.TYPE, KnownRegistryDataMapsReplyPayload.STREAM_CODEC);
        PayloadTypeRegistry.configurationS2C()
                .register(KnownRegistryDataMapsPayload.TYPE, KnownRegistryDataMapsPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C()
                .register(RegistryDataMapSyncPayload.TYPE, RegistryDataMapSyncPayload.STREAM_CODEC);
        ServerConfigurationNetworking.registerGlobalReceiver(KnownRegistryDataMapsReplyPayload.TYPE,
                RegistryManager::handleKnownDataMapsReply);
    }
}
