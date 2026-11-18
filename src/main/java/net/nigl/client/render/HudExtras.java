package net.nigl.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.nigl.client.Draw;
import net.nigl.client.Theme;
import net.nigl.client.module.Modules;
import net.nigl.client.module.Targets;
import net.nigl.client.module.modules.BowCharge;
import net.nigl.client.module.modules.Nametags;
import net.nigl.client.module.modules.VisualDurability;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class HudExtras {
    public static void render(GuiGraphicsExtractor g) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        try { bow(g, mc); } catch (Throwable ignored) { }
        try { durability(g, mc); } catch (Throwable ignored) { }
        try { nametags(g, mc); } catch (Throwable ignored) { }
    }

    // ---------- Bow Charge ----------
    private static void bow(GuiGraphicsExtractor g, Minecraft mc) {
        BowCharge bc = Modules.get(BowCharge.class);
        if (bc == null || !bc.enabled) return;
        var p = mc.player;
        if (!p.isUsingItem() || !p.getUseItem().is(Items.BOW)) return;
        float f = p.getTicksUsingItem() / 20f;
        float power = Math.min(1f, (f * f + f * 2f) / 3f);
        int col = power < 0.5f ? Theme.lerp(0xFFFF4D4D, 0xFFFFD84D, power * 2f) : Theme.lerp(0xFFFFD84D, 0xFF5CFF9A, (power - 0.5f) * 2f);
        int w = mc.getWindow().getGuiScaledWidth(), h = mc.getWindow().getGuiScaledHeight();
        int bw = 54, x = (w - bw) / 2, y = h / 2 + 12;
        Draw.glass(g, x - 3, y - 3, bw + 6, 12, 6);
        Draw.roundRect(g, x, y, bw, 6, 3, 0x40FFFFFF);
        int fw = Math.max(0, (int) (bw * power));
        if (fw > 0) Draw.roundRect(g, x, y, fw, 6, 3, col);
        String t = power >= 1f ? "FULL" : (int) (power * 100) + "%";
        Theme.text(g, t, (w - Theme.width(t)) / 2, y + 10, power >= 1f ? col : Theme.TEXT);
    }

    // ---------- Visual Durability ----------
    private static void durability(GuiGraphicsExtractor g, Minecraft mc) {
        VisualDurability vd = Modules.get(VisualDurability.class);
        if (vd == null || !vd.enabled) return;
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        int w = mc.getWindow().getGuiScaledWidth(), h = mc.getWindow().getGuiScaledHeight();
        int x = w - 76, y = h / 2 - 38;
        boolean blink = (System.currentTimeMillis() / 350) % 2 == 0;
        for (EquipmentSlot s : slots) {
            ItemStack st = mc.player.getItemBySlot(s);
            if (st.isEmpty() || !st.isDamageableItem()) continue;
            float pct = 1f - st.getDamageValue() / (float) Math.max(1, st.getMaxDamage());
            int col = pct < 0.5f ? Theme.lerp(0xFFFF4D4D, 0xFFFFD84D, pct * 2f) : Theme.lerp(0xFFFFD84D, 0xFF5CFF9A, (pct - 0.5f) * 2f);
            Draw.glass(g, x, y, 70, 18, 9);
            if (pct < 0.15f && blink) Draw.roundRect(g, x - 2, y - 2, 74, 22, 11, 0x40FF4D4D);
            boolean icon = ItemIcons.draw(g, st, x + 2, y + 1);
            int bx = x + (icon ? 21 : 8);
            Draw.roundRect(g, bx, y + 7, 28, 4, 2, 0x40FFFFFF);
            Draw.roundRect(g, bx, y + 7, Math.max(2, (int) (28 * pct)), 4, 2, col);
            String t = (int) (pct * 100) + "%";
            Theme.text(g, t, x + 66 - Theme.width(t), y + 5, pct < 0.15f ? 0xFFFF6B6B : Theme.TEXT);
            y += 21;
        }
    }

    // ---------- Nametags ----------
    private record Tag(LivingEntity e, double sx, double sy, double dist) { }

    private static int tierColor(ItemStack s) {
        String n = BuiltInRegistries.ITEM.getKey(s.getItem()).getPath();
        if (n.contains("netherite")) return 0xFFB38AFF;
        if (n.contains("diamond")) return 0xFF4DE1FF;
        if (n.contains("golden") || n.contains("gold")) return 0xFFFFD54D;
        if (n.contains("iron")) return 0xFFE0E0E0;
        if (n.contains("chain")) return 0xFF9AA0B0;
        if (n.contains("leather")) return 0xFFB07A4D;
        if (n.contains("turtle")) return 0xFF5CFF9A;
        return 0xFFFFFFFF;
    }

    private static void nametags(GuiGraphicsExtractor g, Minecraft mc) {
        Nametags nt = Modules.get(Nametags.class);
        if (nt == null || !nt.enabled) return;
        Entity ce = mc.getCameraEntity();
        if (ce == null) ce = mc.player;
        float pt = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        double cx = Mth.lerp(pt, ce.xo, ce.getX());
        double cy = Mth.lerp(pt, ce.yo, ce.getY()) + ce.getEyeHeight();
        double cz = Mth.lerp(pt, ce.zo, ce.getZ());
        double yaw = Math.toRadians(ce.getViewYRot(pt)), pitch = Math.toRadians(ce.getViewXRot(pt));

        double fx = -Math.sin(yaw) * Math.cos(pitch), fy = -Math.sin(pitch), fz = Math.cos(yaw) * Math.cos(pitch);
        double rx = -Math.cos(yaw), rz = -Math.sin(yaw);
        double ux = -rz * fy, uy = rz * fx - rx * fz, uz = rx * fy;

        int W = mc.getWindow().getGuiScaledWidth(), H = mc.getWindow().getGuiScaledHeight();
        double fpx = (H / 2.0) / Math.tan(Math.toRadians(mc.options.fov().get()) / 2.0);

        List<Tag> tags = new ArrayList<>();
        for (Entity e : mc.level.getEntities(ce, ce.getBoundingBox().inflate(nt.range.value))) {
            if (!(e instanceof LivingEntity le) || e == ce || e.isRemoved() || !le.isAlive()) continue;
            if (!Targets.match(le, nt.players.value, nt.hostile.value, nt.passive.value)) continue;
            double x = Mth.lerp(pt, e.xo, e.getX()), y = Mth.lerp(pt, e.yo, e.getY()) + e.getBbHeight() + 0.3, z = Mth.lerp(pt, e.zo, e.getZ());
            double dx = x - cx, dy = y - cy, dz = z - cz;
            double zc = dx * fx + dy * fy + dz * fz;
            if (zc < 0.2) continue;
            double xc = dx * rx + dz * rz, yc = dx * ux + dy * uy + dz * uz;
            double sx = W / 2.0 + xc / zc * fpx, sy = H / 2.0 - yc / zc * fpx;
            if (sx < -100 || sx > W + 100 || sy < -60 || sy > H + 60) continue;
            tags.add(new Tag(le, sx, sy, Math.sqrt(dx * dx + dy * dy + dz * dz)));
        }
        tags.sort(Comparator.comparingDouble(Tag::dist));

        int n = 0;
        for (Tag t : tags) {
            if (n++ >= 20) break;
            LivingEntity le = t.e();

            StringBuilder line = new StringBuilder(le.getName().getString());
            String hp = String.format(Locale.ROOT, "%.1f", le.getHealth() + le.getAbsorptionAmount());
            String dist = String.format(Locale.ROOT, "%.0fm", t.dist());
            int tw = Theme.width(line.toString());
            if (nt.health.value) tw += 6 + Theme.width(hp);
            if (nt.distance.value) tw += 6 + Theme.width(dist);
            int bw = tw + 14, bh = 14;
            int x = (int) t.sx() - bw / 2, y = (int) t.sy() - bh;

            // Items (Hand + Ruestung)
            List<ItemStack> stacks = new ArrayList<>();
            if (nt.handItems.value) stacks.add(le.getItemBySlot(EquipmentSlot.MAINHAND));
            if (nt.armor.value) {
                stacks.add(le.getItemBySlot(EquipmentSlot.HEAD));
                stacks.add(le.getItemBySlot(EquipmentSlot.CHEST));
                stacks.add(le.getItemBySlot(EquipmentSlot.LEGS));
                stacks.add(le.getItemBySlot(EquipmentSlot.FEET));
            }
            if (nt.handItems.value) stacks.add(le.getItemBySlot(EquipmentSlot.OFFHAND));
            stacks.removeIf(ItemStack::isEmpty);
            if (!stacks.isEmpty()) {
                int rw = stacks.size() * 18 + 4;
                int rx0 = (int) t.sx() - rw / 2, ry0 = y - 24;
                Draw.glass(g, rx0, ry0, rw, 22, 8);
                int ix = rx0 + 4;
                for (ItemStack st : stacks) {
                    boolean drawn = ItemIcons.draw(g, st, ix, ry0 + 3);
                    if (!drawn) {
                        int col = tierColor(st);
                        Draw.roundRect(g, ix, ry0 + 3, 14, 12, 4, Theme.withAlpha(col, 0xB0));
                        String ab = BuiltInRegistries.ITEM.getKey(st.getItem()).getPath();
                        ab = ab.substring(0, 1).toUpperCase(Locale.ROOT);
                        Theme.text(g, ab, ix + 4, ry0 + 5, Theme.onAccent(col), false);
                    }
                    if (st.isDamageableItem()) {
                        float pct = 1f - st.getDamageValue() / (float) Math.max(1, st.getMaxDamage());
                        int col = pct < 0.5f ? Theme.lerp(0xFFFF4D4D, 0xFFFFD84D, pct * 2f) : Theme.lerp(0xFFFFD84D, 0xFF5CFF9A, (pct - 0.5f) * 2f);
                        g.fill(ix, ry0 + 18, ix + Math.max(1, (int) (14 * pct)), ry0 + 20, col);
                    }
                    ix += 18;
                }
            }

            Draw.glass(g, x, y, bw, bh, 7);
            int tx = x + 7;
            Theme.text(g, line.toString(), tx, y + 3, Theme.TEXT);
            tx += Theme.width(line.toString()) + 6;
            if (nt.health.value) {
                float ratio = Math.min(1f, le.getHealth() / Math.max(1f, le.getMaxHealth()));
                Theme.text(g, hp, tx, y + 3, Theme.lerp(0xFFFF5C5C, 0xFF5CFF9A, ratio));
                tx += Theme.width(hp) + 6;
            }
            if (nt.distance.value) Theme.text(g, dist, tx, y + 3, Theme.GREY);
        }
    }

    private HudExtras() { }
}
