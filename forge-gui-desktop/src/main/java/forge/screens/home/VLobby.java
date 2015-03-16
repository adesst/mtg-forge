package forge.screens.home;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import forge.AIOption;
import forge.GuiBase;
import forge.UiCommand;
import forge.assets.FSkinProp;
import forge.deck.CardPool;
import forge.deck.Deck;
import forge.deck.DeckProxy;
import forge.deck.DeckSection;
import forge.deck.DeckType;
import forge.deck.DeckgenUtil;
import forge.deckchooser.DecksComboBoxEvent;
import forge.deckchooser.FDeckChooser;
import forge.deckchooser.IDecksComboBoxListener;
import forge.game.GameType;
import forge.game.card.CardView;
import forge.gui.CardDetailPanel;
import forge.interfaces.IPlayerChangeListener;
import forge.interfaces.IUpdateable;
import forge.item.PaperCard;
import forge.match.GameLobby;
import forge.match.LobbySlot;
import forge.model.FModel;
import forge.net.game.LobbySlotType;
import forge.net.game.UpdateLobbyPlayerEvent;
import forge.properties.ForgePreferences;
import forge.properties.ForgePreferences.FPref;
import forge.toolbox.FCheckBox;
import forge.toolbox.FLabel;
import forge.toolbox.FList;
import forge.toolbox.FOptionPane;
import forge.toolbox.FPanel;
import forge.toolbox.FScrollPane;
import forge.toolbox.FScrollPanel;
import forge.toolbox.FSkin;
import forge.toolbox.FSkin.SkinImage;
import forge.toolbox.FTextField;
import forge.util.Aggregates;
import forge.util.Lang;
import forge.util.NameGenerator;
import forge.util.storage.IStorage;

/**
 * Lobby view. View of a number of players at the deck selection stage.
 *
 * <br><br><i>(V at beginning of class name denotes a view class.)</i>
 */
public class VLobby implements IUpdateable {

    static final int MAX_PLAYERS = 8;
    private static final ForgePreferences prefs = FModel.getPreferences();

    // General variables
    private final GameLobby lobby;
    private IPlayerChangeListener playerChangeListener = null;
    private final LblHeader lblTitle = new LblHeader("Sanctioned Format: Constructed");
    private int activePlayersNum = 0;
    private int playerWithFocus = 0; // index of the player that currently has focus
    private PlayerPanel playerPanelWithFocus;

    private final StartButton btnStart  = new StartButton();
    private final JPanel pnlStart = new JPanel(new MigLayout("insets 0, gap 0, wrap 2"));
    private final JPanel constructedFrame = new JPanel(new MigLayout("insets 0, gap 0, wrap 2")); // Main content frame

    // Variants frame and variables
    private final FPanel variantsPanel = new FPanel(new MigLayout("insets 10, gapx 10"));
    private final VariantCheckBox vntVanguard = new VariantCheckBox(GameType.Vanguard);
    private final VariantCheckBox vntMomirBasic = new VariantCheckBox(GameType.MomirBasic);
    private final VariantCheckBox vntCommander = new VariantCheckBox(GameType.Commander);
    private final VariantCheckBox vntTinyLeaders = new VariantCheckBox(GameType.TinyLeaders);
    private final VariantCheckBox vntPlanechase = new VariantCheckBox(GameType.Planechase);
    private final VariantCheckBox vntArchenemy = new VariantCheckBox(GameType.Archenemy);
    private final VariantCheckBox vntArchenemyRumble = new VariantCheckBox(GameType.ArchenemyRumble);
    private final ImmutableList<VariantCheckBox> vntBoxes =
            ImmutableList.of(vntVanguard, vntMomirBasic, vntCommander, vntTinyLeaders, vntPlanechase, vntArchenemy, vntArchenemyRumble);

    // Player frame elements
    private final JPanel playersFrame = new JPanel(new MigLayout("insets 0, gap 0 5, wrap, hidemode 3"));
    private final FScrollPanel playersScroll = new FScrollPanel(new MigLayout("insets 0, gap 0, wrap, hidemode 3"), true);
    private final List<PlayerPanel> playerPanels = new ArrayList<PlayerPanel>(MAX_PLAYERS);

