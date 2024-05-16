package soturi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public enum Result {
    WON, LOST;

    @JsonIgnore
    public Result flip() {
        return this == WON ? LOST : WON;
    }
}
