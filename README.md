# MicroservicesPlayGround
The test goal is to create a system keeping track of individual interests

I tried to use reactive messaging and reactive programming where ever possible in this project

This project composed of Events, messages, webflux based rest webservices, websockets, reactive database connections and ...

The architecture is microservices and the applied pattern is CQRS. But no internal event sourcing is applied. 
I can say the architecture is event driven and everything is an event inside the application. From outside the appliction will recieve httpRequests. And requests lead to commands )events with a needed side effect)

## ToDo:
    * use spring new load balanced http client
    * Create perosn and user management services
    * Add a servlet based web-socket backend as notification manager ?? 
    (Web flux web socket does not support broker and user specific messages)
    * Add a coin system
    * Add a follow system using a graph database
    * Start to use JWT as IdToken(OpenId) and as AccessToken
    * Make the gateway check access token and authorization of requests.
    Authurization server should provide signed JWTs.
    * if possible separate login microservice from authorization microservice
    * Make login microservice able to let login from google
    * Create a register microservice and connect it to google
    * Use By reference JWT (IdTokens not accessTokens) outside the network. Create a JWT translator microservice on the edge of inner network.
    (https://www.youtube.com/watch?v=BdKmZ7mPNns)
    * use spring cloud reactive streams (which is based on spring integration + binders for things like kafka and rabbitmq)
    in some processor services and also use reactiveKafka in close to user services
    * see how to use higher abstractions above spring cloud streams (like stream data flow and function)
    * consider kafka as eventStore and lean to event sourcing
    
    
## Bounded contexes and Teams > ? <
    * Person (person-command, user-management, auth-server(authentication), gateway(authorization and load balance? and gateway), person-ui(crud forms) ?) 
    * Interest (person-interests, 
    * Video (video-???)
    * Money (noobchain, ?????)
    * R&D (EducationalPrototypes, ?)
    ?

### services and their relation to kafka topics
#### generally a command listener says to send commands to were and an event producer says that were it will send events. (Events=notification, Commands=requests for something to be done)
    * person-interest owns this topics: add-interest-command
