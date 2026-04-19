FROM rabbitmq:4.2-management

COPY rabbitmq.conf /etc/rabbitmq/rabbitmq.conf

RUN rabbitmq-plugins enable rabbitmq_web_stomp

COPY --chmod=755 rabbitMqUserCreation.sh /etc/rabbitmq/rabbitMqUserCreation.sh

EXPOSE 5672 15672 61613 15674
