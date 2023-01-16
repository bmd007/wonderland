package wonderland.game.engine.domain;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;
import reactor.core.publisher.Flux;
import wonderland.game.engine.domain.physics.SimulationComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Level extends World implements SimulationComponent {

    private final static Vec2 SIZE = new Vec2(1366, 768);
    ConcurrentHashMap<String, BodyComponent> bodyComponents = new ConcurrentHashMap<>();

    public Level(float farRight, float farBottom) {
        super(new Vec2(0, -10));
        setupWallsAsLevelBoundaries(farRight, farBottom, 5);
    }

    public Level() {
        super(new Vec2(0, -10));
        var right = SIZE.x + 100;
        var bottom = SIZE.y;
        setupWallsAsLevelBoundaries(right, bottom, 5);
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

    public void add(BodyComponent bodyComponent){
        Body body = createBody(bodyComponent.getBodyDefinition());
        Fixture fixture = body.createFixture(bodyComponent.getFixtureDefinition());
        bodyComponent.setBody(body);
        bodyComponent.setFixture(fixture);
    }

    @Override
    public void create() {


        Flux.fromIterable(bodyComponents.values())
                .doOnNext(BodyComponent::create)
                .subscribe();
    }

    @Override
    public void simulate() {


        Flux.fromIterable(bodyComponents.values())
                .doOnNext(BodyComponent::simulate)
                .subscribe();
    }
}

