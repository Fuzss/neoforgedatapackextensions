package fuzs.neoforgedatapackextensions.fabric.impl;

import fuzs.neoforgedatapackextensions.impl.NeoForgeDataPackExtensions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.v1.DataResourceLoader;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.neoforged.neoforge.network.configuration.RegistryDataMapNegotiation;
import net.neoforged.neoforge.network.payload.KnownRegistryDataMapsPayload;
import net.neoforged.neoforge.network.payload.KnownRegistryDataMapsReplyPayload;
import net.neoforged.neoforge.network.payload.RegistryDataMapSyncPayload;
import net.neoforged.neoforge.registries.DataMapLoader;
import net.neoforged.neoforge.registries.IRegistryExtension;
import net.neoforged.neoforge.registries.RegistryManager;
import org.jspecify.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.*;

public class NeoForgeDataPackExtensionsFabric implements ModInitializer {
    private static final ThreadLocal<WeakReference<ReloadableServerResources>> RELOADABLE_SERVER_RESOURCES_REFERENCE = ThreadLocal.withInitial(
            () -> new WeakReference<>(null));

    @Nullable
    private static DataMapLoader dataMapLoader;

    @Override
    public void onInitialize() {
        registerEventHandlers();
        registerNetworkMessages();
        DataResourceLoader.get()
                .registerReloader(NeoForgeDataPackExtensions.id(DataMapLoader.PATH),
                        (HolderLookup.Provider registries) -> {
                            ReloadableServerResources reloadableServerResources = RELOADABLE_SERVER_RESOURCES_REFERENCE.get()
                                    .get();
                            Objects.requireNonNull(reloadableServerResources, "reloadable server resources is null");
                            return dataMapLoader = new DataMapLoader((RegistryAccess) reloadableServerResources.fullRegistries()
                                    .lookup());
                        });
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

    public static void setReloadableServerResources(ReloadableServerResources reloadableServerResources) {
        RELOADABLE_SERVER_RESOURCES_REFERENCE.set(new WeakReference<>(reloadableServerResources));
    }

    private static <T> void handleSync(ServerPlayer player, Registry<T> registry, Collection<Identifier> attachments) {
        if (attachments.isEmpty()) return;
        final Map<Identifier, Map<ResourceKey<T>, ?>> att = new HashMap<>();
        attachments.forEach(key -> {
            final var attach = RegistryManager.getDataMap(registry.key(), key);
            if (attach == null || attach.networkCodec() == null) return;
            att.put(key, ((IRegistryExtension<T>) registry).neoforgedatapackextensions$getDataMap(attach));
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
