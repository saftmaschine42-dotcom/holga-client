package net.nigl.client.setting;

/** Tastenbelegung eines Moduls (GLFW-Keycode, -1 = nicht belegt). */
public class BindSetting extends Setting {
    public int code = -1;
    public BindSetting() { super("Bind"); }
    @Override public String save() { return Integer.toString(code); }
    @Override public void load(String s) {
        try { code = Integer.parseInt(s.trim()); } catch (NumberFormatException e) { code = -1; }
    }
}