    private final FLabel addPlayerBtn = new FLabel.ButtonBuilder().fontSize(14).text("Add a Player").build();

    // Deck frame elements
    private final JPanel decksFrame = new JPanel(new MigLayout("insets 0, gap 0, wrap, hidemode 3"));
    private final List<FDeckChooser> deckChoosers = new ArrayList<FDeckChooser>(8);
    private final FCheckBox cbSingletons = new FCheckBox("Singleton Mode");
    private final FCheckBox cbArtifacts = new FCheckBox("Remove Artifacts");
    private final Deck[] decks = new Deck[MAX_PLAYERS];

    // Variants
    private final List<FList<Object>> schemeDeckLists = new ArrayList<FList<Object>>();
    private final List<FPanel> schemeDeckPanels = new ArrayList<FPanel>(MAX_PLAYERS);
    private int lastArchenemy = 0;

    private final List<FList<Object>> commanderDeckLists = new ArrayList<FList<Object>>();
    private final List<FPanel> commanderDeckPanels = new ArrayList<FPanel>(MAX_PLAYERS);

    private final List<FList<Object>> planarDeckLists = new ArrayList<FList<Object>>();
    private final List<FPanel> planarDeckPanels = new ArrayList<FPanel>(MAX_PLAYERS);

    private final List<FList<Object>> vgdAvatarLists = new ArrayList<FList<Object>>();
    private final List<FPanel> vgdPanels = new ArrayList<FPanel>(MAX_PLAYERS);
    private final List<CardDetailPanel> vgdAvatarDetails = new ArrayList<CardDetailPanel>();
    private final List<PaperCard> vgdAllAvatars = new ArrayList<PaperCard>();
    private final List<PaperCard> vgdAllAiAvatars = new ArrayList<PaperCard>();
    private final List<PaperCard> nonRandomHumanAvatars = new ArrayList<PaperCard>();
    private final List<PaperCard> nonRandomAiAvatars = new ArrayList<PaperCard>();
    private final Vector<Object> humanListData = new Vector<Object>();
    private final Vector<Object> aiListData = new Vector<Object>();

    // CTR
    public VLobby(final GameLobby lobby) {
        this.lobby = lobby;

        lblTitle.setBackground(FSkin.getColor(FSkin.Colors.CLR_THEME2));

        ////////////////////////////////////////////////////////
        //////////////////// Variants Panel ////////////////////

        variantsPanel.setOpaque(false);
        variantsPanel.add(newLabel("Variants:"));
        for (final VariantCheckBox vcb : vntBoxes) {
            variantsPanel.add(vcb);
        }

        constructedFrame.add(new FScrollPane(variantsPanel, false, true,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED),
                "w 100%, h 45px!, gapbottom 10px, spanx 2, wrap");

        playersFrame.setOpaque(false);
        playersFrame.add(playersScroll, "w 100%, h 100%-35px");

        if (lobby.hasControl()) {
            addPlayerBtn.setFocusable(true);
            addPlayerBtn.setCommand(new Runnable() {
                @Override public final void run() {
                    lobby.addSlot();
                }
            });
            playersFrame.add(addPlayerBtn, "height 30px!, growx, pushx");
        }

        constructedFrame.add(playersFrame, "gapright 10px, w 50%-5px, growy, pushy");

        ////////////////////////////////////////////////////////
        ////////////////////// Deck Panel //////////////////////

        for (int i = 0; i < MAX_PLAYERS; i++) {
            buildDeckPanel(i);
        }
        constructedFrame.add(decksFrame, "w 50%-5px, growy, pushy");
        constructedFrame.setOpaque(false);
        decksFrame.setOpaque(false);

        // Start Button
        if (lobby.hasControl()) {
            pnlStart.setOpaque(false);
            pnlStart.add(btnStart, "align center");

            // Start button event handling
            btnStart.addActionListener(new ActionListener() {
                @Override public final void actionPerformed(final ActionEvent arg0) {
                    lobby.startGame();
                }
            });
        }
    }

