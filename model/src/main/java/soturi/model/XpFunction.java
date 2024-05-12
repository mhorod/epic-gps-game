package soturi.model;

public record XpFunction(double add, double mul, double base) {
    public double eval(double at) {
        return (add + mul * at) * Math.pow(base, at);
    }
}
