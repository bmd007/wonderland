package wonderland.game.engine.domain;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;

public class Game {

    private World world = new World(new Vec2(0, -10));

    public Game(){
        var size = new Vec2(360, 880);
        var bottom = size.y;
        var right = size.x + 100;
        final Vec2 topLeft = new Vec2(0,0);
        final Vec2 bottomLeft = new Vec2(0, bottom);
        final Vec2 bottomRight = new Vec2(right, bottom);
        final Vec2 topRight = new Vec2(right, 0);
        world.cre
        world.add(new Wall(topLeft, topRight));
        world.add(Wall(topRight, bottomRight));
        world.add(Wall(bottomLeft, topLeft));
        world.add(Wall(bottomRight, bottomLeft));
    }
}

