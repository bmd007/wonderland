package wonderland.game.engine.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import wonderland.game.engine.domain.physics.SimulationComponent;

@Data
@AllArgsConstructor
public class BodyComponent implements SimulationComponent {
    BodyDef bodyDefinition;
    FixtureDef fixtureDefinition;
    Body body;
    Fixture fixture;

    @Override
    public void create() {

    }

    @Override
    public void simulate() {

    }
}