    public void populate() {
        for (final FDeckChooser fdc : deckChoosers) {
            fdc.populate();
            fdc.getDecksComboBox().addListener(new IDecksComboBoxListener() {
                @Override
                public void deckTypeSelected(DecksComboBoxEvent ev) {
                    playerPanelWithFocus.focusOnAvatar();
                }
            });
        }
        populateDeckPanel(GameType.Constructed);
        populateVanguardLists();
    }

    public void update() {
        activePlayersNum = lobby.getNumberOfSlots();
        addPlayerBtn.setEnabled(activePlayersNum < MAX_PLAYERS);

        for (final VariantCheckBox vcb : vntBoxes) {
            vcb.setSelected(hasVariant(vcb.variant));
            vcb.setEnabled(lobby.hasControl());
        }

        final boolean allowNetworking = lobby.isAllowNetworking();
        for (int i = 0; i < MAX_PLAYERS; i++) {
            final boolean hasPanel = i < playerPanels.size();
            if (i < activePlayersNum) {
                // visible panels
                final LobbySlot slot = lobby.getSlot(i);
                final PlayerPanel panel;
                if (hasPanel) {
                    panel = playerPanels.get(i);
                    panel.setVisible(true);
                } else {
                    panel = new PlayerPanel(this, allowNetworking, i, slot, lobby.mayEdit(i), lobby.hasControl());
                    playerPanels.add(panel);
                    String constraints = "pushx, growx, wrap, hidemode 3";
                    if (i == 0) {
                        constraints += ", gaptop 5px";
                        playerPanelWithFocus = panel;
                        playerPanelWithFocus.setFocused(true);
                    }
                    playersScroll.add(panel, constraints);
                }

                panel.setType(slot.getType());
                panel.setPlayerName(slot.getName());
                panel.setAvatar(slot.getAvatarIndex());
                panel.setTeam(slot.getTeam());
                panel.setIsReady(slot.isReady());
                panel.setIsArchenemy(slot.isArchenemy());
                panel.setUseAiSimulation(slot.getAiOptions().contains(AIOption.USE_SIMULATION));
                panel.setMayEdit(lobby.mayEdit(i));
                panel.setMayControl(lobby.mayControl(i));
                panel.setMayRemove(lobby.mayRemove(i));
                panel.update();

                deckChoosers.get(i).setIsAi(slot.getType() == LobbySlotType.AI);
            } else if (hasPanel) {
                playerPanels.get(i).setVisible(false);
            }
        }

        if (playerWithFocus >= activePlayersNum) {
            playerWithFocus = activePlayersNum - 1;
        }
        changePlayerFocus(playerWithFocus);
        refreshPanels(true, true);
    }

    public void setPlayerChangeListener(final IPlayerChangeListener listener) {
        this.playerChangeListener = listener;
    }

    void setReady(final int index, final boolean ready) {
        if (ready && decks[index] == null) {
            GuiBase.getInterface().showOptionDialog("Select a deck before readying!", "Error", FSkinProp.ICO_WARNING, new String[] { "Ok" }, 0);
            update();
            return;
        }

        firePlayerChangeListener(index);
        changePlayerFocus(index);
    }
    void firePlayerChangeListener(final int index) {
        if (playerChangeListener != null) {
            playerChangeListener.update(index, getSlot(index));
        }
    }
    void fireDeckChangeListener(final int index, final Deck deck) {
        decks[index] = deck;
        if (playerChangeListener != null) {
            playerChangeListener.update(index, UpdateLobbyPlayerEvent.deckUpdate(deck));
        }
    }
    void fireDeckSectionChangeListener(final int index, final DeckSection section, final CardPool cards) {
        decks[index].putSection(section, cards);
        if (playerChangeListener != null) {
            playerChangeListener.update(index, UpdateLobbyPlayerEvent.deckUpdate(section, cards));
        }
    }

    void removePlayer(final int index) {
        lobby.removeSlot(index);
    }
    boolean hasVariant(final GameType variant) {
        return lobby.hasVariant(variant);
    }

    private UpdateLobbyPlayerEvent getSlot(final int index) {
        final PlayerPanel panel = playerPanels.get(index);
        return UpdateLobbyPlayerEvent.create(panel.getType(), panel.getPlayerName(), panel.getAvatarIndex(), panel.getTeam(), panel.isArchenemy(), panel.isReady(), panel.getAiOptions());
    }

