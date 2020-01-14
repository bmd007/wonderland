FROM adoptopenjdk/openjdk11

MAINTAINER "Mohammad Mahdi Amini 'bmd579@gmail.com'"

ADD build/libs/messenger.jar /messenger.jar

ENV JAVA_OPTS ""

ENTRYPOINT ["sh", "-c", "exec java -jar $JAVA_OPTS /messenger.jar"]

EXPOSE 8081
