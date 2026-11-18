package net.nigl.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.nigl.client.Config;
import net.nigl.client.Draw;
import net.nigl.client.Theme;
import net.nigl.client.setting.ListSetting;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Auswahl-Menue mit Suchfeld: ALLE Bloecke bzw. ALLE Entities aus der Registry. */
public class PickerScreen extends Screen {
    private record Entry(String id, String name, String lower) { }

    private static final int ROW = 12;
    private final Screen parent;
    private final ListSetting set;
    private final List<Entry> all = new ArrayList<>();
    private List<Entry> shown = new ArrayList<>();
    private String query = "";
    private boolean onlySelected = false;
    private int scroll = 0;
    private boolean draggingBar = false;

    public PickerScreen(Screen parent, ListSetting set) {
        super(Component.literal("Nigl Picker"));
        this.parent = parent;
        this.set = set;
        if (set.kind == ListSetting.Kind.BLOCK) {
            for (Block b : BuiltInRegistries.BLOCK) {
                if (b.defaultBlockState().isAir()) continue;
                String id = BuiltInRegistries.BLOCK.getKey(b).toString();
                String n = b.getName().getString();
                all.add(new Entry(id, n, (n + " " + id).toLowerCase(Locale.ROOT)));
            }
        } else {
            for (EntityType<?> t : BuiltInRegistries.ENTITY_TYPE) {
                String id = BuiltInRegistries.ENTITY_TYPE.getKey(t).toString();
                String n = t.getDescription().getString();
                all.add(new Entry(id, n, (n + " " + id).toLowerCase(Locale.ROOT)));
            }
        }
        all.sort(Comparator.comparing(Entry::name));
        refilter();
    }

    private void refilter() {
        String q = query.toLowerCase(Locale.ROOT).trim();
        List<Entry> out = new ArrayList<>();
        for (Entry e : all) {
            if (onlySelected && !set.has(e.id())) continue;
            if (!q.isEmpty() && !e.lower().contains(q)) continue;
            out.add(e);
        }
        out.sort(Comparator.comparing((Entry e) -> !set.has(e.id())).thenComparing(Entry::name));
        shown = out;
        scroll = 0;
    }

    // ---- Layout: Panel nutzt fast die ganze Bildschirmhoehe ----
    private int pw() { return Math.min(320, width - 16); }
    private int ph() { return height - 16; }
    private int px() { return (width - pw()) / 2; }
    private int py() { return 8; }
    private int listTop() { return py() + 64; }
    private int visible() { return Math.max(1, (py() + ph() - 22 - listTop()) / ROW); }
    private int maxScroll() { return Math.max(0, shown.size() - visible()); }
    private void clampScroll() { scroll = Math.max(0, Math.min(maxScroll(), scroll)); }

    private int[] btn(int i) {
        int gap = 5, w = (pw() - 20 - 2 * gap) / 3;
        return new int[]{px() + 10 + i * (w + gap), py() + 46, w, 14};
    }

    private int[] bar() { // Scrollbar-Track: x, y, w, h
        return new int[]{px() + pw() - 11, listTop(), 5, visible() * ROW};
    }

