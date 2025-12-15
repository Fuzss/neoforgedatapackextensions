package fuzs.neoforgedatapackextensions.api.v1;

import com.mojang.serialization.Codec;
import fuzs.neoforgedatapackextensions.impl.services.ServiceProviderHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public interface DataMapRegistry {
    DataMapRegistry INSTANCE = ServiceProviderHelper.load(DataMapRegistry.class);

    @Nullable <R, T> T getData(DataMapToken<R, T> token, Holder<R> holder);

    <R, T> DataMapToken<R, T> register(Identifier id, ResourceKey<Registry<R>> registry, Codec<T> codec);

    <R, T> DataMapToken<R, T> register(Identifier id, ResourceKey<Registry<R>> registry, Codec<T> codec, Codec<T> networkCodec, boolean mandatory);
}
