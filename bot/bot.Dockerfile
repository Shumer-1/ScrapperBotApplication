FROM maven:3.8.8-eclipse-temurin-22 AS build
WORKDIR /workspace
COPY bot/pom.xml bot/pom.xml
COPY bot/src bot/src
RUN mvn -f bot/pom.xml clean package -DskipTests

FROM eclipse-temurin:22-jre-alpine
WORKDIR /app
COPY --from=build /workspace/bot/target/bot.jar ./bot.jar

ARG APP_TELEGRAM_TOKEN
ENV APP_TELEGRAM_TOKEN=${APP_TELEGRAM_TOKEN}
EXPOSE 8090
ENTRYPOINT ["sh", "-c", "java -jar -Dapp.telegram-token=$APP_TELEGRAM_TOKEN /app/bot.jar"]
