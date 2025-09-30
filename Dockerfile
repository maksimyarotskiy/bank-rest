FROM openjdk:21-oracle AS builder

WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw /app/mvnw
COPY mvnw.cmd /app/mvnw.cmd
COPY .mvn /app/.mvn
COPY pom.xml /app/pom.xml
COPY src /app/src

# Make mvnw executable
RUN chmod +x mvnw

# Install dependencies and build the application
RUN ./mvnw clean package -DskipTests -Dmaven.test.skip=true

FROM openjdk:21-oracle

WORKDIR /app

# Copy the JAR file
COPY --from=builder /app/target/*.jar app.jar

# Expose the port
EXPOSE 8080

# Set environment variables
ENV JAVA_OPTS=""

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
