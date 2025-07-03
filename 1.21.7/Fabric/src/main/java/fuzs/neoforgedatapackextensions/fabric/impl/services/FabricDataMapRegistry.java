package fuzs.neoforgedatapackextensions.fabric.impl.services;

import com.mojang.serialization.Codec;
import fuzs.neoforgedatapackextensions.api.v1.DataMapRegistry;
import fuzs.neoforgedatapackextensions.api.v1.DataMapToken;
import fuzs.neoforgedatapackextensions.fabric.api.v1.FabricDataMapToken;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.RegistryManager;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import org.jetbrains.annotations.Nullable;

public final class FabricDataMapRegistry implements DataMapRegistry {

    @Override
    @Nullable
    public <R, T> T getData(DataMapToken<R, T> token, Holder<R> holder) {
        return holder.neoforgedatapackextensions$getData(FabricDataMapToken.unwrap(token));
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
        RegistryManager.registerDataMap(type);
        return new FabricDataMapToken<>(type);
    }
}
