FROM alpine:latest
RUN apk --no-cache -X http://dl-cdn.alpinelinux.org/alpine/edge/testing add openjdk12 unzip wget

MAINTAINER Pidockmedia "pidockmedia@gmail.com"

RUN mkdir /app /ul
WORKDIR /ul
RUN wget -q -O comixed-release.zip https://github.com/comixed/comixed/releases/download/v0.10.0-SNAPSHOT/comixed-release-0.10.0-SNAPSHOT-RELEASE.zip
RUN unzip *.zip
RUN rm *.zip
RUN mv comixed*/bin/comixed-app*.jar /app/comixed-app.jar
WORKDIR /app
RUN rm -r /ul

ENV PATH="$JAVA_HOME/bin:${PATH}"

EXPOSE 7171
VOLUME /comic_data

CMD ["java", "-jar", "/app/comixed-app.jar"]
