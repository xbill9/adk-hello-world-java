# Maven and JDK image used by the Cloud Run source build.
FROM maven:3.9-eclipse-temurin-25

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn compile -B

EXPOSE 8080

# Shell expansion is required because Cloud Run supplies PORT at runtime.
CMD ["sh", "-c", "exec mvn exec:java -Dexec.mainClass=com.google.adk.web.AdkWebServer -Dexec.classpathScope=compile -Dexec.args=\"--server.port=${PORT:-8080} --adk.agents.source-dir=.\""]
