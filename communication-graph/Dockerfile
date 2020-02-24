FROM adoptopenjdk/openjdk11

MAINTAINER "Mohammad Mahdi Amini 'bmd579@gmail.com'"

ADD build/libs/communication-graph.jar /communication-graph.jar

ENV JAVA_OPTS ""

ENTRYPOINT ["sh", "-c", "exec java -jar $JAVA_OPTS /communication-graph.jar"]

EXPOSE 8081
