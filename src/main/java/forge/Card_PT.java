package forge;

/**
 * <p>Card_Color class.</p>
 *
 * @author Forge
 * @version $Id$
 */
public class Card_PT {

    private int power;
    private int toughness;
    private long timeStamp = 0;

    /**
     * <p>getTimestamp.</p>
     *
     * @return a long.
     */
    public long getTimestamp() {
        return timeStamp;
    }

    /**
     * <p>Constructor for Card_PT.</p>
     *
     * @param newPower     a int.
     * @param newToughness a int.
     * @param stamp        a long.
     */
    Card_PT(int newPower, int newToughness, long stamp) {
        power = newPower;
        toughness = newToughness;
        timeStamp = stamp;
    }

    public int getPower() {
        return power;
    }

    public int getToughness() {
        return toughness;
    }

    /**
     * <p>equals.</p>
     *
     * @param newPower     a int.
     * @param newToughness a int.
     * @param stamp        a long.
     * @return a boolean.
     */
    public boolean equals(int newPower, int newToughness, long stamp) {
        return timeStamp == stamp && power == newPower && toughness == newToughness;
    }
}
