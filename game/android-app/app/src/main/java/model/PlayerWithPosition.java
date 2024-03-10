package model;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
public record PlayerWithPosition(Player player, Position position) { }
