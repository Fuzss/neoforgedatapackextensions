package fuzs.neoforgedatapackextensions.neoforge.impl.services;

import com.mojang.serialization.Codec;
import fuzs.neoforgedatapackextensions.api.v1.DataMapRegistry;
import fuzs.neoforgedatapackextensions.api.v1.DataMapToken;
import fuzs.neoforgedatapackextensions.neoforge.api.v1.NeoForgeDataMapToken;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class NeoForgeDataMapRegistry implements DataMapRegistry {

    @Override
    @Nullable
    public <R, T> T getData(DataMapToken<R, T> token, Holder<R> holder) {
        return holder.getData(NeoForgeDataMapToken.unwrap(token));
    }

    @Override
    public <R, T> DataMapToken<R, T> register(ResourceLocation id, ResourceKey<Registry<R>> registry, Codec<T> codec) {
        return this.register(DataMapType.builder(id, registry, codec).build());
    }

    @Override
    public <R, T> DataMapToken<R, T> register(ResourceLocation id, ResourceKey<Registry<R>> registry, Codec<T> codec, Codec<T> networkCodec, boolean mandatory) {
        return this.register(DataMapType.builder(id, registry, codec).synced(networkCodec, mandatory).build());
    }

    private <R, T> DataMapToken<R, T> register(DataMapType<R, T> type) {
        IEventBus eventBus = ModLoadingContext.get().getActiveContainer().getEventBus();
        Objects.requireNonNull(eventBus, "mod event bus is null");
        eventBus.addListener((final RegisterDataMapTypesEvent evt) -> {
            evt.register(type);
        });
        return new NeoForgeDataMapToken<>(type);
    }
}
