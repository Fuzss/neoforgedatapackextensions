/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.neoforge.registries.DataMapLoader;
import net.neoforged.neoforge.registries.datamaps.*;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * A provider for {@link DataMapType data map} generation.
 */
public abstract class DataMapProvider implements DataProvider {
    protected final CompletableFuture<HolderLookup.Provider> lookupProvider;
    protected final PackOutput.PathProvider pathProvider;
    private final Map<DataMapType<?, ?>, Builder<?, ?>> builders = new HashMap<>();

    /**
     * Create a new provider.
     *
     * @param packOutput     the output location
     * @param lookupProvider a {@linkplain CompletableFuture} supplying the registries
     */
    protected DataMapProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        this.lookupProvider = lookupProvider;
        this.pathProvider = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, DataMapLoader.PATH);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        gather();

        return lookupProvider.thenCompose(provider -> {
            final DynamicOps<JsonElement> dynamicOps = provider.createSerializationContext(JsonOps.INSTANCE);

            return CompletableFuture.allOf(this.builders.entrySet().stream().map(entry -> {
                DataMapType<?, ?> type = entry.getKey();
                final Path path = this.pathProvider.json(type.id().withPrefix(DataMapLoader.getFolderLocation(type.registryKey().location()) + "/"));
                return generate(path, cache, entry.getValue(), dynamicOps);
            }).toArray(CompletableFuture[]::new));
        });
    }

    private <T, R> CompletableFuture<?> generate(Path out, CachedOutput cache, Builder<T, R> builder, DynamicOps<JsonElement> ops) {
        return CompletableFuture.supplyAsync(() -> {
            final Codec<Optional<DataMapFile<T, R>>> withConditionsCodec = ExtraCodecs.optionalEmptyMap(DataMapFile.codec(builder.registryKey, builder.type));
            return withConditionsCodec.encodeStart(ops, Optional.of(builder.build())).getOrThrow(msg -> new RuntimeException("Failed to encode %s: %s".formatted(out, msg)));
        }).thenComposeAsync(encoded -> DataProvider.saveStable(cache, encoded, out));
    }

    /**
     * Generate data map entries.
     */
    protected abstract void gather();

    @SuppressWarnings("unchecked")
    public <T, R> Builder<T, R> builder(DataMapType<R, T> type) {
        // Avoid any weird classcastexceptions at runtime if a builder was previously created with this method
        if (type instanceof AdvancedDataMapType<R, T, ?> advanced) {
            return builder(advanced);
        }
        return (Builder<T, R>) builders.computeIfAbsent(type, k -> new Builder<>(type));
    }

    @SuppressWarnings("unchecked")
    public <T, R, VR extends DataMapValueRemover<R, T>> AdvancedBuilder<T, R, VR> builder(AdvancedDataMapType<R, T, VR> type) {
        return (AdvancedBuilder<T, R, VR>) builders.computeIfAbsent(type, k -> new AdvancedBuilder<>(type));
    }

    @Override
    public String getName() {
        return "Data Maps";
    }

    public static class Builder<T, R> {
        private final Map<Either<TagKey<R>, ResourceKey<R>>, Optional<DataMapEntry<T>>> values = new LinkedHashMap<>();
        protected final List<DataMapEntry.Removal<T, R>> removals = new ArrayList<>();
        protected final ResourceKey<Registry<R>> registryKey;
        private final DataMapType<R, T> type;

        private boolean replace;

        public Builder(DataMapType<R, T> type) {
            this.type = type;
            this.registryKey = type.registryKey();
        }

        public Builder<T, R> add(ResourceKey<R> key, T value, boolean replace) {
            this.values.put(Either.right(key), Optional.of(new DataMapEntry<>(value, replace)));
            return this;
        }

        public Builder<T, R> add(ResourceLocation id, T value, boolean replace) {
            return add(ResourceKey.create(registryKey, id), value, replace);
        }

        public Builder<T, R> add(Holder<R> object, T value, boolean replace) {
            return add(object.unwrapKey().orElseThrow(), value, replace);
        }

        public Builder<T, R> add(TagKey<R> tag, T value, boolean replace) {
            this.values.put(Either.left(tag), Optional.of(new DataMapEntry<>(value, replace)));
            return this;
        }

        public Builder<T, R> remove(ResourceLocation id) {
            this.removals.add(new DataMapEntry.Removal<>(Either.right(ResourceKey.create(registryKey, id)), Optional.empty()));
            return this;
        }

        public Builder<T, R> remove(TagKey<R> tag) {
            this.removals.add(new DataMapEntry.Removal<>(Either.left(tag), Optional.empty()));
            return this;
        }

        public Builder<T, R> remove(Holder<R> value) {
            this.removals.add(new DataMapEntry.Removal<>(Either.right(value.unwrap().orThrow()), Optional.empty()));
            return this;
        }

        public Builder<T, R> replace(boolean replace) {
            this.replace = replace;
            return this;
        }

        public DataMapFile<T, R> build() {
            return new DataMapFile<>(replace, values, removals);
        }
    }

    public static class AdvancedBuilder<T, R, VR extends DataMapValueRemover<R, T>> extends Builder<T, R> {
        public AdvancedBuilder(AdvancedDataMapType<R, T, VR> type) {
            super(type);
        }

        public AdvancedBuilder<T, R, VR> remove(TagKey<R> tag, VR remover) {
            this.removals.add(new DataMapEntry.Removal<>(Either.left(tag), Optional.of(remover)));
            return this;
        }

        public AdvancedBuilder<T, R, VR> remove(Holder<R> value, VR remover) {
            this.removals.add(new DataMapEntry.Removal<>(Either.right(value.unwrap().orThrow()), Optional.of(remover)));
            return this;
        }

        public AdvancedBuilder<T, R, VR> remove(ResourceLocation id, VR remover) {
            this.removals.add(new DataMapEntry.Removal<>(Either.right(ResourceKey.create(registryKey, id)), Optional.of(remover)));
            return this;
        }
    }
}
