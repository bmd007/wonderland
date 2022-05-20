### WONDER MATCHER

#### NOTE: here we grouped all the activities into wonders, however each activity should have each own completely separate stack.
    So we could have `bowling-mate-seeker` here for example.


## TODO
 `userA` is just a placeholder for a/one user below in each line. The userA in different lines are not necessarily the same.

 1. best way/place to publish SeekerWonderingUpdates. RSocket from app fire and forgets to X. X publishes updates in Kafka in turn. What is X ?!
 2. customer app should get a pageable connection to WONDER_SEEKERS table through RSocket
 3. userA disliked userB event (fire and forger rsocket)
 4. userA liked userB event (fire and forger rsocket)
 5. userA liked set of users so far (KafkaStreams simple key value state store)
 6. userA is liked by userB event
 7. join 6 to 7 and compute matches
 8. userA and userB matched event
 9. userB and userA matched event
 10. userA matched with set of users (kafkaSteams simple key value state store)
 11. userA unmatched with userB
 12. userB got unmatched by userA
 13. update matched state for userA
 14. ....