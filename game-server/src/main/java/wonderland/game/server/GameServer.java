package wonderland.game.server;

import com.jme3.app.SimpleApplication;
import com.jme3.system.JmeContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
public final class GameServer extends SimpleApplication {

    public GameServer() {
        this.start(JmeContext.Type.Headless);
    }

    @Override
    public void simpleInitApp() {

    }
}
