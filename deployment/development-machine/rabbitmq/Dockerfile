FROM rabbitmq:management

RUN apt-get update && apt-get -y upgrade
RUN apt-get --yes --force-yes install nano
RUN apt-get --yes --force-yes install curl

COPY rabbitmq.conf /etc/rabbitmq/conf.d/10-defaults.conf

RUN rabbitmq-plugins enable rabbitmq_stomp
RUN rabbitmq-plugins enable rabbitmq_management
RUN rabbitmq-plugins enable rabbitmq_web_stomp_examples

EXPOSE 5672
EXPOSE 15672
EXPOSE 4369
EXPOSE 25672
EXPOSE 61613
EXPOSE 61614
EXPOSE 55674
