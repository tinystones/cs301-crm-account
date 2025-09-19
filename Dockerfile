# Use a lightweight OpenJDK image
FROM eclipse-temurin:21-jre-alpine

# Set the working directory
WORKDIR /app

# Copy the built jar file
COPY target/account-0.0.1-SNAPSHOT.jar app.jar

# Expose the port your app runs on (default Spring Boot port)
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]
