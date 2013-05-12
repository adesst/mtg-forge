package forge.card.ability.ai;

import java.util.List;
import java.util.Random;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import forge.Card;
import forge.CardLists;
import forge.CardPredicates.Presets;
import forge.card.ability.SpellAbilityAi;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.Target;
import forge.game.GameState;
import forge.game.phase.CombatUtil;
import forge.game.phase.PhaseType;
import forge.game.player.Player;
import forge.game.zone.ZoneType;
import forge.util.MyRandom;

public class TapAllAi extends SpellAbilityAi {

    /* (non-Javadoc)
     * @see forge.card.abilityfactory.SpellAiLogic#canPlayAI(forge.game.player.Player, java.util.Map, forge.card.spellability.SpellAbility)
     */
    @Override
    protected boolean canPlayAI(final Player ai, SpellAbility sa) {
        // If tapping all creatures do it either during declare attackers of AIs
        // turn
        // or during upkeep/begin combat?

        final Card source = sa.getSourceCard();
        final Player opp = ai.getOpponent();
        final GameState game = ai.getGame();

        if (game.getPhaseHandler().getPhase().isAfter(PhaseType.COMBAT_BEGIN)) {
            return false;
        }

        String valid = "";
        if (sa.hasParam("ValidCards")) {
            valid = sa.getParam("ValidCards");
        }

        List<Card> validTappables = game.getCardsIn(ZoneType.Battlefield);

        final Target tgt = sa.getTarget();

        if (sa.getTarget() != null) {
            tgt.resetTargets();
            tgt.addTarget(opp);
            validTappables = opp.getCardsIn(ZoneType.Battlefield);
        }

        validTappables = CardLists.getValidCards(validTappables, valid, source.getController(), source);
        validTappables = CardLists.filter(validTappables, Presets.UNTAPPED);

        final Random r = MyRandom.getRandom();
        if (r.nextFloat() > Math.pow(.6667, sa.getActivationsThisTurn())) {
            return false;
        }

        if (validTappables.isEmpty()) {
            return false;
        }

        final List<Card> human = CardLists.filter(validTappables, new Predicate<Card>() {
            @Override
            public boolean apply(final Card c) {
                return c.getController().equals(opp);
            }
        });
        final List<Card> compy = CardLists.filter(validTappables, new Predicate<Card>() {
            @Override
            public boolean apply(final Card c) {
                return c.getController().equals(ai);
            }
        });
        if (human.size() <= compy.size()) {
            return false;
        }
        // in AI's turn, check if there are possible attackers, before tapping blockers
        if (game.getPhaseHandler().isPlayerTurn(ai) && !SpellAbilityAi.isSorcerySpeed(sa)) {
            validTappables = ai.getCardsIn(ZoneType.Battlefield);
            final boolean any = Iterables.any(validTappables, new Predicate<Card>() {
                @Override
                public boolean apply(final Card c) {
                    return CombatUtil.canAttack(c) && CombatUtil.canAttackNextTurn(c);
                }
            });
            if(!any) {
                return false;
            }
        }
        return true;
    }

    /**
     * <p>
     * getTapAllTargets.
     * </p>
     * 
     * @param valid
     *            a {@link java.lang.String} object.
     * @param source
     *            a {@link forge.Card} object.
     * @return a {@link forge.CardList} object.
     */
    private List<Card> getTapAllTargets(final String valid, final Card source) {
        final GameState game = source.getGame();
        List<Card> tmpList = game.getCardsIn(ZoneType.Battlefield);
        tmpList = CardLists.getValidCards(tmpList, valid, source.getController(), source);
        tmpList = CardLists.filter(tmpList, Presets.UNTAPPED);
        return tmpList;
    }

    @Override
    protected boolean doTriggerAINoCost(final Player ai, SpellAbility sa, boolean mandatory) {
        final Card source = sa.getSourceCard();

        String valid = "";
        if (sa.hasParam("ValidCards")) {
            valid = sa.getParam("ValidCards");
        }

        List<Card> validTappables = getTapAllTargets(valid, source);

        final Target tgt = sa.getTarget();

        if (tgt != null) {
            tgt.resetTargets();
            tgt.addTarget(ai.getOpponent());
            validTappables = ai.getOpponent().getCardsIn(ZoneType.Battlefield);
        }

        if (mandatory) {
            return true;
        }

        final Random r = MyRandom.getRandom();
        boolean rr = false;
        if (r.nextFloat() <= Math.pow(.6667, sa.getActivationsThisTurn())) {
            rr = true;
        }

        if (validTappables.size() > 0) {
            final int human = Iterables.size(Iterables.filter(validTappables, new Predicate<Card>() {
                @Override
                public boolean apply(final Card c) {
                    return c.getController().isOpponentOf(ai);
                }
            }));
            final int compy = Iterables.size(Iterables.filter(validTappables, new Predicate<Card>() {
                @Override
                public boolean apply(final Card c) {
                    return !c.getController().isOpponentOf(ai);
                }
            }));
            if (human > compy) {
                return rr;
            }
        }

        return false;
    }
}
