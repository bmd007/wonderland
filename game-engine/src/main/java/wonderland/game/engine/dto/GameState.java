package wonderland.game.engine.dto;

import lombok.Data;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

//immutable todo?
@Data
public class GameState {
    private long tickNumber;
    private Instant timeStamp;
    Set<Movable> movables = new HashSet<>();

    public static final GameState createState(long tickNumber) {
        GameState gameState = new GameState();
        gameState.tickNumber = tickNumber;
        gameState.timeStamp = Instant.now();
        return gameState;
    }

    public GameState addNinjaInitially(){
        Movable ninja = Movable.builder()
                .angel(0)
                .id("ninja")
                .positionX(10)
                .positionY(10)
                .velocityX(111)
                .velocityY(-111)
                .build();
        movables.add(ninja);
        return this;
    }

    public GameState addMovable(Movable movable) {
        movables.add(movable);
        return this;
    }
}
