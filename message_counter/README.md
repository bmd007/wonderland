## HOW to run

### how to run with 3 instances inside docker network
 * install java11 and docker and docker-compose
 * gradlew bootJar
 * docker-compose -f docker-compose-all-together.yml build
 * docker-compose -f docker-compose-all-together.yml up
 
#### APIs to call

 * GET localhost:8222/api/counter
 * GET localhost:8222/api/counter/{counterName}
 * POST localhost:8222/api/counter/{counterName}/create
 * POST localhost:8222/api/counter/{counterName}/increase
 
 
 
### how to run with 1 instances outside docker network    
 * install java11 and docker and docker-compose
 * gradlew bootRun -Dspring.profiles.active=local
 * docker-compose -f docker-compose.yml up
 
#### APIs to call

 * GET localhost:8585/api/counter
 * GET localhost:8585/api/counter/{counterName}
 * POST localhost:8585/api/counter/{counterName}/create
 * POST localhost:8585/api/counter/{counterName}/increase
