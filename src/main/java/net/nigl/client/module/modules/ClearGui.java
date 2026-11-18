package net.nigl.client.module.modules;

import net.nigl.client.module.Category;
import net.nigl.client.module.Module;

/** Inventar/Truhen ohne Blur; das Abdunkeln entfernt ClearGuiMixin. */
public class ClearGui extends Module {
    private Integer old;
    public ClearGui() { super("ClearGui", "Durchsichtiges Inventar/Truhen (kein Blur, kein Abdunkeln)", Category.RESOURCES); }

    @Override public void onEnable() { old = mc.options.menuBackgroundBlurriness().get(); mc.options.menuBackgroundBlurriness().set(0); }
    @Override public void onTick() { mc.options.menuBackgroundBlurriness().set(0); }
    @Override public void onDisable() { if (old != null) mc.options.menuBackgroundBlurriness().set(old); old = null; }
}
