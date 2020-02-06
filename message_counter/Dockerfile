FROM adoptopenjdk/openjdk13

MAINTAINER "Mohammad Mahdi Amini 'bmd579@gmail.com'"

ADD build/libs/apsis_counter.jar /application.jar

ENV JAVA_OPTS ""

ENTRYPOINT ["sh", "-c", "exec java -jar $JAVA_OPTS /application.jar"]

EXPOSE 8585
EXPOSE 8586