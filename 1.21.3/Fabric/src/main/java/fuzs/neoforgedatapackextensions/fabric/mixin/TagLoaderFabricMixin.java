package fuzs.neoforgedatapackextensions.fabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import fuzs.neoforgedatapackextensions.fabric.impl.tags.RemovedTagEntry;
import net.minecraft.tags.TagFile;
import net.minecraft.tags.TagLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;
import java.util.SequencedSet;
import java.util.function.Consumer;

@Mixin(value = TagLoader.class, priority = 800)
abstract class TagLoaderFabricMixin<T> {

    @ModifyReceiver(
            method = "load", at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/serialization/Codec;parse(Lcom/mojang/serialization/Dynamic;)Lcom/mojang/serialization/DataResult;"
    )
    )
    public Codec<TagFile> load(Codec<TagFile> codec, Dynamic<?> input) {
        // switch the vanilla tag entry codec with our own that supports reading the 'remove' field
        return RemovedTagEntry.CODEC;
    }

    @ModifyArg(
            method = "tryBuildTag",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/tags/TagEntry;build(Lnet/minecraft/tags/TagEntry$Lookup;Ljava/util/function/Consumer;)Z"
            )
    )
    private Consumer<T> tryBuildTag(Consumer<T> consumer, @Local SequencedSet<T> sequencedSet, @Local TagLoader.EntryWithSource entryWithSource) {
        return entryWithSource.entry() instanceof RemovedTagEntry ? sequencedSet::remove : consumer;
    }

    @WrapWithCondition(
            method = "tryBuildTag", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z")
    )
    private boolean tryBuildTag(List<TagLoader.EntryWithSource> entries, Object entryWithSource) {
        return !(((TagLoader.EntryWithSource) entryWithSource).entry() instanceof RemovedTagEntry);
    }
}
