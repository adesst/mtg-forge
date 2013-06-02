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
package forge.game.phase;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Predicate;

import forge.Card;
import forge.CardLists;
import forge.card.cost.Cost;
import forge.card.mana.ManaCost;
import forge.card.staticability.StaticAbility;
import forge.game.Game;
import forge.game.player.Player;
import forge.game.player.PlayerController.ManaPaymentPurpose;
import forge.game.zone.ZoneType;

/**
 * <p>
 * PhaseUtil class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class PhaseUtil {
    // ******* UNTAP PHASE *****
    /**
     * <p>
     * skipUntap.
     * </p>
     * 
     * @param p
     *            a {@link forge.game.player.Player} object.
     * @return a boolean.
     */
    static boolean isSkipUntap(final Player p) {

        if (p.hasKeyword("Skip your next untap step.")) {
            p.removeKeyword("Skip your next untap step.");
            return true;
        }
        if (p.hasKeyword("Skip the untap step of this turn.")
                || p.hasKeyword("Skip your untap step.")) {
            return true;
        }

        return false;
    }


    /**
     * <p>
     * handleDeclareBlockers.
     * </p>
     * 
     * @param game
     */
    public static void handleDeclareBlockers(Game game) {
        final Combat combat = game.getCombat();

        // Handles removing cards like Mogg Flunkies from combat if group block
        // didn't occur
        final List<Card> filterList = combat.getAllBlockers();
        for (Card blocker : filterList) {
            final List<Card> attackers = new ArrayList<Card>(combat.getAttackersBlockedBy(blocker));
            for (Card attacker : attackers) {
                boolean hasPaid = payRequiredBlockCosts(game, blocker, attacker);

                if ( !hasPaid ) {
                    combat.removeBlockAssignment(attacker, blocker);
                }
            }
        }
        for (Card c : filterList) {
            if (c.hasKeyword("CARDNAME can't attack or block alone.") && c.isBlocking()) {
                if (combat.getAllBlockers().size() < 2) {
                    combat.undoBlockingAssignment(c);
                }
            }
        }

        combat.setUnblockedAttackers();

        List<Card> list = combat.getAllBlockers();

        list = CardLists.filter(list, new Predicate<Card>() {
            @Override
            public boolean apply(final Card c) {
                return !c.getDamageHistory().getCreatureBlockedThisCombat();
            }
        });

        CombatUtil.checkDeclareBlockers(game, list);

        for (final Card a : combat.getAttackers()) {
            CombatUtil.checkBlockedAttackers(game, a, combat.getBlockers(a));
        }
    }


    private static boolean payRequiredBlockCosts(Game game, Card blocker, Card attacker) {
        Cost blockCost = new Cost(ManaCost.ZERO, true);
        // Sort abilities to apply them in proper order
        for (Card card : game.getCardsIn(ZoneType.Battlefield)) {
            final ArrayList<StaticAbility> staticAbilities = card.getStaticAbilities();
            for (final StaticAbility stAb : staticAbilities) {
                Cost c1 = stAb.getBlockCost(blocker, attacker);
                if ( c1 != null )
                    blockCost.add(c1);
            }
        }
        
        boolean hasPaid = blockCost.getTotalMana().isZero() && blockCost.isOnlyManaCost(); // true if needless to pay
        if (!hasPaid) { 
            hasPaid = blocker.getController().getController().payManaOptional(blocker, blockCost, "Pay cost to declare " + blocker + " a blocker", ManaPaymentPurpose.DeclareBlocker);
        }
        return hasPaid;
    }
}
