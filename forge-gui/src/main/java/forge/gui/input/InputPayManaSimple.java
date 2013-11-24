/*
 * Forge: Play Magic: the Gathering.
 * Copyright (C) 2011  Forge Team
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
package forge.gui.input;

import forge.card.mana.ManaCostBeingPaid;
import forge.card.spellability.SpellAbility;
import forge.game.Game;
import forge.game.card.Card;
import forge.game.player.Player;
import forge.view.ButtonUtil;

//pays the cost of a card played from the player's hand
//the card is removed from the players hand if the cost is paid
//CANNOT be used for ABILITIES
public class InputPayManaSimple extends InputPayMana {
    // anything that uses this should be converted to Ability_Cost
    /** Constant <code>serialVersionUID=3467312982164195091L</code>. */
    private static final long serialVersionUID = 3467312982164195091L;

    private final Card originalCard;
    private final String originalManaCost;

    public InputPayManaSimple(final Game game, final SpellAbility sa, final ManaCostBeingPaid manaCostToPay) {
        super(sa);
        this.originalManaCost = manaCostToPay.toString(); // Change
        this.originalCard = sa.getSourceCard();

        if (sa.getSourceCard().isCopiedSpell() && sa.isSpell()) {
            this.manaCost = new ManaCostBeingPaid("0");
            game.getStack().add(this.saPaidFor);
        } else {
            this.manaCost = manaCostToPay;
        }
        
    }

    protected void onManaAbilityPaid() {
        if (this.manaCost.isPaid()) {
            this.originalCard.setSunburstValue(this.manaCost.getSunburst());
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void selectPlayer(final Player selectedPlayer) {

        if (player == selectedPlayer) {
            if (player.canPayLife(this.phyLifeToLose + 2) && manaCost.payPhyrexian()) {
                this.phyLifeToLose += 2;
            }

            this.showMessage();
        }

    }

    /**
     * <p>
     * done.
     * </p>
     */
    @Override
    protected void done() {

        this.originalCard.setSunburstValue(this.manaCost.getSunburst());

        if (this.phyLifeToLose > 0) {
            player.payLife(this.phyLifeToLose, this.originalCard);
        }
        if (!this.saPaidFor.getSourceCard().isCopiedSpell()) {
            player.getManaPool().clearManaPaid(this.saPaidFor, false);

            if (this.saPaidFor.isSpell()) {
                this.saPaidFor.setSourceCard(game.getAction().moveToStack(this.originalCard));
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    protected final void onCancel() {
        player.getManaPool().refundManaPaid(this.saPaidFor);
        // Update UI

        this.stop();
    }

    /** {@inheritDoc} */
    @Override
    public final void showMessage() {
        if( isFinished() ) return;
        
        ButtonUtil.enableOnlyCancel();


        if (this.manaCost.isPaid() && !new ManaCostBeingPaid(this.originalManaCost).isPaid()) {
            this.done();
            this.stop();
        } else
            updateMessage();

    }

    /* (non-Javadoc)
     * @see forge.control.input.InputPayManaBase#updateMessage()
     */
    @Override
    protected void updateMessage() {
        final StringBuilder msg = new StringBuilder("Pay Mana Cost: " + this.manaCost.toString());
        if (this.phyLifeToLose > 0) {
            msg.append(" (");
            msg.append(this.phyLifeToLose);
            msg.append(" life paid for phyrexian mana)");
        }

        if (this.manaCost.containsPhyrexianMana()) {
            msg.append("\n(Click on your life total to pay life for phyrexian mana.)");
        }

        // has its own variant of checkIfPaid
        showMessage(msg.toString());
        
    }
}
