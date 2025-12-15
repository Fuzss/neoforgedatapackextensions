package fuzs.neoforgedatapackextensions.fabric.mixin;

import fuzs.neoforgedatapackextensions.fabric.impl.registries.datamaps.IRegistryWithData;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.IRegistryExtension;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.IdentityHashMap;
import java.util.Map;

@Mixin(MappedRegistry.class)
abstract class MappedRegistryFabricMixin<T> implements IRegistryExtension<T>, IRegistryWithData<T> {
    @Unique
    final Map<DataMapType<T, ?>, Map<ResourceKey<T>, ?>> neoforgedatapackextensions$dataMaps = new IdentityHashMap<>();

    @Override
    public <A> @Nullable A neoforgedatapackextensions$getData(DataMapType<T, A> type, ResourceKey<T> key) {
        final var innerMap = this.neoforgedatapackextensions$dataMaps.get(type);
        return innerMap == null ? null : (A) innerMap.get(key);
    }

    @Override
    public <A> Map<ResourceKey<T>, A> neoforgedatapackextensions$getDataMap(DataMapType<T, A> type) {
        return (Map<ResourceKey<T>, A>) this.neoforgedatapackextensions$dataMaps.getOrDefault(type, Map.of());
    }

    @Override
    public Map<DataMapType<T, ?>, Map<ResourceKey<T>, ?>> neoforgedatapackextensions$getDataMaps() {
        return this.neoforgedatapackextensions$dataMaps;
    }
}
