package wonderland.game.engine.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.jbox2d.common.Vec2;
import wonderland.game.engine.dto.JoystickInputEvent;
import wonderland.game.engine.dto.Movable;

import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Level extends PhysicalComponent {
    public static final Queue<JoystickInputEvent> JOYSTICK_EVENTS = new ConcurrentLinkedQueue<>();
    private static final Vec2 SIZE = new Vec2(1366, 768);

    private float farRight;
    private float farBottom;
    private float margin = 5;
    private Ninja ninja = new Ninja(new Vec2(SIZE.x / 2, SIZE.y / 2), 0);

    public static Level level1() {
        return new Level("GodBless");
    }

    public Level(float farRight, float farBottom, String name, String id) {
        super(id, name);
        this.farRight = farRight;
        this.farBottom = farBottom;
        setupWallsAsLevelBoundaries();
    }

    public Level(String name) {
        super(UUID.randomUUID().toString(), name);
        this.farRight = SIZE.x + 100;
        this.farBottom = SIZE.y;
        setupWallsAsLevelBoundaries();
        add(ninja);
    }

    public void setupWallsAsLevelBoundaries() {
        final Vec2 topLeft = new Vec2(0, 0).add(new Vec2(margin, margin));
        final Vec2 topRight = new Vec2(farRight, 0).add(new Vec2(-margin, margin));
        final Vec2 bottomLeft = new Vec2(0, farBottom).add(new Vec2(margin, -margin));
        final Vec2 bottomRight = new Vec2(farRight, farBottom).add(new Vec2(-margin, -margin));
        add(new Wall(topLeft, topRight));
        add(new Wall(topRight, bottomRight));
        add(new Wall(bottomLeft, topLeft));
        add(new Wall(bottomRight, bottomLeft));
    }

    private void applyInput(JoystickInputEvent joystickInputEvent) {
        ninja.applyJoystickInputEvent(joystickInputEvent);
    }

    //synchronized?
    public void update(Double delta) {
        Optional.ofNullable(JOYSTICK_EVENTS.poll()).ifPresent(this::applyInput);
        WORLD.step(delta.floatValue(), 10, 10);
        log.info("subTick {} :: ninja: {}:{}", delta, ninja.body.getPosition(), ninja.body.m_linearVelocity);
    }
}
