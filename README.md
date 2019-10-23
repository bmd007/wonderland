# MicroservicesPlayGround
The test goal is to create a system keeping track of individual interests

I tried to use reactive messaging and reactive programming where ever possible in this project

This project composed of Events, messages, webflux based rest webservices, websockets, reactive database connections and ...

The architecture is microservices and the applied pattern is CQRS. But no internal event sourcing is applied. 
I can say the architecture is event driven and everything is an event inside the application. From outside the appliction will recieve httpRequests. And requests lead to commands )events with a needed side effect)

## ToDo:
    * use spring new load balanced http client
    * add create docker image task for each service
    * use some sort of container orchestration tool and somehow automate the process of deploying in that tool
    * Create perosn and user (full featured authentication/authorization(JWT signed tokens)) management services
    * Add a notification manager service that communicates with rabbitMQ and (using help of activeMQ web socket) sends messages to exact users. The service should be able to call activeMQ using amqp)
    * Add a coin system
    * Add a follow system using a graph database
    * add login gateway for authenticaion/idenfication checks (resource server in OAuath2 world)
    * add API gateway for authorization checks (resource server in OAuath2 world)
    * Make login gateway able to let login/register from google
    (https://www.youtube.com/watch?v=BdKmZ7mPNns)
    * use spring cloud reactive streams (which is based on spring integration + binders for things like kafka and rabbitmq)
    in some processor services and also use reactiveKafka in close to user services
    * see how to use higher abstractions above spring cloud streams (like stream data flow and function)
    * redesign the "business" and follow CQRS and newly learnt lessons from kafka and then re shape domains 
    * heavily apply event-driven 
    * when spring security core 5.3 published with new authorization server, migrate to it

	    
    
## Bounded contexes and Teams > ? <
    * Person (person-command, user-management, auth-server(authentication), gateway(authorization and load balance? and gateway), person-ui(crud forms) ?) 
    * Interest (person-interests, 
    * Video (video-???)
    ?

### services and their relation to kafka topics
#### generally a command listener says to send commands to were and an event producer says that were it will send events. (Events=notification, Commands=requests for something to be done)
    * person-interest owns this topics: add-interest-command


###USE 
 * https://docs.spring.io/spring-security-oauth2-boot/docs/current-SNAPSHOT/reference/htmlsingle/#boot-features-security-oauth2-authorization-server
 * https://docs.spring.io/spring-security/site/docs/current/reference/html5/#webflux-oauth2
