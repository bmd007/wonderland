FROM adoptopenjdk/openjdk11

MAINTAINER "Mohammad Mahdi Amini 'bmd579@gmail.com'"

ADD build/libs/helloyee.jar /helloyee.jar

ENV JAVA_OPTS ""

ENTRYPOINT ["sh", "-c", "exec java -jar $JAVA_OPTS /helloyee.jar"]

EXPOSE 7451
