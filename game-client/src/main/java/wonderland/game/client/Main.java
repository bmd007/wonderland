package wonderland.game.client;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import io.netty.util.internal.shaded.org.jctools.queues.atomic.MpscUnboundedAtomicArrayQueue;
import io.rsocket.internal.jctools.queues.MpscUnboundedArrayQueue;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;

public class Main extends SimpleApplication {

    Box b = new Box(1, 1, 1);
    Geometry geom = new Geometry("Box", b);
    static Flux<Integer> values =  Flux.interval(Duration.ofNanos(200L))
            .map(Long::intValue)
            .map(value -> (value % 2) + 1 )
            .map(value -> value*2);
    static ConcurrentLinkedQueue<Integer> localValues = new ConcurrentLinkedQueue<Integer>();


    public static void main(String[] args) {
        Main app = new Main();
        AppSettings settings = new AppSettings(true);
        settings.setTitle("My Awesome Game");
        app.setSettings(settings);
        app.start();
        values.subscribe(localValues::add);
    }

    @Override
    public void simpleInitApp() {
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        geom.setMaterial(mat);
        rootNode.attachChild(geom);
    }

    @Override
    public void simpleUpdate(float tpf) {
        geom.setLocalTranslation(localValues.poll(), 2, 5);
    }
}
