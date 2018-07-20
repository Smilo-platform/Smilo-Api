FROM openjdk:8-jre

RUN mkdir /opt/smilo
COPY target/libs /opt/smilo/libs
COPY target/smilo-*.jar /opt/smilo/smilo-api.jar

WORKDIR /opt/smilo
EXPOSE 8080

CMD ["java", "-jar", "smilo-api.jar", "--spring.profiles.active=default,testnet"]]
