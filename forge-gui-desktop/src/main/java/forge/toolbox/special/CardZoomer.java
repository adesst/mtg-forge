/*
 * Forge: Play Magic: the Gathering.
 * Copyright (C) 2013  Forge Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package forge.toolbox.special;

import forge.Singletons;
import forge.assets.FSkinProp;
import forge.card.CardCharacteristicName;
import forge.card.CardDetailUtil;
import forge.game.card.Card;
import forge.gui.SOverlayUtils;
import forge.toolbox.FOverlay;
import forge.toolbox.FSkin;
import forge.toolbox.FSkin.SkinnedLabel;
import forge.toolbox.imaging.FImagePanel;
import forge.toolbox.imaging.FImageUtil;
import forge.toolbox.imaging.FImagePanel.AutoSizeImageMode;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

import java.awt.event.*;

/** 
 * Displays card image at its original size and correct orientation.
 * <p>
 * Supports split, flip and double-sided cards as well as cards that
 * can be played face-down (eg. morph).
 *
 * @version $Id: CardZoomer.java 24769 2014-02-09 13:56:04Z Hellfish $
 * 
 */
public enum CardZoomer {
    SINGLETON_INSTANCE;    

    // Gui controls
    private final JPanel overlay = FOverlay.SINGLETON_INSTANCE.getPanel();
    private JPanel pnlMain;
    private FImagePanel imagePanel;
    private SkinnedLabel lblFlipcard = new SkinnedLabel();    
        
    // Details about the current card being displayed.
    private Card thisCard;
    private CardCharacteristicName cardState = CardCharacteristicName.Original;
    private boolean isImageFlipped = false;
    private boolean isFaceDownCard = false;
     
    // The zoomer is in button mode when it is activated by holding down the
    // middle mouse button or left and right mouse buttons simultaneously.
    private boolean isButtonMode = false;    
    private boolean isOpen = false;
    private long lastClosedTime;
                           
    // Used to ignore mouse wheel rotation for a short period of time.
    private Timer mouseWheelCoolDownTimer;
    private boolean isMouseWheelEnabled = false;    
    
    // ctr
    private CardZoomer() {
        lblFlipcard.setIcon(FSkin.getIcon(FSkinProp.ICO_FLIPCARD));
        setMouseButtonListener();
        setMouseWheelListener();
        setKeyListeners();
    }
    