    /** Builds the actual deck panel layouts for each player.
     * These are added to a list which can be referenced to populate the deck panel appropriately. */
    @SuppressWarnings("serial")
    private void buildDeckPanel(final int playerIndex) {
        String sectionConstraints = "insets 0, gap 0, wrap";
        String labelConstraints = "gaptop 10px, gapbottom 5px";

        // Main deck
        final FDeckChooser mainChooser = new FDeckChooser(null, false);
        mainChooser.initialize();
        mainChooser.getLstDecks().setSelectCommand(new UiCommand() {
            @Override public final void run() {
                VLobby.this.onDeckClicked(playerIndex, mainChooser.getSelectedDeckType(), mainChooser.getLstDecks().getSelectedItems());
            }
        });
        deckChoosers.add(mainChooser);

        // Scheme deck list
        final FPanel schemeDeckPanel = new FPanel();
        schemeDeckPanel.setBorderToggle(false);
        schemeDeckPanel.setLayout(new MigLayout(sectionConstraints));
        schemeDeckPanel.add(new FLabel.Builder().text("Select Scheme deck:").build(), labelConstraints);
        final FList<Object> schemeDeckList = new FList<Object>();
        schemeDeckList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        schemeDeckList.addListSelectionListener(new ListSelectionListener() {
            @Override public final void valueChanged(final ListSelectionEvent e) {
                if (playerIndex >= activePlayersNum) {
                    return;
                }

                final Object selected = schemeDeckList.getSelectedValue();
                final Deck deck = decks[playerIndex];
                CardPool schemePool = null;
                if (selected instanceof String) {
                    String sel = (String) selected;
                    if (sel.contains("Use deck's scheme section")) {
                        if (deck.has(DeckSection.Schemes)) {
                            schemePool = deck.get(DeckSection.Schemes);
                        } else {
                            sel = "Random";
                        }
                    }
                    final IStorage<Deck> sDecks = FModel.getDecks().getScheme();
                    if (sel.equals("Random") && sDecks.size() != 0) {
                        schemePool = Aggregates.random(sDecks).get(DeckSection.Schemes);                            
                    }
                } else if (selected instanceof Deck) {
                    schemePool = ((Deck) selected).get(DeckSection.Schemes);
                }
                if (schemePool == null) { //Can be null if player deselects the list selection or chose Generate
                    schemePool = DeckgenUtil.generateSchemePool();
                }
                fireDeckSectionChangeListener(playerIndex, DeckSection.Schemes, schemePool);
                getDeckChooser(playerIndex).saveState();
            }
        });

        final FScrollPane scrSchemes = new FScrollPane(schemeDeckList, true,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        schemeDeckPanel.add(scrSchemes, "grow, push");
        schemeDeckLists.add(schemeDeckList);
        schemeDeckPanels.add(schemeDeckPanel);

        // Commander deck list
        final FPanel commanderDeckPanel = new FPanel();
        commanderDeckPanel.setBorderToggle(false);
        commanderDeckPanel.setLayout(new MigLayout(sectionConstraints));
        commanderDeckPanel.add(new FLabel.Builder().text("Select Commander deck:").build(), labelConstraints);
        final FList<Object> commanderDeckList = new FList<Object>();
        commanderDeckList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        commanderDeckList.addListSelectionListener(new ListSelectionListener() {
            @Override public final void valueChanged(final ListSelectionEvent e) {
                if (playerIndex >= activePlayersNum) {
                    return;
                }

                final Object selected = commanderDeckList.getSelectedValue();
                Deck deck = null;
                if (selected instanceof String) {
                    final String sel = (String) selected;
                    final IStorage<Deck> comDecks = FModel.getDecks().getCommander();
                    if (sel.equals("Random") && comDecks.size() > 0) {
                        deck = Aggregates.random(comDecks);
                    }
                } else if (selected instanceof Deck) {
                    deck = (Deck) selected;
                }
                final GameType commanderGameType = hasVariant(GameType.TinyLeaders) ? GameType.TinyLeaders : GameType.Commander;
                if (deck == null) { //Can be null if player deselects the list selection or chose Generate
                    deck = DeckgenUtil.generateCommanderDeck(isPlayerAI(playerIndex), commanderGameType);
                }
                fireDeckChangeListener(playerIndex, deck);
                getDeckChooser(playerIndex).saveState();
            }
        });
        

        final FScrollPane scrCommander = new FScrollPane(commanderDeckList, true,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        commanderDeckPanel.add(scrCommander, "grow, push");
        commanderDeckLists.add(commanderDeckList);
        commanderDeckPanels.add(commanderDeckPanel);

        // Planar deck list
        final FPanel planarDeckPanel = new FPanel();
        planarDeckPanel.setBorderToggle(false);
        planarDeckPanel.setLayout(new MigLayout(sectionConstraints));
        planarDeckPanel.add(new FLabel.Builder().text("Select Planar deck:").build(), labelConstraints);
        final FList<Object> planarDeckList = new FList<Object>();
        planarDeckList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        planarDeckList.addListSelectionListener(new ListSelectionListener() {
            @Override public final void valueChanged(final ListSelectionEvent e) {
                if (playerIndex >= activePlayersNum) {
                    return;
                }

                final Object selected = planarDeckList.getSelectedValue();
                final Deck deck = decks[playerIndex];
                CardPool planePool = null;
                if (selected instanceof String) {
                    String sel = (String) selected;
                    if (sel.contains("Use deck's planes section")) {
                        if (deck.has(DeckSection.Planes)) {
                            planePool = deck.get(DeckSection.Planes);
                        } else {
                            sel = "Random";
                        }
                    }
                    final IStorage<Deck> pDecks = FModel.getDecks().getPlane();
                    if (sel.equals("Random") && pDecks.size() != 0) {
                        planePool = Aggregates.random(pDecks).get(DeckSection.Planes);                            
                    }
                } else if (selected instanceof Deck) {
                    planePool = ((Deck) selected).get(DeckSection.Planes);
                }
                if (planePool == null) { //Can be null if player deselects the list selection or chose Generate
                    planePool = DeckgenUtil.generatePlanarPool();
                }
                fireDeckSectionChangeListener(playerIndex, DeckSection.Planes, planePool);
                getDeckChooser(playerIndex).saveState();
            }
        });

        final FScrollPane scrPlanes = new FScrollPane(planarDeckList, true,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        planarDeckPanel.add(scrPlanes, "grow, push");
        planarDeckLists.add(planarDeckList);
        planarDeckPanels.add(planarDeckPanel);

        // Vanguard avatar list
        FPanel vgdDeckPanel = new FPanel();
        vgdDeckPanel.setBorderToggle(false);

        final FList<Object> vgdAvatarList = new FList<Object>();
        vgdAvatarList.setListData(isPlayerAI(playerIndex) ? aiListData : humanListData);
        vgdAvatarList.setSelectedIndex(0);
        vgdAvatarList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        vgdAvatarList.addListSelectionListener(vgdLSListener);
        final FScrollPane scrAvatars = new FScrollPane(vgdAvatarList, true,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        final CardDetailPanel vgdDetail = new CardDetailPanel();
        vgdAvatarDetails.add(vgdDetail);

        vgdDeckPanel.setLayout(new MigLayout(sectionConstraints));
        vgdDeckPanel.add(new FLabel.Builder().text("Select a Vanguard avatar:").build(), labelConstraints);
        vgdDeckPanel.add(scrAvatars, "grow, push");
        vgdDeckPanel.add(vgdDetail, "h 200px, pushx, growx, hidemode 3");
        vgdAvatarLists.add(vgdAvatarList);
        vgdPanels.add(vgdDeckPanel);
    }

    protected void onDeckClicked(final int iPlayer, final DeckType type, final Collection<DeckProxy> selectedDecks) {
        if (iPlayer < activePlayersNum && lobby.mayEdit(iPlayer)) {
            final String text = type.toString() + ": " + Lang.joinHomogenous(selectedDecks, DeckProxy.FN_GET_NAME);
            playerPanels.get(iPlayer).setDeckSelectorButtonText(text);
            fireDeckChangeListener(iPlayer, selectedDecks.iterator().next().getDeck());
        }
    }

    /** Populates the deck panel with the focused player's deck choices. */
    private void populateDeckPanel(final GameType forGameType) {
        decksFrame.removeAll();

        if (!lobby.mayEdit(playerWithFocus)) {
            return;
        }

        if (GameType.Constructed == forGameType) {
            decksFrame.add(deckChoosers.get(playerWithFocus), "grow, push");
            if (deckChoosers.get(playerWithFocus).getSelectedDeckType().toString().contains("Random")) {
                final String strCheckboxConstraints = "h 30px!, gap 0 20px 0 0";
                decksFrame.add(cbSingletons, strCheckboxConstraints);
                decksFrame.add(cbArtifacts, strCheckboxConstraints);
            }
        } else if (GameType.Archenemy == forGameType || GameType.ArchenemyRumble == forGameType) {
            if (isPlayerArchenemy(playerWithFocus)) {
                decksFrame.add(schemeDeckPanels.get(playerWithFocus), "grow, push");
            } else {
                populateDeckPanel(GameType.Constructed);
            }
        } else if (GameType.Commander == forGameType || GameType.TinyLeaders == forGameType) {
            decksFrame.add(commanderDeckPanels.get(playerWithFocus), "grow, push");
        } else if (GameType.Planechase == forGameType) {
            decksFrame.add(planarDeckPanels.get(playerWithFocus), "grow, push");
        } else if (GameType.Vanguard == forGameType) {
            updateVanguardList(playerWithFocus);
            decksFrame.add(vgdPanels.get(playerWithFocus), "grow, push");
        }
        refreshPanels(false, true);
    }

    /** @return {@link javax.swing.JButton} */
    JButton getBtnStart() {
        return this.btnStart;
    }

    public LblHeader getLblTitle() { return lblTitle; }
    public JPanel getConstructedFrame() { return constructedFrame; }
    public JPanel getPanelStart() { return pnlStart; }
    public List<FDeckChooser> getDeckChoosers() { return Collections.unmodifiableList(deckChoosers); }

    /** Gets the random deck checkbox for Singletons. */
    FCheckBox getCbSingletons() { return cbSingletons; }

    /** Gets the random deck checkbox for Artifacts. */
    FCheckBox getCbArtifacts() { return cbArtifacts; }

    FCheckBox getVntArchenemy()       { return vntArchenemy; }
    FCheckBox getVntArchenemyRumble() { return vntArchenemyRumble; }
    FCheckBox getVntCommander()       { return vntCommander; }
    FCheckBox getVntMomirBasic()      { return vntMomirBasic; }
    FCheckBox getVntPlanechase()      { return vntPlanechase; }
    FCheckBox getVntTinyLeaders()     { return vntTinyLeaders; }
    FCheckBox getVntVanguard()        { return vntVanguard; }

    public int getLastArchenemy() { return lastArchenemy; }
    public void setLastArchenemy(final int archenemy) { lastArchenemy = archenemy; }

    public final List<PlayerPanel> getPlayerPanels() {
        return playerPanels;
    }
    public final PlayerPanel getPlayerPanelWithFocus() {
        return playerPanelWithFocus;
    }

    public final FDeckChooser getDeckChooser(int playernum) {
        return deckChoosers.get(playernum);
    }

    GameType getCurrentGameMode() {
        return lobby.getGameType();
    }
    void setCurrentGameMode(final GameType mode) {
        lobby.setGameType(mode);
        update();
    }

    public boolean isPlayerAI(final int playernum) {
        return playernum < activePlayersNum ? playerPanels.get(playernum).isAi() : false;
    }

    public int getNumPlayers() {
        return activePlayersNum;
    }

    /** Revalidates the player and deck sections. Necessary after adding or hiding any panels. */
    private void refreshPanels(boolean refreshPlayerFrame, boolean refreshDeckFrame) {
        if (refreshPlayerFrame) {
            playersScroll.validate();
            playersScroll.repaint();
        }
        if (refreshDeckFrame) {
            decksFrame.validate();
            decksFrame.repaint();
        }
    }

    public void changePlayerFocus(int newFocusOwner) {
        changePlayerFocus(newFocusOwner, lobby.getGameType());
    }

    void changePlayerFocus(int newFocusOwner, GameType gType) {
        playerPanelWithFocus.setFocused(false);
        playerWithFocus = newFocusOwner;
        playerPanelWithFocus = playerPanels.get(playerWithFocus);
        playerPanelWithFocus.setFocused(true);

        playersScroll.getViewport().scrollRectToVisible(playerPanelWithFocus.getBounds());
        populateDeckPanel(gType);

        refreshPanels(true, true);
    }

    /** Saves avatar prefs for players one and two. */
    void updateAvatarPrefs() {
        int pOneIndex = playerPanels.get(0).getAvatarIndex();
        int pTwoIndex = playerPanels.get(1).getAvatarIndex();

        prefs.setPref(FPref.UI_AVATARS, pOneIndex + "," + pTwoIndex);
        prefs.save();
    }

    /** Adds a pre-styled FLabel component with the specified title. */
    FLabel newLabel(String title) {
        return new FLabel.Builder().text(title).fontSize(14).fontStyle(Font.ITALIC).build();
    }

    List<Integer> getUsedAvatars() {
        final List<Integer> usedAvatars = Lists.newArrayListWithCapacity(MAX_PLAYERS);
        for (final PlayerPanel pp : playerPanels) {
            usedAvatars.add(pp.getAvatarIndex());
        }
        return usedAvatars;
    }

    final String getNewName() {
        final String title = "Get new random name";
        final String message = "What type of name do you want to generate?";
        final SkinImage icon = FOptionPane.QUESTION_ICON;
        final String[] genderOptions = new String[]{ "Male", "Female", "Any" };
        final String[] typeOptions = new String[]{ "Fantasy", "Generic", "Any" };

        final int genderIndex = FOptionPane.showOptionDialog(message, title, icon, genderOptions, 2);
        if (genderIndex < 0) {
            return null;
        }
        final int typeIndex = FOptionPane.showOptionDialog(message, title, icon, typeOptions, 2);
        if (typeIndex < 0) {
            return null;
        }

        final String gender = genderOptions[genderIndex];
        final String type = typeOptions[typeIndex];

        String confirmMsg, newName;
        List<String> usedNames = getPlayerNames();
        do {
            newName = NameGenerator.getRandomName(gender, type, usedNames);
            confirmMsg = "Would you like to use the name \"" + newName + "\", or try again?";
        } while (!FOptionPane.showConfirmDialog(confirmMsg, title, "Use this name", "Try again", true));

        return newName;
    }

    List<String> getPlayerNames() {
        List<String> names = new ArrayList<String>();
        for (PlayerPanel pp : playerPanels) {
            names.add(pp.getPlayerName());
        }
        return names;
    }

    /////////////////////////////////////////////
    //========== Various listeners in build order

    @SuppressWarnings("serial") private class VariantCheckBox extends FCheckBox {
        private final GameType variant;
        private VariantCheckBox(final GameType variantType) {
            super(variantType.toString());
            this.variant = variantType;

            setToolTipText(variantType.getDescription());
            addItemListener(new ItemListener() {
                @Override public final void itemStateChanged(final ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        lobby.applyVariant(variantType);
                    } else {
                        lobby.removeVariant(variantType);
                    }
                    VLobby.this.update();
                }
            });
        }
    }

    final ActionListener nameListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            FTextField nField = (FTextField)e.getSource();
            nField.transferFocus();
        }
    };

    /** This listener will look for a vanguard avatar being selected in the lists
    / and update the corresponding detail panel. */
    private ListSelectionListener vgdLSListener = new ListSelectionListener() {
        @Override public final void valueChanged(final ListSelectionEvent e) {
            final int index = vgdAvatarLists.indexOf(e.getSource());
            if (index >= activePlayersNum) {
                return;
            }
            final Object selected = vgdAvatarLists.get(index).getSelectedValue();
            final PlayerPanel pp = playerPanels.get(index);
            final CardDetailPanel cdp = vgdAvatarDetails.get(index);

            final PaperCard vanguardAvatar;
            final Deck deck = decks[index];
            if (selected instanceof PaperCard) {
                pp.setVanguardButtonText(((PaperCard) selected).getName());
                cdp.setCard(CardView.getCardForUi((PaperCard) selected));
                cdp.setVisible(true);
                refreshPanels(false, true);

                vanguardAvatar = (PaperCard)selected;
            } else {
                String sel = (String) selected;
                pp.setVanguardButtonText(sel);
                cdp.setVisible(false);

                if (sel.contains("Use deck's default avatar") && deck.has(DeckSection.Avatar)) {
                    vanguardAvatar = deck.get(DeckSection.Avatar).get(0);
                } else { //Only other string is "Random"
                    if (playerPanels.get(index).isAi()) { //AI
                        vanguardAvatar = Aggregates.random(getNonRandomAiAvatars());
                    } else { //Human
                        vanguardAvatar = Aggregates.random(getNonRandomHumanAvatars());
                    }
                }
            }

            final CardPool avatarOnce = new CardPool();
            avatarOnce.add(vanguardAvatar);
            fireDeckSectionChangeListener(index, DeckSection.Avatar, avatarOnce);
            getDeckChooser(index).saveState();
        }
    };


    /////////////////////////////////////
    //========== METHODS FOR VARIANTS

    /** Gets the list of planar deck lists. */
    public List<FList<Object>> getPlanarDeckLists() {
        return planarDeckLists;
    }

    /** Gets the list of commander deck lists. */
    public List<FList<Object>> getCommanderDeckLists() {
        return commanderDeckLists;
    }

    /** Gets the list of scheme deck lists. */
    public List<FList<Object>> getSchemeDeckLists() {
        return schemeDeckLists;
    }

    public boolean isPlayerArchenemy(final int playernum) {
        return playerPanels.get(playernum).isArchenemy();
    }

    /** Gets the list of Vanguard avatar lists. */
    public List<FList<Object>> getVanguardLists() {
        return vgdAvatarLists;
    }

    /** Return all the Vanguard avatars. */
    public Iterable<PaperCard> getAllAvatars() {
        if (vgdAllAvatars.isEmpty()) {
            for (PaperCard c : FModel.getMagicDb().getVariantCards().getAllCards()) {
                if (c.getRules().getType().isVanguard()) {
                    vgdAllAvatars.add(c);
                }
            }
        }
        return vgdAllAvatars;
    }

    /** Return the Vanguard avatars not flagged RemAIDeck. */
    public List<PaperCard> getAllAiAvatars() {
        return vgdAllAiAvatars;
    }

    /** Return the Vanguard avatars not flagged RemRandomDeck. */
    public List<PaperCard> getNonRandomHumanAvatars() {
        return nonRandomHumanAvatars;
    }

    /** Return the Vanguard avatars not flagged RemAIDeck or RemRandomDeck. */
    public List<PaperCard> getNonRandomAiAvatars() {
        return nonRandomAiAvatars;
    }

    /** Populate vanguard lists. */
    private void populateVanguardLists() {
        humanListData.add("Use deck's default avatar (random if unavailable)");
        humanListData.add("Random");
        aiListData.add("Use deck's default avatar (random if unavailable)");
        aiListData.add("Random");
        for (PaperCard cp : getAllAvatars()) {
            humanListData.add(cp);
            if (!cp.getRules().getAiHints().getRemRandomDecks()) {
                nonRandomHumanAvatars.add(cp);
            }
            if (!cp.getRules().getAiHints().getRemAIDecks()) {
                aiListData.add(cp);
                vgdAllAiAvatars.add(cp);
                if (!cp.getRules().getAiHints().getRemRandomDecks()) {
                    nonRandomAiAvatars.add(cp);
                }
            }
        }
    }

    /** update vanguard list. */
    public void updateVanguardList(int playerIndex) {
        FList<Object> vgdList = getVanguardLists().get(playerIndex);
        Object lastSelection = vgdList.getSelectedValue();
        vgdList.setListData(isPlayerAI(playerIndex) ? aiListData : humanListData);
        if (null != lastSelection) {
            vgdList.setSelectedValue(lastSelection, true);
        }

        if (-1 == vgdList.getSelectedIndex()) {
            vgdList.setSelectedIndex(0);
        }
    }
}
