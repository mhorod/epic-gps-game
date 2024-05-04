package soturi.content;

public class GameRegistry {
    /** Cumulative xp requirement for {@code lvl} (inclusive) */
    public long getXpForLvlCumulative(int lvl) {
        if (lvl <= 1)
            return 0;
        return (long) (Math.pow(1.1, lvl - 2) * 100);
    }

    /** Xp requirement to for from {@code lvl} to {@code lvl + 1} */
    public long getXpForNextLvl(int lvl) {
        if (lvl <= 1)
            return 0;
        return getXpForLvlCumulative(lvl + 1) - getXpForLvlCumulative(lvl);
    }

    /** Convert xp to lvl */
    public int getLvlFromXp(long xp) {
        int r = 1000, l = 1;
        while (r - l > 1) {
            int m = (l + r) / 2;
            if (xp >= getXpForLvlCumulative(m))
                l = m;
            else
                r = m;
        }
        return l;
    }
}
