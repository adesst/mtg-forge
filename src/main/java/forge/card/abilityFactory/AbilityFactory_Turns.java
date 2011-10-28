package forge.card.abilityFactory;

import java.util.ArrayList;
import java.util.HashMap;

import forge.AllZone;
import forge.Player;
import forge.card.spellability.Ability_Activated;
import forge.card.spellability.Ability_Sub;
import forge.card.spellability.Spell;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.Target;

/**
 * <p>
 * AbilityFactory_Turns class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class AbilityFactory_Turns {

    // *************************************************************************
    // ************************* ADD TURN **************************************
    // *************************************************************************

    /**
     * <p>
     * createAbilityAddTurn.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createAbilityAddTurn(final AbilityFactory af) {

        final SpellAbility abAddTurn = new Ability_Activated(af.getHostCard(), af.getAbCost(), af.getAbTgt()) {
            private static final long serialVersionUID = -3526200766738015688L;

            @Override
            public String getStackDescription() {
                return addTurnStackDescription(af, this);
            }

            @Override
            public boolean canPlayAI() {
                return addTurnCanPlayAI(af, this);
            }

            @Override
            public void resolve() {
                addTurnResolve(af, this);
            }

            @Override
            public boolean doTrigger(final boolean mandatory) {
                return addTurnTriggerAI(af, this, mandatory);
            }

        };
        return abAddTurn;
    }

    /**
     * <p>
     * createSpellAddTurn.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createSpellAddTurn(final AbilityFactory af) {
        final SpellAbility spAddTurn = new Spell(af.getHostCard(), af.getAbCost(), af.getAbTgt()) {
            private static final long serialVersionUID = -3921131887560356006L;

            @Override
            public String getStackDescription() {
                return addTurnStackDescription(af, this);
            }

            @Override
            public boolean canPlayAI() {
                return addTurnCanPlayAI(af, this);
            }

            @Override
            public void resolve() {
                addTurnResolve(af, this);
            }

        };
        return spAddTurn;
    }

    /**
     * <p>
     * createDrawbackAddTurn.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createDrawbackAddTurn(final AbilityFactory af) {
        final SpellAbility dbAddTurn = new Ability_Sub(af.getHostCard(), af.getAbTgt()) {
            private static final long serialVersionUID = -562517287448810951L;

            @Override
            public String getStackDescription() {
                return addTurnStackDescription(af, this);
            }

            @Override
            public void resolve() {
                addTurnResolve(af, this);
            }

            @Override
            public boolean chkAIDrawback() {
                return true;
            }

            @Override
            public boolean doTrigger(final boolean mandatory) {
                return addTurnTriggerAI(af, this, mandatory);
            }

        };
        return dbAddTurn;
    }

    /**
     * <p>
     * addTurnStackDescription.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @return a {@link java.lang.String} object.
     */
    private static String addTurnStackDescription(final AbilityFactory af, final SpellAbility sa) {
        HashMap<String, String> params = af.getMapParams();
        StringBuilder sb = new StringBuilder();
        int numTurns = AbilityFactory.calculateAmount(af.getHostCard(), params.get("NumTurns"), sa);

        if (!(sa instanceof Ability_Sub)) {
            sb.append(sa.getSourceCard()).append(" - ");
        } else {
            sb.append(" ");
        }

        ArrayList<Player> tgtPlayers;

        Target tgt = af.getAbTgt();
        if (tgt != null) {
            tgtPlayers = tgt.getTargetPlayers();
        } else {
            tgtPlayers = AbilityFactory.getDefinedPlayers(sa.getSourceCard(), params.get("Defined"), sa);
        }

        for (Player player : tgtPlayers) {
            sb.append(player).append(" ");
        }

        sb.append("takes ");
        if (numTurns > 1) {
            sb.append(numTurns);
        } else {
            sb.append("an");
        }
        sb.append(" extra turn");
        if (numTurns > 1) {
            sb.append("s");
        }
        sb.append(" after this one.");

        Ability_Sub abSub = sa.getSubAbility();
        if (abSub != null) {
            sb.append(abSub.getStackDescription());
        }

        return sb.toString();
    }

    /**
     * <p>
     * addTurnCanPlayAI.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @return a boolean.
     */
    private static boolean addTurnCanPlayAI(final AbilityFactory af, final SpellAbility sa) {
        return addTurnTriggerAI(af, sa, false);
    }

    /**
     * <p>
     * addTurnTriggerAI.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @param mandatory
     *            a boolean.
     * @return a boolean.
     */
    private static boolean addTurnTriggerAI(final AbilityFactory af, final SpellAbility sa, final boolean mandatory) {

        HashMap<String, String> params = af.getMapParams();

        Target tgt = sa.getTarget();

        if (sa.getTarget() != null) {
            tgt.resetTargets();
            sa.getTarget().addTarget(AllZone.getComputerPlayer());
        } else {
            ArrayList<Player> tgtPlayers = AbilityFactory.getDefinedPlayers(sa.getSourceCard(), params.get("Defined"),
                    sa);
            for (Player p : tgtPlayers) {
                if (p.isHuman() && !mandatory) {
                    return false;
                }
            }
            // not sure if the AI should be playing with cards that give the
            // Human more turns.
        }
        return true;
    }

    /**
     * <p>
     * addTurnResolve.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     */
    private static void addTurnResolve(final AbilityFactory af, final SpellAbility sa) {
        HashMap<String, String> params = af.getMapParams();
        int numTurns = AbilityFactory.calculateAmount(af.getHostCard(), params.get("NumTurns"), sa);

        ArrayList<Player> tgtPlayers;

        Target tgt = af.getAbTgt();
        if (tgt != null) {
            tgtPlayers = tgt.getTargetPlayers();
        } else {
            tgtPlayers = AbilityFactory.getDefinedPlayers(sa.getSourceCard(), params.get("Defined"), sa);
        }

        for (Player p : tgtPlayers) {
            if (tgt == null || p.canTarget(sa)) {
                for (int i = 0; i < numTurns; i++) {
                    AllZone.getPhase().addExtraTurn(p);
                }
            }
        }
    }

} // end class AbilityFactory_Turns
