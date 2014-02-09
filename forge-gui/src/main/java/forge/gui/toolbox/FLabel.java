package forge.gui.toolbox;

import forge.Command;
import forge.gui.framework.ILocalRepaint;
import forge.gui.toolbox.FSkin.SkinColor;
import forge.gui.toolbox.FSkin.SkinImage;
import forge.gui.toolbox.FSkin.SkinnedLabel;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.*;

/** 
 * Uses the Builder pattern to facilitate/encourage inline styling.
 * Credit to Effective Java 2 (Joshua Bloch).
 * Methods in builder can be chained. To declare:
 * <code>new FLabel.Builder().method1(foo).method2(bar).method3(baz)...</code>
 * <br>and then call build() to make the label (don't forget that part).
 * <br><br>
 * Adjustable features of FLabel:<br>
 * - Automatic font scaling (60% size by default, can toggle on/off)<br>
 * - Automatic icon scaling (80% size by default, can toggle on/off)<br>
 * - Scale font according to height or width<br>
 * - Hoverable<br>
 * - Selectable<br>
 * - Can execute command when clicked
 */
@SuppressWarnings("serial")
public class FLabel extends SkinnedLabel implements ILocalRepaint {
    /** 
     * Uses the Builder pattern to facilitate/encourage inline styling.
     * Credit to Effective Java 2 (Joshua Bloch).
     * Methods in builder can be chained. To declare:
     * <code>new FLabel.Builder().method1(foo).method2(bar).method3(baz)...</code>
     * <br>and then call build() to make the label (don't forget that part).
    */
    public static class Builder {
        //========== Default values for FLabel are set here.
        private double  bldIconScaleFactor  = 0.8;
        private int     bldFontStyle        = Font.PLAIN;
        private int     bldFontSize         = 14;
        private float   bldUnhoveredAlpha   = 0.7f;
        private int     bldIconAlignX       = SwingConstants.LEFT;
        private Point   bldIconInsets       = new Point(0, 0);

        private boolean bldSelectable         = false;
        private boolean bldSelected           = false;
        protected boolean bldHoverable        = false;
        protected boolean bldOpaque           = false;
        private boolean bldIconInBackground   = false;
        private boolean bldIconScaleAuto      = true;
        protected boolean bldReactOnMouseDown = false;
        private boolean bldUseSkinColors      = true;
        private boolean bldEnabled            = true;

        protected String  bldText, bldToolTip;
        private SkinImage bldIcon;
        private int bldFontAlign;
        protected Command bldCmd;

        // Build!
        /** @return {@link forge.gui.toolbox.FLabel} */
        public FLabel build() { return new FLabel(this); }

        // Begin builder methods.
        /**@param s0 &emsp; {@link java.lang.String}
         * @return {@link forge.gui.toolbox.Builder} */
        public Builder text(final String s0) { this.bldText = s0; return this; }

        /**@param s0 &emsp; {@link java.lang.String}
         * @return {@link forge.gui.toolbox.Builder} */
        public Builder tooltip(final String s0) { this.bldToolTip = s0; return this; }

        /**@param i0 &emsp; {@link forge.gui.toolbox.FSkin.SkinIcon}
         * @return {@link forge.gui.toolbox.Builder} */
        public Builder icon(final SkinImage i0) { this.bldIcon = i0; return this; }

        /**@param i0 &emsp; SwingConstants.CENTER, .LEFT, or .RIGHT
         * @return {@link forge.gui.toolbox.Builder} */
        public Builder fontAlign(final int i0) { this.bldFontAlign = i0; return this; }

        /**@param b0 &emsp; boolean
         * @return {@link forge.gui.toolbox.Builder} */
        public Builder opaque(final boolean b0) { this.bldOpaque = b0; return this; }
        public Builder opaque() { opaque(true); return this; }

        /**@param b0 &emsp; boolean
         * @return {@link forge.gui.toolbox.Builder} */
        public Builder hoverable(final boolean b0) { this.bldHoverable = b0; return this; }
        public Builder hoverable() { hoverable(true); return this; }

        /**@param b0 &emsp; boolean
         * @return {@link forge.gui.toolbox.Builder} */
        public Builder selectable(final boolean b0) { this.bldSelectable = b0; return this; }
        public Builder selectable() { selectable(true); return this; }

        /**@param b0 &emsp; boolean
         * @return {@link forge.gui.toolbox.Builder} */
        public Builder selected(final boolean b0) { this.bldSelected = b0; return this; }
        public Builder selected() { selected(true); return this; }

