package net.neoforged.neoforge.registries.datamaps;

import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.Nullable;

public interface ILookupWithData<T> {

    @Nullable default <A> A neoforgedatapackextensions$getData(DataMapType<T, A> attachment, ResourceKey<T> key) {
        return null;
    }
}
