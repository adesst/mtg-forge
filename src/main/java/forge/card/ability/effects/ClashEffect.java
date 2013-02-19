package forge.card.ability.effects;

import java.util.HashMap;

import forge.Singletons;
import forge.card.ability.AbilityFactory;
import forge.card.ability.AbilityUtils;
import forge.card.ability.SpellAbilityEffect;
import forge.card.spellability.AbilitySub;
import forge.card.spellability.SpellAbility;
import forge.card.trigger.TriggerType;

public class ClashEffect extends SpellAbilityEffect {

    /* (non-Javadoc)
     * @see forge.card.abilityfactory.SpellEffect#getStackDescription(java.util.Map, forge.card.spellability.SpellAbility)
     */
    @Override
    protected String getStackDescription(SpellAbility sa) {
        return sa.getSourceCard().getName() + " - Clash with an opponent.";
    }

    /* (non-Javadoc)
     * @see forge.card.abilityfactory.SpellEffect#resolve(java.util.Map, forge.card.spellability.SpellAbility)
     */
    @Override
    public void resolve(SpellAbility sa) {
        final boolean victory = sa.getSourceCard().getController().clashWithOpponent(sa.getSourceCard());

        // Run triggers
        final HashMap<String, Object> runParams = new HashMap<String, Object>();
        runParams.put("Player", sa.getSourceCard().getController());

        if (victory) {
            if (sa.hasParam("WinSubAbility")) {
                final SpellAbility win = AbilityFactory.getAbility(
                        sa.getSourceCard().getSVar(sa.getParam("WinSubAbility")), sa.getSourceCard());
                win.setActivatingPlayer(sa.getSourceCard().getController());
                ((AbilitySub) win).setParent(sa);

                AbilityUtils.resolve(win, false);
            }
            runParams.put("Won", "True");
        } else {
            if (sa.hasParam("OtherwiseSubAbility")) {
                final SpellAbility otherwise = AbilityFactory.getAbility(
                        sa.getSourceCard().getSVar(sa.getParam("OtherwiseSubAbility")), sa.getSourceCard());
                otherwise.setActivatingPlayer(sa.getSourceCard().getController());
                ((AbilitySub) otherwise).setParent(sa);

                AbilityUtils.resolve(otherwise, false);
            }
            runParams.put("Won", "False");
        }

        Singletons.getModel().getGame().getTriggerHandler().runTrigger(TriggerType.Clashed, runParams, false);
    }

}
