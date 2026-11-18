package net.nigl.client.render;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;

/** Zeichnet Item-Icons ueber Reflection (Methodenname je nach Version "item"/"renderItem"). */
public final class ItemIcons {
    private static Method m;
    private static boolean resolved;

    private static void resolve(GuiGraphicsExtractor g) {
        resolved = true;
        for (String n : new String[]{"item", "renderItem", "fakeItem", "renderFakeItem"}) {
            for (Method c : g.getClass().getMethods()) {
                Class<?>[] p = c.getParameterTypes();
                if (c.getName().equals(n) && p.length == 3 && p[0] == ItemStack.class && p[1] == int.class && p[2] == int.class) {
                    m = c;
                    return;
                }
            }
        }
    }

    public static boolean draw(GuiGraphicsExtractor g, ItemStack s, int x, int y) {
        if (!resolved) resolve(g);
        if (m == null) return false;
        try {
            m.invoke(g, s, x, y);
            return true;
        } catch (Throwable t) {
            m = null;
            return false;
        }
    }

    private ItemIcons() { }
}
