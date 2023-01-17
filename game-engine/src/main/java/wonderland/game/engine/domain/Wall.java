package wonderland.game.engine.domain;

import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.BodyType;

public class Wall extends PhysicalComponent {
    public Wall(Vec2 topLeft, Vec2 topRight) {
        super();
        final EdgeShape shape = new EdgeShape();
        shape.set(topLeft, topRight);
        fixtureDefinition.shape = shape;
        fixtureDefinition.restitution = 0.1F;
        fixtureDefinition.friction = 0.3F;
        bodyDefinition.position = new Vec2(0, 0);
        bodyDefinition.type = BodyType.STATIC;
    }
}
