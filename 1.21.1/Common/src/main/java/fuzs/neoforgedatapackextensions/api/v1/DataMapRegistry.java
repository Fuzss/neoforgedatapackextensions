package fuzs.neoforgedatapackextensions.api.v1;

import com.mojang.serialization.Codec;
import fuzs.neoforgedatapackextensions.impl.services.ServiceProviderHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public interface DataMapRegistry {
    DataMapRegistry INSTANCE = ServiceProviderHelper.load(DataMapRegistry.class);

    @Nullable
    <R, T> T getData(DataMapToken<R, T> token, Holder<R> holder);

    <R, T> DataMapToken<R, T> register(ResourceLocation id, ResourceKey<Registry<R>> registry, Codec<T> codec);

    <R, T> DataMapToken<R, T> register(ResourceLocation id, ResourceKey<Registry<R>> registry, Codec<T> codec, Codec<T> networkCodec, boolean mandatory);
}