        /**@param b0 &emsp; boolean that controls when the label responds to mouse events
         * @return {@link forge.gui.toolbox.Builder} */
        public Builder reactOnMouseDown(final boolean b0) { this.bldReactOnMouseDown = b0; return this; }
        public Builder reactOnMouseDown() { reactOnMouseDown(true); return this; }

        /**@param b0 &emsp; boolean that controls whether the text uses skin colors
         * @return {@link forge.gui.toolbox.Builder} */
        public Builder useSkinColors(final boolean b0) { bldUseSkinColors = b0; return this; }

        /**@param c0 &emsp; {@link forge.Command} to execute if clicked
         * @return {@link forge.gui.toolbox.Builder} */
        public Builder cmdClick(final Command c0) { this.bldCmd = c0; return this; }

        /**@param i0 &emsp; int
         * @return {@link forge.gui.toolbox.Builder} */
        public Builder fontSize(final int i0) { this.bldFontSize = i0; return this; }

        /**@param i0 &emsp; Font.PLAIN, Font.BOLD, or Font.ITALIC
         * @return {@link forge.gui.toolbox.Builder} */
        public Builder fontStyle(final int i0) { this.bldFontStyle = i0; return this; }

        /**@param b0 &emsp; boolean
         * @return {@link forge.gui.toolbox.Builder} */
        public Builder enabled(final boolean b0) { this.bldEnabled = b0; return this; }

        /**@param b0 &emsp; boolean
         * @return {@link forge.gui.toolbox.Builder} */
        public Builder iconScaleAuto(final boolean b0) { this.bldIconScaleAuto = b0; return this; }

        /**@param d0 &emsp; double between 0 and 1, 0.8 by default
         * @return {@link forge.gui.toolbox.Builder} */
        public Builder iconScaleFactor(final double d0) { this.bldIconScaleFactor = d0; return this; }

        /**@param b0 &emsp; boolean, icon will be drawn independent of text
         * @return {@link forge.gui.toolbox.Builder} */
        public Builder iconInBackground(final boolean b0) { this.bldIconInBackground = b0; return this; }
        public Builder iconInBackground() { iconInBackground(true); return this; }

        /**@param f0 &emsp; 0.0f - 1.0f. alpha factor applied when label is hoverable but not currently hovered.
         * @return {@link forge.gui.toolbox.Builder} */
        public Builder unhoveredAlpha(final float f0) { this.bldUnhoveredAlpha = f0; return this; }

        /**@param i0 &emsp; Int. Only available for background icon.
         * SwingConstants.HORIZONTAL .VERTICAL or .CENTER
         * @return {@link forge.gui.toolbox.Builder} */
        public Builder iconAlignX(final int i0) { this.bldIconAlignX = i0; return this; }

        /**@param i0 &emsp; Point. Only available for background icon.
         * Additional padding to top left corner of icon, after alignX.
         * @return {@link forge.gui.toolbox.Builder} */
        public Builder iconInsets(final Point i0) { this.bldIconInsets = i0; return this; }
    }

    // sets better defaults for button labels
    public static class ButtonBuilder extends Builder {
        public ButtonBuilder() {
            bldHoverable = true;
            bldOpaque    = true;
        }
    }

    //========== Constructors
    // Call this using FLabel.Builder()...
    protected FLabel(final Builder b0) {
        super(b0.bldText);

        // Init fields from builder
        this.iconScaleFactor = b0.bldIconScaleFactor;

        this.opaque = b0.bldOpaque;
        this.iconInBackground = b0.bldIconInBackground;
        this.iconScaleAuto = b0.bldIconScaleAuto;
        this.selectable = b0.bldSelectable;
        this.selected = b0.bldSelected;
        this.iconAlignX = b0.bldIconAlignX;
        this.iconInsets = b0.bldIconInsets;

        this.setEnabled(b0.bldEnabled);
        this.setFontStyle(b0.bldFontStyle);
        this.setFontSize(b0.bldFontSize);
        this.setUnhoveredAlpha(b0.bldUnhoveredAlpha);
        this.setCommand(b0.bldCmd);
        this.setReactOnMouseDown(b0.bldReactOnMouseDown);
        this.setFontAlign(b0.bldFontAlign);
        this.setToolTipText(b0.bldToolTip);
        this.setHoverable(b0.bldHoverable);

        // Call this last; to allow the properties which affect icons to already be in place.
        this.setIcon(b0.bldIcon);

        // If the label has button-like properties, interpret keypresses like a button
        if (b0.bldSelectable || b0.bldHoverable) {
            this.setFocusable(true);

            this.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(final KeyEvent e) {
                    if (e.getKeyChar() == ' ' || e.getKeyCode() == 10 || e.getKeyCode() == KeyEvent.VK_ENTER) { _doMouseAction(); }
                }
            });

