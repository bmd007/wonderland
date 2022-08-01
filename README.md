# MicroservicesPlayGround
The architecture is microservices and the applied pattern is CQRS. 
No event sourcing as overall architecture (maybe in one of the services if needed).
A new concept somewhat similar to event sourcing is being experimented with here: 
    
    Every user interaction that goes through api-gateway results in an event (if possible) instead of an HTTP call. 
    So in terms of CRUD, only R (reads) are potential HTTP requests going out of API-gateway to core services.
    Anything else is an event like: dancer1HasLikedDancer2, ... . 
    This idea relies on the fact that Kafka topics can act as a durable, avaialbe, scalable, distrubted and ... database.

Toward outside:
 - There is AMQP based communication to outside (an Android app) (with help of rabbit mq) as push solution
 - RSocket communication from API-gateway to the flutter app.

## ToDo:
    * what if pic is empty in firebase
    * SSL all over rabbit mq
    * ssl API-gateway for RSocket
    * search among close by dancers within a raduis specified by the user
    * create chat-history kafka streams Map<ThisDancerName, Map<ChateeName, List<Message>> and back chatBoxWidget with it!
    * refill dance partner avaialble option automatically when no more left to choose from
    * rename wonder-matcher to dance-partner-finder or dance-partner-matcher .... names in the match making stack are not mature enough!
    * refactor Consumed/Produced management in kafka stream applications. Use Kafka classes rather than yours. 
    * upgrade elastic search stack to 7 and higher
    * add authorization/authentication checks (resource server in OAuath2 world) (and connect it to google) in API gateway 
    * publish logs into elastic search as application_log index
 	* integrate nomad (use information from master thesis)