    /**
     * Creates listener for keys that are recognised by zoomer.
     * <p><ul>
     * <li>ESC will close zoomer in mouse-wheel mode only.
     * <li>CTRL will flip or transform card in either mode if applicable.
     */
    private void setKeyListeners() {
        overlay.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!isButtonMode) {
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        closeZoomer();
                    }
                }
                if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                    toggleCardImage();
                }
            }        
        });
    }

    /**
     * Creates listener for mouse button events.
     * <p>
     *  NOTE: Needed even if ButtonMode to prevent Zoom getting stuck open on certain systems.
     */
    private void setMouseButtonListener() {
        overlay.addMouseListener(new MouseAdapter() {            
            @Override
            public void mouseReleased(MouseEvent e) {
                closeZoomer();
            }
        });
    }
    
    /**
     * Creates listener for mouse wheel events.
     * <p>
     * If the zoomer is opened using the mouse wheel then additional
     * actions can be performed dependent on the card type -
     * <p><ul>
     * <li>If mouse wheel is rotated back then close zoomer.
     * <li>If mouse wheel is rotated forward and...<ul>
     *   <li>if image is a flip card then rotate 180 degrees.
     *   <li>if image is a double-sided card then show other side. 
     */
    private void setMouseWheelListener() {
        overlay.addMouseWheelListener(new MouseWheelListener() {                        
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (!isButtonMode) {
                    if (isMouseWheelEnabled) {
                        isMouseWheelEnabled = false;
                        if (e.getWheelRotation() > 0) {
                            closeZoomer();
                        } else {
                            toggleCardImage();
                            startMouseWheelCoolDownTimer(250);                        
                        }                                                        
                    }                                    
                }
            }                                    
        });
    }
    
    /**
     * Opens zoomer in mouse wheel mode and displays the image associated with
     * the given card based on its current {@code CardCharacteristicName} state.
     * <p>
     * This method should be called if the zoomer is activated by rotating the mouse wheel.
     */
    public void doMouseWheelZoom(Card newCard) {
        doMouseWheelZoom(newCard, newCard.getCurState());
    }
    
    /**
     * Opens zoomer in mouse wheel mode and displays the image associated with
     * the given card based on the specified {@code CardCharacteristicName} state.
     * <p>
     * This method should be called if the zoomer is activated by rotating the mouse wheel. 
     */
    public void doMouseWheelZoom(Card newCard, CardCharacteristicName state) {
        isButtonMode = false;
        isFaceDownCard = (state == CardCharacteristicName.FaceDown);
        cardState = state;
        displayCard(newCard);
        startMouseWheelCoolDownTimer(200);          
    }

    /**
     * Opens zoomer in mouse button mode and displays the image associated with
     * the given card based on its current {@code CardCharacteristicName} state.
     * <p>
     * This method should be called if the zoomer is activated by holding down
     * the middle mouse button or left and right mouse buttons simultaneously.
     */
    public void doMouseButtonZoom(Card newCard) {
        doMouseButtonZoom(newCard, newCard.getCurState());
    }
    
    /**
     * Opens zoomer in mouse button mode and displays the image associated with
     * the given card based on the specified {@code CardCharacteristicName} state.
     * <p>
     * This method should be called if the zoomer is activated by holding down
     * the middle mouse button or left and right mouse buttons simultaneously.
     */    
    public void doMouseButtonZoom(Card newCard, CardCharacteristicName state) {        
        
        // don't display zoom if already zoomed or just closed zoom 
        // (handles mouse wheeling while middle clicking)
        if (isOpen || System.currentTimeMillis() - lastClosedTime < 250) {
            return;
        }        
        
        isButtonMode = true;        
        isFaceDownCard = (state == CardCharacteristicName.FaceDown);
        cardState = state;                
        displayCard(newCard);        
    }
    
    public boolean isZoomerOpen() {
        return isOpen;
    }
    
    private void displayCard(Card card) {
        isMouseWheelEnabled = false;
        isImageFlipped = false;       
        thisCard = card;
        setLayout();
        setImage();         
        SOverlayUtils.showOverlay();
        isOpen = true;
    }
    
    /**
     * Displays a graphical indicator that shows whether the current card can be flipped or transformed.
     */
    private void setFlipIndicator() {
        boolean isFaceDownFlippable = (isFaceDownCard && Singletons.getControl().mayShowCard(thisCard)); 
        if (thisCard.isFlipCard() || thisCard.isDoubleFaced() || isFaceDownFlippable ) {
            imagePanel.setLayout(new MigLayout("insets 0, w 100%!, h 100%!"));        
            imagePanel.add(lblFlipcard, "pos (100% - 100px) 0");
        }                    
    }
    
    /**
     * Needs to be called whenever the source image changes.
     */
    private void setImage() {
        imagePanel = new FImagePanel();
        imagePanel.setImage(FImageUtil.getImage(thisCard, cardState), getInitialRotation(), AutoSizeImageMode.SOURCE);
        pnlMain.removeAll();
        pnlMain.add(imagePanel, "w 80%!, h 80%!");
        pnlMain.validate();
        setFlipIndicator();
    }
        
    private int getInitialRotation() {
        return (thisCard.isSplitCard() || thisCard.isPlane() || thisCard.isPhenomenon() ? 90 : 0);
    }   
    
    private void setLayout() {
        overlay.removeAll();
        pnlMain = new JPanel();
        pnlMain.setOpaque(false);
        overlay.setLayout(new MigLayout("insets 0, w 100%!, h 100%!"));
        pnlMain.setLayout(new MigLayout("insets 0, wrap, align center"));
        overlay.add(pnlMain, "w 100%!, h 100%!");
    }

    public void closeZoomer() {
        if (!isOpen) { return; }
        stopMouseWheelCoolDownTimer();        
        isOpen = false;
        SOverlayUtils.hideOverlay();
        lastClosedTime = System.currentTimeMillis();
    }
    
    /**
     * If the zoomer is ativated using the mouse wheel then ignore
     * wheel for a short period of time after opening. This will 
     * prevent flip and double side cards from immediately flipping.
     */
    private void startMouseWheelCoolDownTimer(int millisecsDelay) {        
        isMouseWheelEnabled = false;
        createMouseWheelCoolDownTimer(millisecsDelay);
        mouseWheelCoolDownTimer.setInitialDelay(millisecsDelay);
        mouseWheelCoolDownTimer.restart();           
    }
    
    /**
     * Used to ignore mouse wheel rotation for {@code millisecsDelay} milliseconds.
     */
    private void createMouseWheelCoolDownTimer(int millisecsDelay) {
        if (mouseWheelCoolDownTimer == null) {           
            mouseWheelCoolDownTimer = new Timer(millisecsDelay, new ActionListener() {                
                @Override
                public void actionPerformed(ActionEvent e) {
                    isMouseWheelEnabled = true;                
                }
            });
        }
    }
    
    private void stopMouseWheelCoolDownTimer() {
        if (mouseWheelCoolDownTimer != null && mouseWheelCoolDownTimer.isRunning()) {
            mouseWheelCoolDownTimer.stop();
        }        
    }
        
    /**
     * Toggles between primary and alternate image associated with card if applicable.
     */
    private void toggleCardImage() {
        if (thisCard.isFlipCard()) {
            toggleFlipCard();
        } else if (thisCard.isDoubleFaced()) {
            toggleDoubleFacedCard();
        } else if (isFaceDownCard) {                           
            toggleFaceDownCard();
        }
    }
            
    /**
     * Flips image by rotating 180 degrees each time.
     * <p>
     * No need to get the alternate card image from cache.
     * Can simply rotate current card image in situ to get same effect.
     */
    private void toggleFlipCard() {                
        isImageFlipped = !isImageFlipped;               
        imagePanel.setRotation(isImageFlipped ? 180 : 0);        
    }
    
    /**
     * Toggles between the front and back image of a card that can be
     * played face-down (eg. morph).
     * <p>
     * Uses constraint that prevents a player from identifying opponent's face-down cards.
     */
    private void toggleFaceDownCard() {
        cardState = CardDetailUtil.getAlternateState(thisCard, cardState);        
        setImage();
    }
            
    /**
     * Toggles between the front and back image of a double-sided card.
     */
    private void toggleDoubleFacedCard() {
        cardState = CardDetailUtil.getAlternateState(thisCard, cardState);
        setImage();
    }
}
