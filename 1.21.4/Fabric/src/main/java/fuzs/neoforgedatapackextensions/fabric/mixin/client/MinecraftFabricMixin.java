package fuzs.neoforgedatapackextensions.fabric.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import fuzs.neoforgedatapackextensions.fabric.impl.registries.datamaps.IRegistryWithData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
abstract class MinecraftFabricMixin {

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;Z)V", at = @At("RETURN"))
    public void disconnect(Screen screen, boolean keepResourcePacks, CallbackInfo callback) {
        //same injection point as Fabric Registry Sync Api f or unmapping registries
        BuiltInRegistries.REGISTRY.forEach((Registry<?> registry) -> {
            ((IRegistryWithData<?>) registry).neoforgedatapackextensions$getDataMaps().clear();
        });
    }
}
