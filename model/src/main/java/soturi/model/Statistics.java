package soturi.model;

public record Statistics(long maxHp, long attack, long defense) {
    public Statistics mul(double by) {
        return new Statistics(
            (long) (maxHp * by),
            (long) (attack * by),
            (long) (defense * by)
        );
    }
    public Statistics add(Statistics other) {
        return new Statistics(
            maxHp + other.maxHp,
            attack + other.attack,
            defense + other.defense
        );
    }
}
