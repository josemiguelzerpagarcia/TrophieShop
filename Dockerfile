FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml ./
COPY src ./src

RUN mvn -B -DskipTests clean package

FROM eclipse-temurin:21-jre

WORKDIR /app

ENV JAVA_OPTS=""

COPY --from=build /app/target/trophieshop-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8989

CMD ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]