package net.nigl.client.module.modules;

import net.nigl.client.ResourcePacks;
import net.nigl.client.module.Category;
import net.nigl.client.module.Module;

/** Modul, das ein eingebautes Resource-Pack ein-/ausschaltet. */
public abstract class PackModule extends Module {
    private final String pack;

    protected PackModule(String name, String description, String pack) {
        super(name, description + " (laedt Ressourcen neu)", Category.RESOURCES);
        this.pack = pack;
    }

    @Override public void onEnable() { ResourcePacks.set(pack, true); }
    @Override public void onDisable() { ResourcePacks.set(pack, false); }

    /** Zustand mit dem tatsaechlich aktiven Pack abgleichen. */
    public void sync() {
        Boolean s = ResourcePacks.selected(pack);
        if (s != null) enabled = s;
    }
}
