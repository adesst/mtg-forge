package forge.deck.generate;

import forge.Singletons;
import forge.card.CardDb;
import forge.deck.CardPool;
import forge.deck.generation.DeckGenerator3Color;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by IntelliJ IDEA. User: dhudson
 */
@Test(groups = { "UnitTest" }, timeOut = 1000, enabled = false)
public class Generate3ColorDeckTest {

    /**
     * Generate3 color deck test1.
     */
    @Test(timeOut = 1000, enabled = false)
    public void generate3ColorDeckTest1() {
        CardDb cardDb = Singletons.getMagicDb().getCommonCards();
        final DeckGenerator3Color gen = new DeckGenerator3Color(cardDb, "white", "blue", "black");
        final CardPool cardList = gen.getDeck(60, false);
        Assert.assertNotNull(cardList);
    }
}
