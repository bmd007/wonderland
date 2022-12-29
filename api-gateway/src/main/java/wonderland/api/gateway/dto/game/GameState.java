package wonderland.api.gateway.dto.game;

import lombok.Data;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Data
public class GameState {
    private long tickNumber;
    private Instant timeStamp;
    Set<Movable> movables = new HashSet<>();

    static final GameState createState(long tickNumber) {
        GameState gameState = new GameState();
        gameState.tickNumber = tickNumber;
        gameState.timeStamp = Instant.now();
        return gameState;
    }

    GameState addMovable(Movable movable) {
        movables.add(movable);
        return this;
    }
}
