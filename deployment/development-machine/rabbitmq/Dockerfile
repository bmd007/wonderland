FROM rabbitmq:management

RUN apt-get update && apt-get -y upgrade
RUN apt-get --yes --force-yes install nano
RUN apt-get --yes --force-yes install curl

COPY rabbitmq.conf /etc/rabbitmq/rabbitmq.conf

RUN rabbitmq-plugins enable rabbitmq_web_stomp
RUN rabbitmq-plugins enable rabbitmq_management

COPY rabbitMqUserCreation.sh /etc/rabbitmq/rabbitMqUserCreation.sh
#todo make the command above automaticaally executable

EXPOSE 1883
EXPOSE 4369
EXPOSE 5672

EXPOSE 15155
EXPOSE 15672
EXPOSE 15673
EXPOSE 15674
EXPOSE 15675
EXPOSE 15676

EXPOSE 25672
EXPOSE 61613
EXPOSE 61614
EXPOSE 55674