            this.addFocusListener(new FocusListener() {
                @Override public void focusLost(FocusEvent arg0)   { repaintSelf(); }
                @Override public void focusGained(FocusEvent arg0) { repaintSelf(); }
            });
        }

        if (b0.bldUseSkinColors) {
            // Non-custom display properties
            this.setForeground(clrText);
            this.setBackground(clrMain);
        }

        // Resize adapter
        this.removeComponentListener(cadResize);
        this.addComponentListener(cadResize);

        // First-time-shown adapter (required to size icons properly
        // if icon is set while the label is still 0 x 0)
        this.addAncestorListener(showFirstTime);
    }

    //========== Variable initialization
    // Final inits
    private static final SkinColor clrHover = FSkin.getColor(FSkin.Colors.CLR_HOVER);
    private static final SkinColor clrText = FSkin.getColor(FSkin.Colors.CLR_TEXT);
    private static final SkinColor clrMain = FSkin.getColor(FSkin.Colors.CLR_INACTIVE);
    private static final SkinColor d50 = clrMain.stepColor(-50);
    private static final SkinColor d30 = clrMain.stepColor(-30);
    private static final SkinColor d10 = clrMain.stepColor(-10);
    private static final SkinColor l10 = clrMain.stepColor(10);
    private static final SkinColor l20 = clrMain.stepColor(20);
    private static final SkinColor l30 = clrMain.stepColor(30);

    // Custom properties, assigned either at realization (using builder)
    // or dynamically (using methods below).
    private double iconScaleFactor;
    private int fontStyle, iconAlignX;
    private int iw, ih;
    private boolean selectable, selected, hoverable, hovered, pressed, opaque,
        iconInBackground, iconScaleAuto, reactOnMouseDown;
    private Point iconInsets;

    // Various variables used in image rendering.
    private Image img;

    private Runnable cmdClick, cmdRightClick;

    private double iar;

    private AlphaComposite alphaDim, alphaStrong;

    private final ActionListener fireResize = new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent evt) { resetIcon(); resizeTimer.stop(); }
    };

    private final Timer resizeTimer = new Timer(10, fireResize);

    // Resize adapter; on a timer to prevent resizing while "sliding" between sizes
    private final ComponentAdapter cadResize = new ComponentAdapter() {
        @Override
        public void componentResized(final ComponentEvent e) { resizeTimer.restart(); }
    };

    private final AncestorListener showFirstTime = new AncestorListener() {
        @Override
        public void ancestorAdded(final AncestorEvent e) {
            resetIcon();
        }

        @Override
        public void ancestorMoved(AncestorEvent arg0) {
        }

        @Override
        public void ancestorRemoved(AncestorEvent arg0) {
        }
    };

    private void _doMouseAction() {
        if (selectable) { setSelected(!selected); }
        if (cmdClick != null && isEnabled()) {
            cmdClick.run();
        }
    }

    private void _doRightClickAction() {
        if (cmdRightClick != null && isEnabled()) {
            cmdRightClick.run();
        }
    }

    // Mouse event handler
    private final FMouseAdapter madEvents = new FMouseAdapter() {
        @Override
        public void onMouseEnter(MouseEvent e) {
            setHovered(true);
        }

        @Override
        public void onMouseExit(MouseEvent e) {
            setHovered(false);
        }

        @Override
        public void onLeftMouseDown(MouseEvent e) {
            if (reactOnMouseDown) {
                _doMouseAction(); //for best responsiveness, do action before repainting for pressed state
            }
            setPressed(true);
        }

        @Override
        public void onLeftMouseUp(MouseEvent e) {
            setPressed(false);
        }

        @Override
        public void onLeftClick(MouseEvent e) {
            if (!reactOnMouseDown) {
                _doMouseAction();
            }
        }

        @Override
        public void onRightClick(MouseEvent e) {
            _doRightClickAction();
        }
    };

    //========== Methods
    /** @param b0 &emsp; boolean */
    // Must be public.
    @Override
    public void setEnabled(final boolean b0) {
        if (this.isEnabled() == b0) { return; }
        super.setEnabled(b0);
        if (!this.hoverable) { return; }
        if (!b0) { this.removeMouseListener(madEvents); }
        else { this.addMouseListener(madEvents); }
    }

    /** @param b0 &emsp; boolean */
    // Must be public.
    public void setHoverable(final boolean b0) {
        if (this.hoverable == b0) { return; }
        this.hoverable = b0;
        if (!this.isEnabled()) { return; }
        if (!b0) { this.removeMouseListener(madEvents); }
        else { this.addMouseListener(madEvents); }
    }

    protected void setHovered(boolean hovered0) {
        this.hovered = hovered0;
        repaintSelf();
    }

    protected void setPressed(boolean pressed0) {
        this.pressed = pressed0;
        repaintSelf();
    }

    /** @param b0 &emsp; boolean */
    // Must be public.
    public void setSelected(final boolean b0) {
        this.selected = b0;
        repaintSelf();
    }

    public boolean getSelected() {
        return this.selected;
    }

    /** Sets alpha if icon is in background.
     * @param f0 &emsp; float */
    // NOT public; must be set when label is built.
    private void setUnhoveredAlpha(final float f0) {
        this.alphaDim = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, f0);
        this.alphaStrong = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
    }

    public void setFontSize(final int i0) {
        switch(this.fontStyle) {
            case Font.BOLD: this.setFont(FSkin.getBoldFont(i0)); break;
            case Font.ITALIC: this.setFont(FSkin.getItalicFont(i0)); break;
            default: this.setFont(FSkin.getFont(i0));
        }
    }

    /** @param i0 &emsp; Font.PLAIN, .BOLD, or .ITALIC */
    // NOT public; must be set when label is built.
    public void setFontStyle(final int i0) {
        if (i0 != Font.PLAIN && i0 != Font.BOLD && i0 != Font.ITALIC) {
            throw new IllegalArgumentException("FLabel$setFontStyle "
                    + "must be passed either Font.PLAIN, Font.BOLD, or Font.ITALIC.");
        }
        this.fontStyle = i0;
    }

    /** @param i0 &emsp; SwingConstants.CENTER, .LEFT or .RIGHT */
    // NOT public; must be set when label is built.
    public void setFontAlign(final int i0) {
        if (i0 != SwingConstants.CENTER && i0 != SwingConstants.LEFT && i0 != SwingConstants.RIGHT) {
            throw new IllegalArgumentException("FLabel$setFontStyle "
                    + "must be passed either SwingConstants.CENTER, "
                    + "SwingConstants.LEFT, or SwingConstants.RIGHT");
        }
        this.setHorizontalAlignment(i0);
    }

    public int getAutoSizeWidth() {
        int width = 0;
        if (this.getText() != null && !this.getText().isEmpty()) {
            FontMetrics metrics = this.getFontMetrics(this.getFont());
            width = metrics.stringWidth(this.getText());
        }
        if (this.getIcon() != null) {
            width += this.getIcon().getIconWidth() + this.getIconTextGap();
        }
        if (opaque || selectable) {
            width += 6; //account for border/padding if opaque
        }
        return width;
    }

    /** Resizing in MigLayout "slides" between the original and destination sizes.
     * To prevent this label from recalculating on each increment, a timer
     * is run to check that the the "sliding" is finished.  To resize this label
     * explicitly, retrieve this timer and start it.  It will stop automatically.
     * 
     * @return {@link javax.swing.Timer}
     */
    public Timer getResizeTimer() {
        return this.resizeTimer;
    }

    /** @return {@link forge.Command} */
    public Runnable getCommand() {
        return this.cmdClick;
    }

	/** @return {@link forge.Command} */
    public Runnable getRightClickCommand() {
        return this.cmdRightClick;
    }

    @Override
    // Must be public.
    public void setIcon(final Icon i0) {
        // Will need image (not icon) for scaled and non-scaled.
        // Will need image if not in background, but scaled.
        if (iconInBackground || iconScaleAuto) {
            if (i0 != null) {
                img = ((ImageIcon) i0).getImage();
                iw = img.getWidth(null);
                ih = img.getHeight(null);
                iar = ((double) iw) / ((double) ih);
            }
            else {
                img = null;
                iw = 0;
                ih = 0;
                iar = 0;
            }
        }
        else { // If not in background, not scaled, can use original icon.
            super.setIcon(i0);
        }
    }

    /** @param c0 &emsp; {@link forge.Command} on click */
    public void setCommand(final Runnable c0) {
        this.cmdClick = c0;
    }

	/** @param c0 &emsp; {@link forge.Command} on right-click */
    public void setRightClickCommand(final Runnable c0) {
        this.cmdRightClick = c0;
    }

    public void setReactOnMouseDown(boolean b0) {
        this.reactOnMouseDown = b0;
    }

    @Override
    public void setOpaque(final boolean b0) {
        // Must be overridden to allow drawing order of background, icon, string
        this.opaque = b0;
        super.setOpaque(false);
    }

    /** Major performance kicker - won't repaint whole screen! */
    @Override
    public void repaintSelf() {
        final Dimension d = getSize();
        repaint(0, 0, d.width, d.height);
    }

    @Override
    public void paintComponent(final Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        int w = getWidth();
        int h = getHeight();

        boolean paintWithHover = hoverable && hovered && isEnabled();
        Composite oldComp = g2d.getComposite();
        if (hoverable) {
            g2d.setComposite(paintWithHover ? alphaStrong : alphaDim);
        }

        boolean paintPressedState = pressed && hovered && isEnabled() && (opaque || selectable);
        if (paintPressedState) {
            paintPressed(g2d, w, h);
        }
        else if (opaque) {
            if (selected) {
                paintDown(g2d, w, h);
            }
            else {
                paintUp(g2d, w, h);
            }
        }
        else if (selectable) {
            if (selected) {
                paintDown(g2d, w, h);
            }
            else {
                paintBorder(g2d, w, h);
            }
        }

        paintContent(g2d, w, h, paintPressedState);

        if (hoverable) {
            g2d.setComposite(oldComp);
        }

        if (hasFocus() && isEnabled()) {
            paintFocus(g2d, w, h);
        }
    }

    protected void paintContent(final Graphics2D g, int w, int h, final boolean paintPressedState) {
        if (paintPressedState) { //while pressed, translate graphics so icon and text appear shifted down and to the right
            g.translate(1, 1);
        }

        // Icon in background
        if (iconInBackground) {
            int sh = (int) (h * iconScaleFactor);
            int sw = (int) (sh * iar);

            int x = iconAlignX == SwingConstants.CENTER
                        ? (int) ((w - sw) / 2 + iconInsets.getX())
                        : (int) iconInsets.getX();

            int y = (int) (((h - sh) / 2) + iconInsets.getY());

            g.drawImage(img, x, y, sw + x, sh + y, 0, 0, iw, ih, null);
        }

        super.paintComponent(g);

        if (paintPressedState) { //reset translation after icon and text painted
            g.translate(-1, -1);
        }
    }

    private void paintFocus(final Graphics2D g, int w, int h) {
        FSkin.setGraphicsColor(g, clrHover);
        g.drawRect(0, 0, w - 2, h - 2);
        FSkin.setGraphicsColor(g, l30);
        g.drawRect(1, 1, w - 4, h - 4);
    }

    private void paintPressed(final Graphics2D g, int w, int h) {
        FSkin.setGraphicsGradientPaint(g, 0, h, d50, 0, 0, d10);
        g.fillRect(0, 0, w - 1, h - 1);

        FSkin.setGraphicsColor(g, d50);
        g.drawRect(0, 0, w - 2, h - 2);
        FSkin.setGraphicsColor(g, d10);
        g.drawRect(1, 1, w - 4, h - 4);
    }

    private void paintUp(final Graphics2D g, int w, int h) {
        FSkin.setGraphicsGradientPaint(g, 0, h, d10, 0, 0, l20);
        g.fillRect(0, 0, w, h);

        FSkin.setGraphicsColor(g, d50);
        g.drawRect(0, 0, w - 2, h - 2);
        FSkin.setGraphicsColor(g, l10);
        g.drawRect(1, 1, w - 4, h - 4);
    }

    private void paintBorder(final Graphics2D g, int w, int h) {
        FSkin.setGraphicsColor(g, l10);
        g.drawRect(0, 0, w - 2, h - 2);
        FSkin.setGraphicsColor(g, l30);
        g.drawRect(1, 1, w - 4, h - 4);
    }

    private void paintDown(final Graphics2D g, int w, int h) {
        FSkin.setGraphicsGradientPaint(g, 0, h, d30, 0, 0, l10);
        g.fillRect(0, 0, w - 1, h - 1);

        FSkin.setGraphicsColor(g, d30);
        g.drawRect(0, 0, w - 2, h - 2);
        FSkin.setGraphicsColor(g, l10);
        g.drawRect(1, 1, w - 4, h - 4);
    }

    private void resetIcon() {
        // Non-background icon
        if (img != null && iconScaleAuto  && !iconInBackground) {
            int h = (int) (getHeight() * iconScaleFactor);
            int w = (int) (h * iar);
            if (w == 0 || h == 0) { return; }

            super.setIcon(new ImageIcon(img.getScaledInstance(w, h, Image.SCALE_SMOOTH)));
        }
    }
}