    private void barTo(double my) {
        int[] b = bar();
        int vis = visible();
        int barH = Math.max(16, b[3] * vis / Math.max(1, shown.size()));
        double t = (my - b[1] - barH / 2.0) / Math.max(1, b[3] - barH);
        scroll = (int) Math.round(Math.max(0, Math.min(1, t)) * maxScroll());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        super.extractRenderState(g, mx, my, delta);
        int px = px(), py = py(), pw = pw(), ph = ph();
        int acc = Theme.accent(0.3f);
        Draw.glass(g, px, py, pw, ph, 12);
        Draw.shimmer(g, px, py, pw, 12);

        String title = (set.kind == ListSetting.Kind.BLOCK ? "BLOCKS" : "ENTITIES") + " - " + set.name;
        Theme.bold(g, title, px + 12, py + 9, Theme.TEXT);
        String cnt = set.ids.size() + " gewaehlt / " + all.size() + " gesamt";
        Theme.text(g, cnt, px + pw - 12 - Theme.width(cnt), py + 9, Theme.GREY);

        // Suchfeld
        Draw.roundRect(g, px + 10, py + 24, pw - 20, 16, 8, 0x30FFFFFF);
        boolean blink = (System.currentTimeMillis() / 500) % 2 == 0;
        if (query.isEmpty()) Theme.text(g, "Suche tippen...", px + 18, py + 28, Theme.GREY);
        else Theme.text(g, query + (blink ? "|" : ""), px + 18, py + 28, Theme.TEXT);

        // Buttons
        String[] labels = {"Alle gefilterten", "Auswahl leeren", onlySelected ? "Nur Auswahl: AN" : "Nur Auswahl: AUS"};
        for (int i = 0; i < 3; i++) {
            int[] b = btn(i);
            boolean hov = mx >= b[0] && mx < b[0] + b[2] && my >= b[1] && my < b[1] + b[3];
            boolean on = i == 2 && onlySelected;
            Draw.roundRect(g, b[0], b[1], b[2], b[3], 7, on ? Theme.withAlpha(acc, 0xD0) : Theme.withAlpha(0xFFFFFF, hov ? 0x38 : 0x22));
            String l = labels[i];
            Theme.text(g, l, b[0] + (b[2] - Theme.width(l)) / 2, b[1] + 3, on ? Theme.onAccent(acc) : Theme.TEXT, !on);
        }

        // Liste
        clampScroll();
        int vis = visible(), top = listTop();
        for (int i = 0; i < vis; i++) {
            int idx = scroll + i;
            if (idx >= shown.size()) break;
            Entry e = shown.get(idx);
            int ry = top + i * ROW;
            boolean hov = mx >= px + 8 && mx < px + pw - 14 && my >= ry && my < ry + ROW;
            boolean sel = set.has(e.id());
            if (hov) Draw.roundRect(g, px + 8, ry, pw - 22, ROW - 1, 5, 0x22FFFFFF);
            Draw.roundRect(g, px + 13, ry + 2, 8, 8, 3, sel ? Theme.withAlpha(acc, 0xF0) : 0x40FFFFFF);
            if (sel) g.fill(px + 15, ry + 5, px + 18, ry + 7, Theme.onAccent(acc));
            String name = e.name();
            int avail = pw - 22 - 40;
            while (name.length() > 3 && Theme.width(name) > avail * 0.55) name = name.substring(0, name.length() - 1);
            Theme.text(g, name, px + 27, ry + 2, sel ? 0xFFFFFFFF : Theme.TEXT);
            String id = e.id().startsWith("minecraft:") ? e.id().substring(10) : e.id();
            while (id.length() > 3 && Theme.width(id) > avail * 0.42) id = id.substring(0, id.length() - 1);
            Theme.text(g, id, px + pw - 18 - Theme.width(id), ry + 2, Theme.GREY);
        }
        if (shown.isEmpty()) Theme.text(g, "Nichts gefunden", px + 16, top + 3, Theme.GREY);

        // Scrollbar (ziehbar)
        int[] b = bar();
        Draw.roundRect(g, b[0], b[1], b[2], b[3], 2, 0x22FFFFFF);
        if (shown.size() > vis) {
            int barH = Math.max(16, b[3] * vis / shown.size());
            int barY = b[1] + (b[3] - barH) * scroll / Math.max(1, maxScroll());
            Draw.roundRect(g, b[0], barY, b[2], barH, 2, draggingBar ? 0xCCFFFFFF : 0x88FFFFFF);
        }

        // Fussleiste: Position + Hinweis
        int from = shown.isEmpty() ? 0 : scroll + 1, to = Math.min(shown.size(), scroll + vis);
        String foot = "Zeige " + from + "-" + to + " von " + shown.size() + "   |   Mausrad / Pfeile / Bild hoch-runter / Scrollbar ziehen";
        Theme.text(g, foot, px + 12, py + ph - 14, Theme.GREY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x(), my = event.y();
        if (event.button() != 0) return super.mouseClicked(event, doubleClick);
        // Scrollbar
        int[] bb = bar();
        if (mx >= bb[0] - 3 && mx < bb[0] + bb[2] + 3 && my >= bb[1] && my < bb[1] + bb[3]) {
            draggingBar = true;
            barTo(my);
            return true;
        }
        for (int i = 0; i < 3; i++) {
            int[] b = btn(i);
            if (mx >= b[0] && mx < b[0] + b[2] && my >= b[1] && my < b[1] + b[3]) {
                if (i == 0) { for (Entry e : shown) set.add(e.id()); }
                else if (i == 1) { set.clear(); if (onlySelected) refilter(); }
                else { onlySelected = !onlySelected; refilter(); }
                return true;
            }
        }
        int top = listTop();
        if (mx >= px() + 8 && mx < px() + pw() - 14 && my >= top) {
            int idx = scroll + (int) ((my - top) / ROW);
            if (idx >= 0 && idx < shown.size() && idx < scroll + visible()) {
                set.toggle(shown.get(idx).id());
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (draggingBar) { barTo(event.y()); return true; }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        draggingBar = false;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        double d = scrollY != 0 ? scrollY : scrollX;
        scroll -= (int) Math.signum(d) * 3;
        clampScroll();
        return true;
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        query += event.codepointAsString();
        refilter();
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int k = event.key();
        switch (k) {
            case 259 -> { // Backspace
                if (!query.isEmpty()) { query = query.substring(0, query.length() - 1); refilter(); }
            }
            case 265 -> scroll -= 1;                 // Hoch
            case 264 -> scroll += 1;                 // Runter
            case 266 -> scroll -= visible();         // Bild hoch
            case 267 -> scroll += visible();         // Bild runter
            case 268 -> scroll = 0;                  // Pos1
            case 269 -> scroll = maxScroll();        // Ende
            case 344, 256 -> { onClose(); return true; } // Right Shift / Esc
            default -> { }
        }
        clampScroll();
        return true;
    }

    @Override
    public void onClose() {
        Config.save();
        Minecraft.getInstance().setScreenAndShow(parent);
    }
}
