package net.nigl.client.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.nigl.client.module.Modules;
import net.nigl.client.module.modules.ClearGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ClearGuiMixin {
    @Inject(method = "extractTransparentBackground", at = @At("HEAD"), cancellable = true)
    private void nigl$clear(GuiGraphicsExtractor g, CallbackInfo ci) {
        ClearGui m = Modules.get(ClearGui.class);
        if (m != null && m.enabled && (Object) this instanceof AbstractContainerScreen<?>) ci.cancel();
    }
}
