FROM rabbitmq:management

RUN apt-get update && apt-get -y upgrade
RUN apt-get --yes --force-yes install nano
RUN apt-get --yes --force-yes install curl

COPY rabbitmq.conf /etc/rabbitmq/rabbitmq.conf
COPY cert.pem /etc/rabbitmq/cert.pem
COPY chain.pem /etc/rabbitmq/chain.pem
COPY fullchain.pem /etc/rabbitmq/fullchain.pem
COPY privkey.pem /etc/rabbitmq/privkey.pem

RUN rabbitmq-plugins enable rabbitmq_stomp
RUN rabbitmq-plugins enable rabbitmq_management
RUN rabbitmq-plugins enable rabbitmq_web_stomp_examples
RUN rabbitmq-plugins enable rabbitmq_mqtt
RUN rabbitmq-plugins enable rabbitmq_web_mqtt

COPY rabbitMqUserCreation.sh /etc/rabbitmq/rabbitMqUserCreation.sh

EXPOSE 5672
EXPOSE 15672
EXPOSE 15674
EXPOSE 15676
EXPOSE 4369
EXPOSE 25672
EXPOSE 61613
EXPOSE 61614
EXPOSE 55674
EXPOSE 1883
EXPOSE 15675
