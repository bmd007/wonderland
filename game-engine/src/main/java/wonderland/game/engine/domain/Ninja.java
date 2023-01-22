package wonderland.game.engine.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.BodyType;
import wonderland.game.engine.dto.JoystickInputEvent;

@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Ninja extends PhysicalComponent {
    private boolean landedSinceLastElevation = false;
    private boolean lookingTowardRight = true;
    final float speed = 20;

    public Ninja(Vec2 initialPosition, float initialAngle) {
        super("ninja", "ninja");
        final CircleShape shape = new CircleShape();
        shape.m_radius = 3;
        fixtureDefinition.shape = shape;
        fixtureDefinition.density = 2;
        fixtureDefinition.restitution = 0.1F;
        fixtureDefinition.friction = 2;
        bodyDefinition.position = initialPosition;
        bodyDefinition.angle = initialAngle;
        bodyDefinition.type = BodyType.DYNAMIC;
        bodyDefinition.fixedRotation = true;
    }

    public void applyJoystickInputEvent(JoystickInputEvent joystickInputEvent) {
        log.info("ninja landed {}, velocity {}, transform {}", landedSinceLastElevation, body.m_linearVelocity, body.getTransform());
        landedSinceLastElevation = body.m_linearVelocity.y == 0;
        if (joystickInputEvent.direction().equals("down")) {
            if (landedSinceLastElevation) {
                body.m_linearVelocity.x = 0;
            }
        } else if (joystickInputEvent.direction().equals("downLeft") || joystickInputEvent.direction().equals("left")) {
//            if (lookingTowardRight) {
//                component.flipHorizontally();
//            }
            lookingTowardRight = false;
            if (body.m_linearVelocity.y == 0) {
                body.m_linearVelocity.x = -speed;
            }
        } else if (joystickInputEvent.direction().equals("downRight") || joystickInputEvent.direction().equals("right")) {
//            if (!lookingTowardRight) {
//                component.flipHorizontally();
//            }
            lookingTowardRight = true;
            if (body.m_linearVelocity.y == 0) {
                body.m_linearVelocity.x = speed;
            }
        } else if (joystickInputEvent.direction().equals("up") && landedSinceLastElevation) {
            landedSinceLastElevation = false;
            body.applyLinearImpulse(new Vec2(0, -1000), body.getWorldCenter());
        } else if (joystickInputEvent.direction().equals("upLeft") && landedSinceLastElevation) {
//            if (lookingTowardRight) {
//                component.flipHorizontally();
//            }
            lookingTowardRight = false;
            landedSinceLastElevation = false;
            body.m_linearVelocity.x = 0;
            body.applyLinearImpulse(new Vec2(joystickInputEvent.relativeDeltaX() * 1000f, joystickInputEvent.relativeDeltaY() * 1000f), body.getWorldCenter());
        } else if (joystickInputEvent.direction().equals("upRight") && landedSinceLastElevation) {
//            if (!lookingTowardRight) {
//                component.flipHorizontally();
//            }
            lookingTowardRight = true;
            body.m_linearVelocity.x = 0;
            landedSinceLastElevation = false;
            body.applyLinearImpulse(new Vec2(joystickInputEvent.relativeDeltaX() * 1000f, joystickInputEvent.relativeDeltaY() * 1000f), body.getWorldCenter());
        }
    }
}
