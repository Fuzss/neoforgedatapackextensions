package fuzs.neoforgedatapackextensions.fabric.mixin;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.ILookupWithData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(HolderLookup.RegistryLookup.Delegate.class)
interface HolderLookup$RegistryLookup$DelegateFabricMixin<T> extends HolderLookup.RegistryLookup<T>, ILookupWithData<T> {

    @Shadow
    RegistryLookup<T> parent();

    @Override
    default <A> @Nullable A neoforgedatapackextensions$getData(DataMapType<T, A> attachment, ResourceKey<T> key) {
        return this.parent().neoforgedatapackextensions$getData(attachment, key);
    }
}
