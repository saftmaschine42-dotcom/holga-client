package net.nigl.client;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.PackRepository;

import java.util.ArrayList;
import java.util.List;

/** Schaltet die eingebauten Nigl-Resource-Packs an/aus (loest einen Resource-Reload aus). */
public final class ResourcePacks {
    private static String find(PackRepository repo, String name) {
        for (String id : repo.getAvailableIds()) if (id.contains(NiglClient.ID) && id.contains(name)) return id;
        return null;
    }

    /** true/false = Pack aktiv/inaktiv, null = Pack nicht gefunden. */
    public static Boolean selected(String name) {
        PackRepository repo = Minecraft.getInstance().getResourcePackRepository();
        String id = find(repo, name);
        return id == null ? null : repo.getSelectedIds().contains(id);
    }

    public static void set(String name, boolean on) {
        Minecraft mc = Minecraft.getInstance();
        PackRepository repo = mc.getResourcePackRepository();
        String id = find(repo, name);
        if (id == null) { NiglClient.LOG.warn("Resource-Pack '{}' nicht gefunden", name); return; }
        List<String> sel = new ArrayList<>(repo.getSelectedIds());
        if (on == sel.contains(id)) return;
        if (on) sel.add(id); else sel.remove(id);
        repo.setSelected(sel);
        mc.options.updateResourcePacks(repo); // speichert + laedt Ressourcen neu
    }

    private ResourcePacks() { }
}
