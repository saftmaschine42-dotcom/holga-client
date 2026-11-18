package net.nigl.client.module.modules;

import net.nigl.client.module.Category;
import net.nigl.client.module.Module;
import net.nigl.client.setting.BoolSetting;
import net.nigl.client.setting.NumberSetting;

public class Nametags extends Module {
    public final BoolSetting players = add(new BoolSetting("Players", true));
    public final BoolSetting hostile = add(new BoolSetting("Hostile", true));
    public final BoolSetting passive = add(new BoolSetting("Passive", false));
    public final BoolSetting armor = add(new BoolSetting("Armor", true));
    public final BoolSetting handItems = add(new BoolSetting("Hand Items", true));
    public final BoolSetting health = add(new BoolSetting("Health", true));
    public final BoolSetting distance = add(new BoolSetting("Distance", true));
    public final NumberSetting range = add(new NumberSetting("Range", 48, 8, 100, 4));

    public Nametags() { super("Nametags", "Name, Leben, Ruestung und Items ueber Gegnern", Category.RENDER); }
}
