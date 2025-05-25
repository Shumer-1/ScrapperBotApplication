FROM maven:3.8.8-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY scrapper/pom.xml scrapper/pom.xml
COPY scrapper/src scrapper/src
RUN mvn -f scrapper/pom.xml clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/scrapper/target/scrapper.jar ./scrapper.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/scrapper.jar"]
