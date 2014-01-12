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
package forge.game.cost;

import java.util.ArrayList;
import java.util.List;
import forge.game.ability.AbilityUtils;
import forge.game.card.Card;
import forge.game.card.CardLists;
import forge.game.player.Player;
import forge.game.spellability.SpellAbility;
import forge.game.zone.ZoneType;
import forge.gui.input.InputSelectCardsFromList;

/**
 * The Class CostReturn.
 */
public class CostGainControl extends CostPartWithList {
    // GainControl<Num/Type{/TypeDescription}>

    /**
     * Instantiates a new cost return.
     * 
     * @param amount
     *            the amount
     * @param type
     *            the type
     * @param description
     *            the description
     */
    public CostGainControl(final String amount, final String type, final String description) {
        super(amount, type, description);
    }

    /*
     * (non-Javadoc)
     * 
     * @see forge.card.cost.CostPart#toString()
     */
    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder();
        final String desc = this.getTypeDescription() == null ? this.getType() : this.getTypeDescription();
        sb.append("Gain control of ").append(Cost.convertAmountTypeToWords(this.getAmount(), desc));
        return sb.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * forge.card.cost.CostPart#canPay(forge.card.spellability.SpellAbility,
     * forge.Card, forge.Player, forge.card.cost.Cost)
     */
    @Override
    public final boolean canPay(final SpellAbility ability) {
        final Player activator = ability.getActivatingPlayer();
        final Card source = ability.getSourceCard();
        List<Card> typeList = new ArrayList<Card>(activator.getGame().getCardsIn(ZoneType.Battlefield));
        typeList = CardLists.getValidCards(typeList, this.getType().split(";"), activator, source);

        Integer amount = this.convertAmount();
        if (amount == null) {
            amount = AbilityUtils.calculateAmount(source, this.getAmount(), ability);
        }
        if (typeList.size() < amount) {
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * forge.card.cost.CostPart#payHuman(forge.card.spellability.SpellAbility,
     * forge.Card, forge.card.cost.Cost_Payment)
     */
    @Override
    public final PaymentDecision payHuman(final SpellAbility ability, final Player payer) {
        final String amount = this.getAmount();
        final Card source = ability.getSourceCard();

        Integer c = this.convertAmount();
        if (c == null) {
            c = AbilityUtils.calculateAmount(source, amount, ability);
        }
        final List<Card> list = payer.getCardsIn(ZoneType.Battlefield);
        List<Card> validCards = CardLists.getValidCards(list, this.getType().split(";"), payer, source);

        InputSelectCardsFromList inp = new InputSelectCardsFromList(c, validCards);
        final String desc = this.getTypeDescription() == null ? this.getType() : this.getTypeDescription();
        inp.setMessage("Gain control of %d " + desc);
        inp.showAndWait();
        if (inp.hasCancelled()) {
            return null;
        }
        return PaymentDecision.card(inp.getSelected());
    }

    /* (non-Javadoc)
     * @see forge.card.cost.CostPartWithList#executePayment(forge.card.spellability.SpellAbility, forge.Card)
     */
    @Override
    protected void doPayment(SpellAbility ability, Card targetCard) {
        targetCard.setController(ability.getActivatingPlayer(), ability.getActivatingPlayer().getGame().getNextTimestamp());
    }

    /* (non-Javadoc)
     * @see forge.card.cost.CostPartWithList#getHashForList()
     */
    @Override
    public String getHashForList() {
        return "ControllGained"; // why the hell double "L"?
    }

    public <T> T accept(ICostVisitor<T> visitor) {
        return visitor.visit(this);
    }

}
