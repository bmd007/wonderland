# MicroservicesPlayGround
The architecture is microservices and the applied pattern is CQRS. No event sourcing as overal architecture (maybe in one of the services if needed) 
I can say the architecture is event driven and everything is an event inside the applications. From outside the appliction will recieve httpRequests. And requests lead to commands (events with a needed side effect)
There is also amqp based comminucation to outside (an Android app) (with help of rabbit mq) as push solution 

## ToDo:
    * add create docker image task for each service
    * use docker compose (all together) as some sort of deployment and orchestration tool
    * use nomad
    * add API gateway for authorization checks (resource server in OAuath2 world) (and connect it to google)
    * complete person_profile app 
    * publish logs into elastic search
    * add kibana for reading logs 
       
## Bounded contexes and Teams > ? < 
    
    
### services and their relation to kafka topics
#### generally, a command listener specifies where to send commands to. And an event producer specifies where it will send events. (Events=notification (something happenede, delta of domain), Commands=requests for something to be done)
    * messenger owns these topics: message_events, 
