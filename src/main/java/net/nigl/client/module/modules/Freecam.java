package net.nigl.client.module.modules;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.player.RemotePlayer;
import net.nigl.client.module.Category;
import net.nigl.client.module.Module;
import net.nigl.client.setting.BoolSetting;
import net.nigl.client.setting.NumberSetting;
import org.lwjgl.glfw.GLFW;

import java.util.UUID;

/**
 * Freecam: Kamera-Entity loest sich vom Spieler.
 * W/S = vor/zurueck (in Blickrichtung), A/D = links/rechts, Space = hoch, Shift = runter, Strg = schneller.
 * Wichtig: Bei LivingEntity nutzt die Kamera yHeadRot (nicht yRot) - das war der Grund fuer das Zittern/falsche Richtung.
 */
public class Freecam extends Module {
    private final NumberSetting speed = add(new NumberSetting("Speed", 20, 2, 80, 1));
    private final BoolSetting blockInput = add(new BoolSetting("Block Input", true));

    private RemotePlayer cam;
    private double x, y, z;
    private float origYaw, origPitch;
    private long lastNs;

    public Freecam() { super("Freecam", "Frei umherfliegen, waehrend dein Spieler stehen bleibt", Category.RENDER); }

    @Override
    public void onEnable() {
        var p = mc.player;
        if (p == null || mc.level == null) { enabled = false; return; }
        cam = new RemotePlayer(mc.level, new GameProfile(UUID.randomUUID(), "Freecam"));
        x = p.getX();
        y = p.getEyeY() - cam.getEyeHeight();
        z = p.getZ();
        origYaw = p.getYRot();
        origPitch = p.getXRot();
        apply(p.getYRot(), p.getXRot());
        mc.setCameraEntity(cam);
        lastNs = System.nanoTime();
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.setCameraEntity(mc.player);
            mc.player.setYRot(origYaw);
            mc.player.setXRot(origPitch);
        }
        cam = null;
    }

    @Override public void onWorldLeave() { enabled = false; cam = null; }

    private void apply(float yaw, float pitch) {
        cam.setPos(x, y, z);
        cam.xo = x; cam.yo = y; cam.zo = z;
        cam.xOld = x; cam.yOld = y; cam.zOld = z;
        cam.setYRot(yaw); cam.setXRot(pitch);
        cam.yRotO = yaw; cam.xRotO = pitch;
        cam.yHeadRot = yaw; cam.yHeadRotO = yaw;
        cam.yBodyRot = yaw; cam.yBodyRotO = yaw;
    }

    /** Vor jedem Tick: Eingaben des echten Spielers abschalten, damit er stehen bleibt. */
    @Override
    public void onPreTick() {
        if (!blockInput.value) return;
        var o = mc.options;
        o.keyUp.setDown(false); o.keyDown.setDown(false);
        o.keyLeft.setDown(false); o.keyRight.setDown(false);
        o.keyJump.setDown(false); o.keyShift.setDown(false);
        o.keySprint.setDown(false);
        o.keyAttack.setDown(false); o.keyUse.setDown(false);
    }

    private static boolean down(long w, int key) { return GLFW.glfwGetKey(w, key) == GLFW.GLFW_PRESS; }

    /** Jeden Frame: Kamera bewegen (fluessig, unabhaengig vom Tick). */
    public void frame() {
        var p = mc.player;
        if (p == null || cam == null) return;
        long now = System.nanoTime();
        float dt = Math.min(0.1f, (now - lastNs) / 1e9f);
        lastNs = now;

        float yaw = p.getYRot(), pitch = p.getXRot();
        if (mc.mouseHandler.isMouseGrabbed()) {
            long w = GLFW.glfwGetCurrentContext();
            double f = (down(w, GLFW.GLFW_KEY_W) ? 1 : 0) - (down(w, GLFW.GLFW_KEY_S) ? 1 : 0);
            double s = (down(w, GLFW.GLFW_KEY_D) ? 1 : 0) - (down(w, GLFW.GLFW_KEY_A) ? 1 : 0);
            double u = (down(w, GLFW.GLFW_KEY_SPACE) ? 1 : 0) - (down(w, GLFW.GLFW_KEY_LEFT_SHIFT) ? 1 : 0);
            double yr = Math.toRadians(yaw), pr = Math.toRadians(pitch);
            // Vorwaerts = Blickrichtung, Rechts = 90 Grad rechts davon
            double lx = -Math.sin(yr) * Math.cos(pr), ly = -Math.sin(pr), lz = Math.cos(yr) * Math.cos(pr);
            double rx = -Math.cos(yr), rz = -Math.sin(yr);
            double dx = lx * f + rx * s, dy = ly * f + u, dz = lz * f + rz * s;
            double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (len > 1) { dx /= len; dy /= len; dz /= len; }
            double v = speed.value * dt * (down(w, GLFW.GLFW_KEY_LEFT_CONTROL) ? 2.5 : 1.0);
            x += dx * v; y += dy * v; z += dz * v;
        }
        apply(yaw, pitch);
    }
}
