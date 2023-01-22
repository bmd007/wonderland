package wonderland.game.engine.domain;


import lombok.Data;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;
import wonderland.game.engine.dto.Movable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.requireNonNull;


@Data
public class PhysicalComponent {
    public static final World WORLD = new World(new Vec2(0, -10));
    static {
        WORLD.drawDebugData();
    }
    private final String id;
    private final String name;
    public BodyDef bodyDefinition = new BodyDef();
    public FixtureDef fixtureDefinition = new FixtureDef();
    public Body body;
    public Fixture fixture;
    private PhysicalComponent parent;
    protected Map<String, PhysicalComponent> children = new ConcurrentHashMap<>();

    public PhysicalComponent(String id, String name) {
        this.id = id;
        this.name = name;
        bodyDefinition.userData = this;
    }

    public PhysicalComponent() {
        this.id = this.name = UUID.randomUUID().toString();
        bodyDefinition.userData = this;
    }

    void add(PhysicalComponent component) {
        component.addToParent(this);
    }

    void addToParent(PhysicalComponent parent) {
        this.parent = parent;
        this.parent.children.put(this.id, this);
        addToWorld();
    }

    private void addToWorld() {
        requireNonNull(bodyDefinition);
        requireNonNull(fixtureDefinition);
        body = WORLD.createBody(bodyDefinition);
        fixture = body.createFixture(fixtureDefinition);
    }
}
