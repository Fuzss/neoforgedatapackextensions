package fuzs.neoforgedatapackextensions.fabric.mixin;

import fuzs.neoforgedatapackextensions.fabric.impl.NeoForgeDataPackExtensionsFabric;
import net.minecraft.server.ReloadableServerResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ReloadableServerResources.class)
abstract class ReloadableServerResourcesFabricMixin {

    @ModifyVariable(method = "lambda$loadResources$1(Lnet/minecraft/world/flag/FeatureFlagSet;Lnet/minecraft/commands/Commands$CommandSelection;Ljava/util/List;Lnet/minecraft/server/permissions/PermissionSet;Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Lnet/minecraft/server/ReloadableServerRegistries$LoadResult;)Ljava/util/concurrent/CompletionStage;",
                    at = @At(value = "STORE", ordinal = 0))
    private static ReloadableServerResources loadResources(ReloadableServerResources reloadableServerResources) {
        NeoForgeDataPackExtensionsFabric.setReloadableServerResources(reloadableServerResources);
        return reloadableServerResources;
    }
}
