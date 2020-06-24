# MicroservicesPlayGround
The architecture is microservices and the applied pattern is CQRS. No event sourcing as overal architecture (maybe in one of the services if needed) 
I can say the architecture is event driven and everything is an event inside the applications. From outside the appliction will recieve httpRequests. And requests lead to commands (events with a needed side effect)
There is also amqp based comminucation to outside (an Android app) (with help of rabbit mq) as push solution 

## ToDo:
    * upgrade elastic search stack to 7 and higher
    * make health check of services coupled to their connection to their dbs (is it a good idea?)
    * add health checks for each service into their docker-compose (because of previous point, it should be alwalys GET:http://servicesname/actuactor/health)
    * make hcl files useable without need to change them each time
    * add API gateway for authorization checks (resource server in OAuath2 world) (and connect it to google)
    * complete person_profile and ui apps
    * publish logs into elastic search as application_log index 
    * add an application that is based on DDD or try to apply DDDs concepts to the whole system (Its alraedy somehow there but not completely) 
       
## Bounded contexts and Teams > ? < 
    
### services and their relation to kafka topics
#### generally, a command listener specifies where to send commands to. An event producer specifies where it will send events. (Events=notification (something happenede, delta of domain), Commands=requests for something to be done)
    * messenger owns these topics: message_events, 
    * message_counter owns thses topics: event_log (internally used for event sourcing)
