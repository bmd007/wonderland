package wonderland.game.engine.domain;

import org.jbox2d.common.Vec2;
import wonderland.game.engine.dto.JoystickInputEvent;
import wonderland.game.engine.dto.Movable;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

public class Level extends PhysicalComponent {
    public static final ConcurrentLinkedQueue<JoystickInputEvent> JOYSTICK_EVENTS = new ConcurrentLinkedQueue<>();
    private static final Vec2 SIZE = new Vec2(1366, 768);

    private final String name;

    public static Level level1() {
        return new Level("GodBless");
    }

    public Level(float farRight, float farBottom, String name, String id) {
        super(id);
        this.name = name;
        setupWallsAsLevelBoundaries(farRight, farBottom, 5);
    }

    public Level(String name) {
        super();
        this.name = name;
        var right = SIZE.x + 100;
        var bottom = SIZE.y;
        setupWallsAsLevelBoundaries(right, bottom, 5);
        add(new Ninja(new Vec2(SIZE.x / 2, SIZE.y / 2), 0));
    }

    public void setupWallsAsLevelBoundaries(float farRight, float farBottom, float margin) {
        final Vec2 topLeft = new Vec2(0, 0).add(new Vec2(margin, margin));
        final Vec2 topRight = new Vec2(farRight, 0).add(new Vec2(-margin, margin));
        final Vec2 bottomLeft = new Vec2(0, farBottom).add(new Vec2(margin, -margin));
        final Vec2 bottomRight = new Vec2(farRight, farBottom).add(new Vec2(-margin, -margin));
        add(new Wall(topLeft, topRight));
        add(new Wall(topRight, bottomRight));
        add(new Wall(bottomLeft, topLeft));
        add(new Wall(bottomRight, bottomLeft));
    }

    private Level applyInput(JoystickInputEvent joystickInputEvent) {
        ((Ninja)children.get("ninja")).applyJoystickInputEvent(joystickInputEvent);
        return this;
    }

    //synchronized?
    public Level update(int subTick) {
        Optional.ofNullable(JOYSTICK_EVENTS.poll()).ifPresent(this::applyInput);
        return this;
    }

    public Stream<Movable> getMovables() {
        return children.values()
                .stream()
                .filter(component -> component.getId().equals("ninja"))
                .map(PhysicalComponent::toMovable);
    }
}
