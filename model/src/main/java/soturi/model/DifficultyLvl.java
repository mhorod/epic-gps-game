package soturi.model;

/**
 * Every city (with {@code population >= config.cityThreshold}) has an associated circular ring
 * with tiles of respective difficulty (given by {@code minLvl, maxLvl}).
 * <p>
 * Sizes of such circles are scaled logarithmically and nominal values correspond to city of size 1M
 * e.g. city of size 1K would have rings of radius two times smaller.
 * <p>
 * The final entry is the default.
 */

public record DifficultyLvl(double radiusInMeters, int minLvl, int maxLvl) { }
