package fuzs.neoforgedatapackextensions.fabric.mixin;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.IRegistryExtension;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.ILookupWithData;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.core.MappedRegistry$1")
abstract class MappedRegistry$LookupFabricMixin<T> implements HolderLookup.RegistryLookup<T>, ILookupWithData<T> {
    @Shadow
    @Final
    MappedRegistry<T> this$0;

    @Override
    public <A> @Nullable A neoforgedatapackextensions$getData(DataMapType<T, A> attachment, ResourceKey<T> key) {
        return ((IRegistryExtension<T>) this.this$0).neoforgedatapackextensions$getData(attachment, key);
    }
}
