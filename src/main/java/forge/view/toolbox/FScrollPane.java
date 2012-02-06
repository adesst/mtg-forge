package forge.view.toolbox;

import java.awt.Component;

import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;

/** 
 * A very basic extension of JScrollPane to centralize common styling changes.
 *
 */
@SuppressWarnings("serial")
public class FScrollPane extends JScrollPane {
    /**
     * A very basic extension of JScrollPane to centralize common styling changes.
     * 
     * @param c0 {@link java.awt.Component}
     */
    public FScrollPane(Component c0) {
        super(c0);
        getViewport().setOpaque(false);
        setBorder(new LineBorder(FSkin.getColor(FSkin.Colors.CLR_BORDERS), 1));
        setOpaque(false);
    }
}
