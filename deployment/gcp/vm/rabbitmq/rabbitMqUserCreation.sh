#!/bin/bash

printenv

rabbitmqctl add_user rabbit-mq-web-stomp-credentials rabbit-mq-web-stomp-credentials
rabbitmqctl set_permissions -p / rabbit-mq-web-stomp-credentials ".*" ".*" ".*"
rabbitmqctl set_user_tags rabbit-mq-web-stomp-credentials management
#this user is named mqtt but it actually doesn't have anything to do with mqtt
