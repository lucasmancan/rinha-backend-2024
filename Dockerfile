FROM arm64v8/gradle as build
WORKDIR /rinha-backend-2024
COPY build.gradle build.gradle
COPY settings.gradle settings.gradle
COPY src src
COPY conf conf
RUN gradle shadowJar

#FROM eclipse-temurin:17-jdk
FROM --platform=arm64 registry.access.redhat.com/ubi8/openjdk-21
WORKDIR /rinha-backend-2024


COPY --from=build /rinha-backend-2024/build/libs/rinha-backend-2024-1.0.0-all.jar app.jar
COPY conf conf
EXPOSE 8080
CMD ["java","-verbose:gc","-XX:+PrintGCDetails","-server","-XX:InitialRAMPercentage=75", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
