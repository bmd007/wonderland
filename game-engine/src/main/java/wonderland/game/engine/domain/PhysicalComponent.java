package wonderland.game.engine.domain;


import lombok.Data;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;
import reactor.core.publisher.Flux;
import wonderland.game.engine.dto.Movable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.requireNonNull;


@Data
public class PhysicalComponent {
    public static World WORLD = new World(new Vec2(0, -10));

    private final String id;
    public BodyDef bodyDefinition = new BodyDef();
    public FixtureDef fixtureDefinition = new FixtureDef();
    public Body body;
    public Fixture fixture;
    private PhysicalComponent parent;
    protected Map<String, PhysicalComponent> children = new ConcurrentHashMap<>();

    public PhysicalComponent(String id) {
        this.id = id;
        bodyDefinition.userData = this;
    }

    public PhysicalComponent() {
        this.id = UUID.randomUUID().toString();
        bodyDefinition.userData = this;
    }

//    public void setParent(PhysicalComponent newParent) {
//        if (newParent == parent) {
//            return;
//        } else if (newParent == null) {
//            removeFromParent();
//        } else if (parent == null) {
//            addToParent(newParent);
//        } else {
////            newParent.lifecycle._adoption.add(this);
//        }
//    }

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
//
//    public void onCreate() {
//        Flux.fromIterable(children.values())
//                .doOnNext(PhysicalComponent::onCreate)
//                .subscribe();
//    }
//
//    public void onUpdate() {
//        Flux.fromIterable(children.values())
//                .doOnNext(PhysicalComponent::onUpdate)
//                .subscribe();
//    }
//
//    public void onRemove() {
//        Flux.fromIterable(children.values())
//                .doOnNext(PhysicalComponent::onRemove)
//                .subscribe();
//    }

    public Movable toMovable(){
        return new Movable(id,  body.m_linearVelocity.x, body.m_linearVelocity.y, body.m_angularVelocity);
    }
}
