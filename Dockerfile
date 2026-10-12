# Runtime image for the BFF. Build the jar first on the host:
#   ./gradlew :server:buildFatJar --no-configuration-cache
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY server/build/libs/server-all.jar app.jar
ENV BFF_MODE=proxy \
    UPSTREAM_BASE_URL=https://api.goldenboot.com
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
