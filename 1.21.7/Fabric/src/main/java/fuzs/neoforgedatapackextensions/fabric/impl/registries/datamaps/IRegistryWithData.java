package fuzs.neoforgedatapackextensions.fabric.impl.registries.datamaps;

import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

import java.util.Map;

public interface IRegistryWithData<T> {

    Map<DataMapType<T, ?>, Map<ResourceKey<T>, ?>> neoforgedatapackextensions$getDataMaps();
}
