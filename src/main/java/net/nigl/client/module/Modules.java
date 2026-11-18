package net.nigl.client.module;

import net.nigl.client.NiglClient;
import net.nigl.client.module.modules.*;

import java.util.ArrayList;
import java.util.List;

public final class Modules {
    public static final List<Module> ALL = new ArrayList<>();

    public static void init() {
        // Combat
        add(new KillAura());
        add(new TriggerBot());
        // Movement
        add(new Sprint());
        add(new Flight());
        add(new Speed());
        add(new Spider());
        add(new Step());
        add(new Glide());
        add(new AutoWalk());
        add(new NoJumpDelay());
        // Render
        add(new Freecam());
        add(new EntityEsp());
        add(new BlockEsp());
        add(new Nametags());
        add(new Zoom());
        // Player
        add(new NoFall());
        add(new AutoRespawn());
        add(new FastPlace());
        add(new FastBreak());
        // Resources
        add(new Fullbright());
        add(new LowFire());
        add(new SmallShield());
        add(new ShortSwords());
        add(new ClearGui());
        add(new BowCharge());
        add(new NoPumpkinBlur());
        add(new ClearWater());
        add(new TotemTweak());
        add(new VisualDurability());
        add(new NoSpeedFov());
        add(new NoHurtCam());
        // Client
        add(new Hud());
        add(new GuiSettings());
    }

    private static void add(Module m) { ALL.add(m); }

    public static void syncPacks() {
        for (Module m : ALL) if (m instanceof PackModule pm) pm.sync();
    }

    public static void preTick() {
        for (Module m : ALL) if (m.enabled) {
            try { m.onPreTick(); } catch (Throwable t) { NiglClient.LOG.error("Fehler in Modul " + m.name, t); }
        }
    }

    public static void worldLeft() {
        for (Module m : ALL) if (m.enabled) m.onWorldLeave();
    }

    public static void tick() {
        for (Module m : ALL) {
            if (!m.enabled) continue;
            try {
                m.onTick();
            } catch (Throwable t) {
                NiglClient.LOG.error("Fehler in Modul " + m.name, t);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Module> T get(Class<T> c) {
        for (Module m : ALL) if (c.isInstance(m)) return (T) m;
        return null;
    }

    public static List<Module> inCategory(Category c) {
        List<Module> out = new ArrayList<>();
        for (Module m : ALL) if (m.category == c) out.add(m);
        return out;
    }

    private Modules() { }
}
