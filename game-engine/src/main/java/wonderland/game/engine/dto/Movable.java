package wonderland.game.engine.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Movable {
    private String id;
    private double positionX;
    private double positionY;
    private double velocityX;
    private double velocityY;
    private double angel;

    public static Movable randomNina() {
        return builder().velocityY(-10000).velocityX(10000).id("ninja").positionY(111.1).positionX(222.2).angel(22.2).build();
    }
}
