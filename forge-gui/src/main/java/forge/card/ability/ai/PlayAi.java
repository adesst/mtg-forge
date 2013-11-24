package forge.card.ability.ai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import com.google.common.base.Predicate;

import forge.card.ability.AbilityUtils;
import forge.card.ability.SpellAbilityAi;
import forge.card.cost.Cost;
import forge.card.spellability.Spell;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.TargetRestrictions;
import forge.game.ai.ComputerUtilCard;
import forge.game.ai.ComputerUtilCost;
import forge.game.card.Card;
import forge.game.card.CardLists;
import forge.game.player.Player;
import forge.game.player.PlayerActionConfirmMode;
import forge.game.zone.ZoneType;
import forge.util.MyRandom;

public class PlayAi extends SpellAbilityAi {

    @Override
    protected boolean canPlayAI(Player ai, SpellAbility sa) {
        final Cost abCost = sa.getPayCosts();
        final Card source = sa.getSourceCard();

        final Random r = MyRandom.getRandom();

        if (abCost != null) {
            // AI currently disabled for these costs
            if (!ComputerUtilCost.checkSacrificeCost(ai, abCost, source)) {
                return false;
            }

            if (!ComputerUtilCost.checkLifeCost(ai, abCost, source, 4, null)) {
                return false;
            }

            if (!ComputerUtilCost.checkDiscardCost(ai, abCost, source)) {
                return false;
            }

            if (!ComputerUtilCost.checkRemoveCounterCost(abCost, source)) {
                return false;
            }
        }

        // don't use this as a response
        if (!ai.getGame().getStack().isEmpty()) {
            return false;
        }

        // prevent run-away activations - first time will always return true
        boolean chance = r.nextFloat() <= Math.pow(.6667, sa.getRestrictions().getNumberTurnActivations());

        List<Card> cards;
        final TargetRestrictions tgt = sa.getTargetRestrictions();
        if (tgt != null) {
            ZoneType zone = tgt.getZone().get(0);
            cards = CardLists.getValidCards(ai.getGame().getCardsIn(zone), tgt.getValidTgts(), ai, source);
            if (cards.isEmpty()) {
                return false;
            }
            sa.getTargets().add(ComputerUtilCard.getBestAI(cards));
        } else if (!sa.hasParam("Valid")) {
            cards = new ArrayList<Card>(AbilityUtils.getDefinedCards(sa.getSourceCard(), sa.getParam("Defined"), sa));
            if (cards.isEmpty()) {
                return false;
            }
        }
        return chance;
    }
    
    /**
     * <p>
     * doTriggerAINoCost
     * </p>
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @param mandatory
     *            a boolean.
     * @param af
     *            a {@link forge.card.ability.AbilityFactory} object.
     *
     * @return a boolean.
     */
    @Override
    protected boolean doTriggerAINoCost(final Player ai, final SpellAbility sa, final boolean mandatory) {

        final TargetRestrictions tgt = sa.getTargetRestrictions();
        if (tgt != null) {
            return false;
        }

        return true;
    }
    
    /* (non-Javadoc)
     * @see forge.card.ability.SpellAbilityAi#confirmAction(forge.game.player.Player, forge.card.spellability.SpellAbility, forge.game.player.PlayerActionConfirmMode, java.lang.String)
     */
    @Override
    public boolean confirmAction(Player player, SpellAbility sa, PlayerActionConfirmMode mode, String message) {
        // as called from PlayEffect:173
        return true;
    }
    
    /* (non-Javadoc)
     * @see forge.card.ability.SpellAbilityAi#chooseSingleCard(forge.game.player.Player, forge.card.spellability.SpellAbility, java.util.List, boolean)
     */
    @Override
    public Card chooseSingleCard(final Player ai, SpellAbility sa, Collection<Card> options, boolean isOptional) {
        List<Card> tgtCards = CardLists.filter(options, new Predicate<Card>() {
            @Override
            public boolean apply(final Card c) {
                for (SpellAbility s : c.getBasicSpells()) {
                    Spell spell = (Spell) s;
                    s.setActivatingPlayer(ai);
                    // timing restrictions still apply
                    if (s.getRestrictions().checkTimingRestrictions(c, s) && spell.canPlayFromEffectAI(false, true)) {
                        return true;
                    }
                }
                return false;
            }
        });
        return ComputerUtilCard.getBestAI(tgtCards);
    }
}
