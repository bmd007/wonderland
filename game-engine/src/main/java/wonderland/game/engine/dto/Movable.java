package wonderland.game.engine.dto;

public record Movable(
        String id,
        double initialPositionX,
        double initialPositionY,
        double initialAngel,
        double linearVelocityX,
        double linearVelocityY,
        double angularVelocity) {
}
