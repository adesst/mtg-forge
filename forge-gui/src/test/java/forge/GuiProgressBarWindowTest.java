package forge;

import forge.gui.GuiProgressBarWindow;
import forge.gui.toolbox.FSkin;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA. User: dhudson
 */
@Test(groups = { "UnitTest" })
public class GuiProgressBarWindowTest {

    /**
     * Gui progress bar window test1.
     */
    @Test(groups = { "UnitTest", "fast" })
    public void guiProgressBarWindowTest1() {
        try {
            FSkin.Colors.updateAll();
            final GuiProgressBarWindow dialog = new GuiProgressBarWindow();
            dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
            Assert.assertNotNull(dialog);
            dialog.dispose();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
