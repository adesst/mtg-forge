package forge.gui.home;

/** 
 * Submenus each belong to a menu group, which
 * is used for several functions, such as expanding
 * and collapsing in the menu.
 */
public enum EMenuGroup { /** */
    SANCTIONED ("Sanctioned Game Formats"), /** */
    QUEST ("Quest Mode"), /** */
    SETTINGS ("Game Settings"), /** */
    UTILITIES ("Tools");

    private final String strTitle;

    /** @param {@link java.lang.String} */
    private EMenuGroup(final String s0) { strTitle = s0; }

    /** @return {@link java.lang.String} */
    public String getTitle() { return this.strTitle; }
}
