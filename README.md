# MicroservicesPlayGround
The architecture is microservices and the applied pattern is CQRS. No event sourcing as overal architecture (maybe in one of the services if needed) 
I can say the architecture is event driven and everything is an event inside the applications. From outside the appliction will recieve httpRequests. And requests lead to commands (events with a needed side effect)

## ToDo:
    * add create docker image task for each service
    * use some sort of container orchestration tool and somehow automate the process of deploying in that tool
    * Add a notification manager service that communicates with activeMQ and (using help of activeMQ web socket) sends messages to exact users. The service should be able to call activeMQ using amqp
    * Add a coin system integration
    * Add a follow system using a graph database
    * add API gateway for authorization checks (resource server in OAuath2 world)
       
## Bounded contexes and Teams > ? < 
    
    
### services and their relation to kafka topics
#### generally a command listener says to send commands to were and an event producer says that were it will send events. (Events=notification, Commands=requests for something to be done)
    * person-interest owns this topics: add-interest-command
