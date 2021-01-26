package wonderland.reactive.game.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.EventListener;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SynchronousSink;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.BiFunction;

@SpringBootApplication
public class GameEngineApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameEngineApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(GameEngineApplication.class, args);
    }

    private final static Integer NUMBER_OF_TICKS_PER_SECOND = 2;

    //todo:
	//microservice match-maker:
		//create a match making controller that is backed by a SET of players as activeSession
		//wrap active sessions a database table
		//when a session is ready to lunch: run a game engine process and let players in the session know about it

	//microservice game engine (this app):
	//-create a thread safe queue of user input events as userInputEvents
	//-create a bi-directional r-socket connection with players (initiated by players) with Flux of 'user input event's
	// as input and Flux of game state objects (game state aggregate root) as output.
	//use this connection to update userInputEvents and after each tick

    BiFunction<Integer, SynchronousSink<Integer>, Integer> tickGenerator = (tickNumber, tickSink) -> {
        tickOperation();
        tickSink.next(tickNumber);
        if (tickNumber >= NUMBER_OF_TICKS_PER_SECOND) {
            tickSink.complete();
        }
        return tickNumber + 1;
    };

    @EventListener(org.springframework.context.event.ContextRefreshedEvent.class)
    public void setup() {
        Flux.interval(Duration.ofSeconds(10L))
                .doOnNext(logicalTime -> {
                    LOGGER.info("******\n on thread {} Started a tick sequence at: {}",
                            Thread.currentThread().getName(),
                            LocalDateTime.now().toLocalTime());
                    Flux.generate(() -> 1, tickGenerator)
                            .subscribeOn(Schedulers.newParallel("scheduler-game-engine", 2))//with this:
                            //each tick do NOT have its own thread, so the 20000 milli sec delay inside each tick operation
                            //(imitating IO delay) is effective: tick1--20sec--tick2--20sec--tick3.
                            //but each tick sequence ("ticks per second") will have its own thread
                            //so tick sequences can run in parallel

                            //Maybe tick/sec actually means no parallelism???

                            //How to change state of UI (games objects value):
                            //tick operations call synchronized setter methods on game objects
							//question is where to keep and how to access UI objects. UIThread.getState().getX().setY()?
									//how to apply DDD to game state design and manipulation??

                            //How to actually update UI it self (on clients):
							// having a UIThread.updateUI method that reads UI objects all together and
							// renders next frame accordingly
							//UI thread calls it every X milli secs

							//for a game this works but for an app with not much of UI changes, this is too much
							// processing. Observer style patterns like MVVM would suffice perfectly for such apps.
							.doOnNext(state -> LOGGER.info("on thread {} next tick happened at {} \n---------",
                                    Thread.currentThread().getName(),
                                    LocalDateTime.now().toLocalTime()))
                            .count()
                            .subscribe(numberOfTicks -> LOGGER.info("on thread {}, {} ticks happened\n---------",
                                    Thread.currentThread().getName(),
                                    numberOfTicks));
                })
                .subscribe();
    }

    private void tickOperation() {
        try {
        	//1-read next command from user input queue (user input events queue)
			//2-calculate new game state accordingly
			//3-save it across game objects (using synchronized methods only if parallelism is applied between tick
			// sequences)
			//4-send the game state to clients asynchronously
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
