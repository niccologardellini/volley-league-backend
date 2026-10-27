# Immagine del backend Volley League.
# Build (dalla root del repository backend):
#   docker build -f relazione_esame/Dockerfile -t volley-backend .
# Avvio completo DB + backend:
#   docker compose --profile full up -d
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies > /dev/null
COPY src src
RUN ./gradlew --no-daemon bootJar -x test

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENV SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/webapp
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
