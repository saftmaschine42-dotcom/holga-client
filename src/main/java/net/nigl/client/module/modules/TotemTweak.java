package net.nigl.client.module.modules;

import net.nigl.client.module.Category;
import net.nigl.client.module.Module;

/** Die Totem-Animation wird per TotemMixin unterdrueckt, solange das Modul an ist. */
public class TotemTweak extends Module {
    public TotemTweak() { super("TotemTweak", "Blendet die grosse Totem-Pop-Animation aus", Category.RESOURCES); }
}
