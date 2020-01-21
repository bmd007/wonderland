FROM adoptopenjdk/openjdk11

MAINTAINER "Mohammad Mahdi Amini 'bmd579@gmail.com'"

ADD build/libs/communication_graph.jar /communication_graph.jar

ENV JAVA_OPTS ""

ENTRYPOINT ["sh", "-c", "exec java -jar $JAVA_OPTS /communication_graph.jar"]

EXPOSE 8081
