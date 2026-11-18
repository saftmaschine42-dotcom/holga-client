package net.nigl.client.module.modules;

import net.nigl.client.module.Category;
import net.nigl.client.module.Module;

public class NoHurtCam extends Module {
    private Double oldTilt, oldDistort;
    public NoHurtCam() { super("NoHurtCam", "Kein Wackeln bei Schaden, kein Uebelkeits-/Portal-Zucken", Category.RESOURCES); }

    @Override public void onEnable() {
        oldTilt = mc.options.damageTiltStrength().get();
        oldDistort = mc.options.screenEffectScale().get();
        apply();
    }
    private void apply() {
        mc.options.damageTiltStrength().set(0.0);
        mc.options.screenEffectScale().set(0.0);
    }
    @Override public void onTick() { apply(); }
    @Override public void onDisable() {
        if (oldTilt != null) mc.options.damageTiltStrength().set(oldTilt);
        if (oldDistort != null) mc.options.screenEffectScale().set(oldDistort);
        oldTilt = null; oldDistort = null;
    }
}
