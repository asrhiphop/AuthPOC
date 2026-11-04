# The verified JAR is built by Maven before Docker runs.
# Keep the tag and manifest digest in sync when updating this image.
FROM eclipse-temurin:25-jre-alpine@sha256:56f273059de6b13a6b49fe0ded691ed2d0399bbee76156a8f2d385f7524a3d43

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
