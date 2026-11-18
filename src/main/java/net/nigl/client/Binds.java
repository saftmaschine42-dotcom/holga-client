package net.nigl.client;

import net.minecraft.client.Minecraft;
import net.nigl.client.module.Module;
import net.nigl.client.module.Modules;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

/** Eigene Keybinds: wird jeden Frame per GLFW abgefragt (unabhaengig von den Vanilla-Tastenbelegungen). */
public final class Binds {
    private static final Map<Module, Boolean> WAS = new HashMap<>();

    public static void poll() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        boolean active = mc.mouseHandler.isMouseGrabbed();
        long w = GLFW.glfwGetCurrentContext();
        if (w == 0L) return;
        for (Module m : Modules.ALL) {
            int k = m.bind.code;
            if (k <= 0) { WAS.remove(m); continue; }
            boolean down = GLFW.glfwGetKey(w, k) == GLFW.GLFW_PRESS;
            boolean prev = WAS.getOrDefault(m, false);
            if (active && down && !prev) m.toggle();
            WAS.put(m, down);
        }
    }

    public static String name(int code) {
        if (code <= 0) return "None";
        switch (code) {
            case 32: return "Space";
            case 256: return "Esc";
            case 257: return "Enter";
            case 258: return "Tab";
            case 259: return "Back";
            case 260: return "Ins";
            case 261: return "Del";
            case 262: return "Right";
            case 263: return "Left";
            case 264: return "Down";
            case 265: return "Up";
            case 266: return "PgUp";
            case 267: return "PgDn";
            case 268: return "Home";
            case 269: return "End";
            case 340: return "LShift";
            case 341: return "LCtrl";
            case 342: return "LAlt";
            case 344: return "RShift";
            case 345: return "RCtrl";
            case 346: return "RAlt";
            default: break;
        }
        if (code >= 290 && code <= 301) return "F" + (code - 289);
        if (code >= 320 && code <= 329) return "Num" + (code - 320);
        String n = GLFW.glfwGetKeyName(code, 0);
        return n != null ? n.toUpperCase() : "K" + code;
    }

    private Binds() { }
}
