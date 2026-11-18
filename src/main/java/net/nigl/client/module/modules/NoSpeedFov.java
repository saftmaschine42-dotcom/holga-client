package net.nigl.client.module.modules;

import net.nigl.client.module.Category;
import net.nigl.client.module.Module;

public class NoSpeedFov extends Module {
    private Double old;
    public NoSpeedFov() { super("NoSpeedFov", "Kein FOV-Zoom bei Speed/Sprint (FOV-Effekte 0%)", Category.RESOURCES); }

    @Override public void onEnable() { old = mc.options.fovEffectScale().get(); mc.options.fovEffectScale().set(0.0); }
    @Override public void onTick() { mc.options.fovEffectScale().set(0.0); }
    @Override public void onDisable() { if (old != null) mc.options.fovEffectScale().set(old); old = null; }
}
