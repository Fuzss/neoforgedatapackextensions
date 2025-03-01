/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import fuzs.neoforgedatapackextensions.impl.NeoForgeDataPackExtensions;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.registries.RegistryManager;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import org.apache.commons.lang3.function.TriConsumer;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;
import java.util.function.BiFunction;

@ApiStatus.Internal
@SuppressWarnings({ "unchecked", "rawtypes" })
public record RegistryDataMapSyncPayload<T>(ResourceKey<? extends Registry<T>> registryKey,
                                            Map<ResourceLocation, Map<ResourceKey<T>, ?>> dataMaps) implements CustomPacketPayload {
    public static final Type<RegistryDataMapSyncPayload<?>> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(
            NeoForgeDataPackExtensions.MOD_ID, "registry_data_map_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RegistryDataMapSyncPayload<?>> STREAM_CODEC = StreamCodec.ofMember(
            RegistryDataMapSyncPayload::write, RegistryDataMapSyncPayload::decode);

    public static <T> RegistryDataMapSyncPayload<T> decode(RegistryFriendlyByteBuf buf) {
        //noinspection RedundantCast javac complains about this cast
        final ResourceKey<Registry<T>> registryKey = (ResourceKey<Registry<T>>) (Object) buf.readRegistryKey();
        final Map<ResourceLocation, Map<ResourceKey<T>, ?>> attach = readMap(buf, FriendlyByteBuf::readResourceLocation, (b1, key) -> {
            final DataMapType<T, ?> dataMap = RegistryManager.getDataMap(registryKey, key);
            return b1.readMap(bf -> bf.readResourceKey(registryKey), bf -> readJsonWithRegistryCodec((RegistryFriendlyByteBuf) bf, dataMap.networkCodec()));
        });
        return new RegistryDataMapSyncPayload<>(registryKey, attach);
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeResourceKey(registryKey);
        writeMap(buf, dataMaps, FriendlyByteBuf::writeResourceLocation, (b1, key, attach) -> {
            final DataMapType<T, ?> dataMap = RegistryManager.getDataMap(registryKey, key);
            // TODO - make datamaps use stream codecs once datapack registries use them too
            b1.writeMap(attach, FriendlyByteBuf::writeResourceKey, (bf, value) -> writeJsonWithRegistryCodec((RegistryFriendlyByteBuf) bf, (Codec) dataMap.networkCodec(), value));
        });
    }

    @Override
    public Type<RegistryDataMapSyncPayload<?>> type() {
        return TYPE;
    }

    private static final Gson GSON = new Gson();

    private static <T> T readJsonWithRegistryCodec(RegistryFriendlyByteBuf buf, Codec<T> codec) {
        JsonElement jsonelement = GsonHelper.fromJson(GSON, buf.readUtf(), JsonElement.class);
        DataResult<T> dataresult = codec.parse(buf.registryAccess().createSerializationContext(JsonOps.INSTANCE), jsonelement);
        return dataresult.getOrThrow(name -> new DecoderException("Failed to decode json: " + name));
    }

    private static <T> void writeJsonWithRegistryCodec(RegistryFriendlyByteBuf buf, Codec<T> codec, T value) {
        DataResult<JsonElement> dataresult = codec.encodeStart(buf.registryAccess().createSerializationContext(JsonOps.INSTANCE), value);
        buf.writeUtf(GSON.toJson(dataresult.getOrThrow(message -> new EncoderException("Failed to encode: " + message + " " + value))));
    }

    /**
     * Variant of {@link FriendlyByteBuf#readMap(StreamDecoder, StreamDecoder)} that allows reading values
     * that depend on the key.
     */
    public static <K, V> Map<K, V> readMap(FriendlyByteBuf friendlyByteBuf, StreamDecoder<? super FriendlyByteBuf, K> keyReader, BiFunction<FriendlyByteBuf, K, V> valueReader) {
        final int size = friendlyByteBuf.readVarInt();
        final Map<K, V> map = Maps.newHashMapWithExpectedSize(size);

        for (int i = 0; i < size; ++i) {
            final K k = keyReader.decode(friendlyByteBuf);
            map.put(k, valueReader.apply(friendlyByteBuf, k));
        }

        return map;
    }

    /**
     * Variant of {@link FriendlyByteBuf#writeMap(Map, StreamEncoder, StreamEncoder)} that allows writing values
     * that depend on the key.
     */
    public static <K, V> void writeMap(FriendlyByteBuf friendlyByteBuf, Map<K, V> map, StreamEncoder<? super FriendlyByteBuf, K> keyWriter, TriConsumer<FriendlyByteBuf, K, V> valueWriter) {
        friendlyByteBuf.writeVarInt(map.size());
        map.forEach((key, value) -> {
            keyWriter.encode(friendlyByteBuf, key);
            valueWriter.accept(friendlyByteBuf, key, value);
        });
    }
}
