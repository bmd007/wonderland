FROM adoptopenjdk/openjdk14

MAINTAINER "Mohammad Mahdi Amini 'bmd579@gmail.com'"

ARG PROJECT_NAME
ARG VERSION

ADD build/libs/$PROJECT_NAME-$VERSION.jar /application.jar

ENV JAVA_OPTS ""

ENTRYPOINT ["sh", "-c", "exec java -jar $JAVA_OPTS /application.jar"]

EXPOSE 8585
EXPOSE 8586