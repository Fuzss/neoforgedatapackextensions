/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import com.google.common.collect.Maps;
import fuzs.neoforgedatapackextensions.impl.NeoForgeDataPackExtensions;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@ApiStatus.Internal
public record KnownRegistryDataMapsReplyPayload(
        Map<ResourceKey<? extends Registry<?>>, Collection<ResourceLocation>> dataMaps) implements CustomPacketPayload {
    public static final Type<KnownRegistryDataMapsReplyPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(
            NeoForgeDataPackExtensions.MOD_ID, "known_registry_data_maps_reply"));
    public static final StreamCodec<FriendlyByteBuf, KnownRegistryDataMapsReplyPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(
                    Maps::newHashMapWithExpectedSize,
                    ResourceLocation.STREAM_CODEC.map(ResourceKey::createRegistryKey, ResourceKey::location),
                    ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.collection(ArrayList::new))),
            KnownRegistryDataMapsReplyPayload::dataMaps,
            KnownRegistryDataMapsReplyPayload::new);

    @Override
    public Type<KnownRegistryDataMapsReplyPayload> type() {
        return TYPE;
    }
}
