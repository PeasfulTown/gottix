FROM eclipse-temurin:17-alpine
RUN mkdir /opt/app
COPY target/*.jar /opt/app/app.jar
WORKDIR /opt/app
CMD [ "java", "-jar", "app.jar"]

