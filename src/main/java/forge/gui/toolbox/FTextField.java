package forge.gui.toolbox;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/** 
 * A custom instance of JTextArea using Forge skin properties.
 *
 */
@SuppressWarnings("serial")
public class FTextField extends JTextField {
    /** 
     * Uses the Builder pattern to facilitate/encourage inline styling.
     * Credit to Effective Java 2 (Joshua Bloch).
     * Methods in builder can be chained. To declare:
     * <code>new FTextField.Builder().method1(foo).method2(bar).method3(baz)...</code>
     * <br>and then call build() to make the widget (don't forget that part).
    */
    public static class Builder {
        //========== Default values for FTextField are set here.
        private int     maxLength = 0; // <=0 indicates no maximum
        private boolean readonly  = false;
        private String  text;
        private String  toolTip;
        private String  ghostText;
        private boolean showGhostTextWithFocus;

        public FTextField build() { return new FTextField(this); }

        public Builder maxLength(int i0)    { maxLength = i0; return this; }
        public Builder readonly(boolean b0) { readonly = b0; return this; }
        public Builder readonly()           { return readonly(true); }

        public Builder text(String s0)                    { text = s0; return this; }
        public Builder tooltip(String s0)                 { toolTip = s0; return this; }
        public Builder ghostText(String s0)               { ghostText = s0; return this; }
        public Builder showGhostTextWithFocus(boolean b0) { showGhostTextWithFocus = b0; return this; }
        public Builder showGhostTextWithFocus()           { return showGhostTextWithFocus(true); }
    }
    
    public static final int HEIGHT = 25; //TODO: calculate this somehow instead of hard-coding it
    
    private String ghostText;
    private boolean showGhostTextWithFocus;

    private FTextField(Builder builder) {
        this.setForeground(FSkin.getColor(FSkin.Colors.CLR_TEXT));
        this.setBackground(FSkin.getColor(FSkin.Colors.CLR_THEME2));
        this.setCaretColor(FSkin.getColor(FSkin.Colors.CLR_TEXT));
        this.setMargin(new Insets(3, 3, 2, 3));
        this.setOpaque(true);
        
        if (0 < builder.maxLength)
        {
            this.setDocument(new _LengthLimitedDocument(builder.maxLength));
        }
        
        this.setEditable(!builder.readonly);
        this.setText(builder.text);
        this.setToolTipText(builder.toolTip);
        this.setFocusable(true);

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(final FocusEvent e) {
                FTextField field = (FTextField)e.getSource();
                if (field.ghostText != null && field.isEmpty() && !field.showGhostTextWithFocus)
                {
                    field.repaint();
                }
            }

            @Override
            public void focusLost(final FocusEvent e) {
                FTextField field = (FTextField)e.getSource();
                if (field.ghostText != null && field.isEmpty() && !field.showGhostTextWithFocus)
                {
                    field.repaint();
                }
            }
        });
        this.showGhostTextWithFocus = builder.showGhostTextWithFocus;
        this.ghostText = builder.ghostText;
        if (this.ghostText == "") { this.ghostText = null; } //don't allow empty string to make other logic easier
    }
    
    public boolean isEmpty()
    {
        String text = this.getText();
        return (text == null || text.isEmpty());
    }
    
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (this.ghostText != null && this.isEmpty() && (this.showGhostTextWithFocus || !this.hasFocus()))
        {
            //TODO: Make ghost text look more like regular text
            final Insets margin = this.getMargin();
            final Graphics2D g2d = (Graphics2D)g.create();
            g2d.setFont(this.getFont());
            g2d.setColor(FSkin.stepColor(this.getForeground(), 20));
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.drawString(this.ghostText, margin.left + 2, margin.top + 15); //account for borders (TODO: why +15?)
            g2d.dispose();
        }
    }
    
    public String getGhostText()
    {
        return this.ghostText;
    }
    
    public void setGhostText(String ghostText0)
    {
        if (ghostText0 == "") { ghostText0 = null; } //don't allow empty string to make other logic easier
        if (this.ghostText == ghostText0) { return; }
        this.ghostText = ghostText0;
        if (this.isEmpty())
        {
            this.repaint();
        }
    }
    
    public boolean getShowGhostTextWithFocus()
    {
        return this.showGhostTextWithFocus;
    }
    
    public void setShowGhostTextWithFocus(boolean showGhostTextWithFocus0)
    {
        if (this.showGhostTextWithFocus == showGhostTextWithFocus0) { return; }
        this.showGhostTextWithFocus = showGhostTextWithFocus0;
        if (this.isEmpty() && this.hasFocus())
        {
            this.repaint();
        }
    }

    private static class _LengthLimitedDocument extends PlainDocument {
        private final int _limit;
        
        _LengthLimitedDocument(int limit) { _limit = limit; }

        // called each time a character is typed or a string is pasted
        @Override
        public void insertString(int offset, String s, AttributeSet attributeSet)
                throws BadLocationException {
            if (_limit <= this.getLength()) {
                return;
            }
            int newLen = this.getLength() + s.length();
            if (_limit < newLen) {
                s = s.substring(0, _limit - this.getLength());
            }
            super.insertString(offset, s, attributeSet);
        }
    }
}
