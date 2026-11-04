# The verified JAR is built by Maven before Docker runs.
# Keep the tag and manifest digest in sync when updating this image.
FROM eclipse-temurin:21-jre-alpine@sha256:82cf3d9ccbe1ed46252008d51b2f2329e6b9e8a2f4394c4bebefc3ea7f4b1372

WORKDIR /app

RUN apk add --no-cache curl \
    && addgroup -S spring \
    && adduser -S spring -G spring
USER spring:spring

COPY --chown=spring:spring target/app.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=5s --start-period=30s --retries=12 \
    CMD ["curl", "--fail", "--silent", "http://localhost:8080/actuator/health"]

ENTRYPOINT ["java", "-jar", "app.jar"]
