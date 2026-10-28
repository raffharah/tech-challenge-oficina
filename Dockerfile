FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw -B dependency:go-offline
COPY src src
RUN ./mvnw -B package -DskipTests

FROM eclipse-temurin:21-jre
RUN useradd --system --no-create-home oficina
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
USER oficina
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
