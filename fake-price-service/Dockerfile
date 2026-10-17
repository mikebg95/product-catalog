FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /build
COPY . .
RUN ./mvnw -B package -DskipTests

FROM eclipse-temurin:25-jre-alpine
COPY --from=build /build/target/*.jar /app.jar
USER nobody
ENTRYPOINT ["java", "-jar", "/app.jar"]