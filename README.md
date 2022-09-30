# Wonderland https://wonderland-007.web.app

### Scenario:
After login, there will be plenty of activity (atm only dancing) to choose from.
Choosing an activity means you are looking for a partner around yourself to carry the activity on together.
Once two people has shown mutual interest toward each other, there are each other's match and can chat.

Also, each user has a profile containing a name plus a photo.
The name is the user's gmail and photo can be selected in the profile edit page.

## A Highly scalable geospatial matchmaking system
implemented using Stream processing and reactive system ideas

### Stack:
 * Java
 * Spring family
 * Kafka and KafkaStreams
 * H2, Elasticsearch, Neo4j
 * Flutter
 * RSocket, AMQP, STOMP, HTTP
 * Firebase, GCP

### Architecture
The architecture is event driven microservices and the applied pattern is CQRS. 
A new concept somewhat similar to event sourcing is being experimented with here:  

    Every user interaction that goes through api-gateway results in an event (if possible) instead of an HTTP call. 
    So in terms of CRUD, only R (reads) are potential HTTP requests going out of API-gateway to core services.
    Anything else is an event like: dancer1HasLikedDancer2, ... . 
    This idea relies on the fact that Kafka topics can act as a durable, avaialbe, scalable, distrubted and ... database.

Toward outside:
 - There is AMQP based communication to RabbitMq and STOMP connection to the APP, as push solution for chatting
 - RSocket communication between API-gateway and the app.
 - HTTPs communication between API-gateway and the app.

## ToDo:
    * create chat-history kafka streams Map<ThisDancerName, Map<ChateeName, List<Message>> and back chatBoxWidget with it!
    * publish logs into elastic search as application_log index
    * rename wonder-matcher to dance-partner-finder or dance-partner-matcher .... names in the match making stack are not mature enough!
    * refactor Consumed/Produced management in kafka stream applications. Use Kafka classes rather than yours. 
    * upgrade elastic search stack to 7 and higher
    * add authorization/authentication checks (resource server in OAuath2 world) (and connect it to google) in API gateway 
 	* integrate nomad (use information from master thesis)
