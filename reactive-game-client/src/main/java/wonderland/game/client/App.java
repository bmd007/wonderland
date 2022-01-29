/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package wonderland.game.client;

import com.jme3.app.SimpleApplication;
import com.jme3.collision.CollisionResults;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Flux;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.jme3.input.MouseInput.BUTTON_LEFT;

@Slf4j
public class App extends SimpleApplication {

    Box blueBox = new Box(2, 2, 2);
    Geometry blueBoxGeom = new Geometry("BlueBox", blueBox);

    Box redBox = new Box(1, 1, 1);
    Geometry redBoxGeom = new Geometry("RedBox", redBox);

    record Rocket(int x, int y){ }
    static ConcurrentLinkedQueue<Rocket> localValues = new ConcurrentLinkedQueue<>();

    private static Flux<Rocket> rockets = RSocketRequester.builder()
//            .rsocketConnector(connector -> connector.reconnect(Retry.fixedDelay(2, Duration.ofSeconds(2))))
            .tcp("localhost", 4789)
            .route("rocket")
            .data("abc")
            .retrieveFlux(String.class)
            .map(value -> value.split(":"))
            .filter(values -> values.length == 2)
            .map(values -> new Rocket(Integer.valueOf(values[0]), Integer.valueOf(values[1])))
            .doOnNext(localValues::add);
//    app.enqueue(callable);


    public static void main(String[] args) {
        rockets.subscribe();
        App app = new App();
        AppSettings settings = new AppSettings(true);
        settings.setTitle("My Awesome Game");
        app.setSettings(settings);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        Material backgroundMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        backgroundMaterial.setColor("Color", ColorRGBA.White);
        rootNode.setMaterial(backgroundMaterial);

        Material redBoxMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Material blueBoxMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        blueBoxMat.setColor("Color", ColorRGBA.Blue);
        redBoxMat.setColor("Color", ColorRGBA.Red);
        blueBoxGeom.setMaterial(blueBoxMat);
        redBoxGeom.setMaterial(redBoxMat);

        rootNode.attachChild(blueBoxGeom);
        rootNode.attachChild(redBoxGeom);

        inputManager.deleteMapping( SimpleApplication.INPUT_MAPPING_MEMORY);
        inputManager.addMapping("Pause Game", new KeyTrigger(KeyInput.KEY_P));
        inputManager.addMapping("Rotate",     new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("Left",  new KeyTrigger(KeyInput.KEY_A),
                new KeyTrigger(KeyInput.KEY_LEFT)); // A and left arrow
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D),
                new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addListener(actionListener, new String[]{"Pause Game", "Left", "Right"});

        inputManager.addMapping("pick target", new MouseButtonTrigger(BUTTON_LEFT));
        flyCam.setEnabled(false);
        inputManager.setCursorVisible(true);
        inputManager.addListener(analogListener, new String[]{"pick target"});

    }

    private ActionListener actionListener = (name, keyPressed, tpf) -> {
        System.out.println(name + ", is pressed: "+ keyPressed + " at "+ tpf);
    };

    private AnalogListener analogListener = (name, intensity, tpf) -> {
        if (name.equals("pick target")) {
            // Reset results list.
            CollisionResults results = new CollisionResults();
            // Convert screen click to 3d position
            Vector2f click2d = inputManager.getCursorPosition();
            Vector3f click3d = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 0f).clone();
            Vector3f dir = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 1f).subtractLocal(click3d).normalizeLocal();
            // Aim the ray from the clicked spot forwards.
            Ray ray = new Ray(click3d, dir);
            // Collect intersections between ray and all nodes in results list.
            rootNode.collideWith(ray, results);
            // (Print the results so we see what is going on:)
            for (int i = 0; i < results.size(); i++) {
                // (For each "hit", we know distance, impact point, geometry.)
                float dist = results.getCollision(i).getDistance();
                Vector3f pt = results.getCollision(i).getContactPoint();
                String target = results.getCollision(i).getGeometry().getName();
                System.out.println("Selection #" + i + ": " + target + " at " + pt + ", " + dist + " WU away.");
            }
            // Use the results -- we rotate the selected geometry.
            if (results.size() > 0) {
                // The closest result is the target that the player picked:
                Geometry target = results.getClosestCollision().getGeometry();
                // Here comes the action:
                if (target.getName().equals("RedBox")) {
                    target.rotate(0, -intensity, 0);
                    target.scale(0.5F, 0.5F, 0.5F);
                } else if (target.getName().equals("BlueBox")) {
                    target.scale(0.5F, 0.5F, 0.5F);
                }
            }
        } // else if ...
    };

    @Override
    public void simpleUpdate(float tpf) {
        var rocket = Optional.ofNullable(localValues.poll())
                .orElseGet(() -> new Rocket(1,5));
        blueBoxGeom.setLocalTranslation(rocket.y, 4, 1);
    }
}
