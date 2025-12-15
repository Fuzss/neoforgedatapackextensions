package fuzs.neoforgedatapackextensions.neoforge.api.v1;

import com.mojang.serialization.Codec;
import fuzs.neoforgedatapackextensions.api.v1.DataMapToken;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import org.jspecify.annotations.Nullable;

public record NeoForgeDataMapToken<R, T>(DataMapType<R, T> type) implements DataMapToken<R, T> {

    @Override
    public ResourceKey<Registry<R>> registryKey() {
        return this.type.registryKey();
    }

    @Override
    public Identifier id() {
        return this.type.id();
    }

    @Override
    public Codec<T> codec() {
        return this.type.codec();
    }

    @Override
    public @Nullable Codec<T> networkCodec() {
        return this.type.networkCodec();
    }

    @Override
    public boolean mandatorySync() {
        return this.type.mandatorySync();
    }

    public static <R, T> DataMapType<R, T> unwrap(DataMapToken<R, T> token) {
        return ((NeoForgeDataMapToken<R, T>) token).type;
    }
}
