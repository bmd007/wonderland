# MicroservicesPlayGround
This repo contain a code of some microservices, making a playground for practice purposes

The test goal is to create a system keeping track of individual interests

I tried to use reactive messaging and reactive programming where ever possible in this project

This project composed of Events, messages, webflux based rest webservices, websockets, reactive database connections and ...


#ToDo:
    - Add a servlet based web-socket backend as notification manager
    (Web flux web socket does not support broker and user specific messages)
    - Add a coin system
    - Add a follow system using a graph database
    - Start to use JWT as IdToken(OpenId) and as AccessToken
    - Make each microservice check access token and authorization of requests.
    Authurization server should provide signed JWTs.
    - if possible separate login microservice from authorization microservice
    - Make login microservice able to let login from google
    - Create a register microservice and connect it to google
    - Use By reference JWT (IdTokens not accessTokens) outside the network. Create a JWT translator microservice on the edge of inner network.
    (https://www.youtube.com/watch?v=BdKmZ7mPNns)