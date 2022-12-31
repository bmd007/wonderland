package wonderland.game.engine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InputEvent {
    private Instant timeStamp;
    private long tickNumber;
    private String playerId;
    private double positionX;
    private double positionY;
    private double angel;
}
