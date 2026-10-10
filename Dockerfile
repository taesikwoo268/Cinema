FROM eclipse-temurin:25-jdk-noble AS build
WORKDIR /workspace
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw
COPY src/ src/
RUN ./mvnw -B -DskipTests package && cp target/cinema-*.jar /workspace/app.jar

FROM eclipse-temurin:25-jre-noble AS runtime
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --gid 10001 cinema \
    && useradd --uid 10001 --gid cinema --no-create-home --shell /usr/sbin/nologin cinema
WORKDIR /app
COPY --from=build --chown=cinema:cinema /workspace/app.jar /app/app.jar
USER cinema
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
