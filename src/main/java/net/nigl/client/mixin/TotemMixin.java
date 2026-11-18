package net.nigl.client.mixin;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.item.ItemStack;
import net.nigl.client.module.Modules;
import net.nigl.client.module.modules.TotemTweak;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class TotemMixin {
    @Inject(method = "displayItemActivation", at = @At("HEAD"), cancellable = true)
    private void nigl$totem(ItemStack stack, CallbackInfo ci) {
        TotemTweak m = Modules.get(TotemTweak.class);
        if (m != null && m.enabled) ci.cancel();
    }
}
