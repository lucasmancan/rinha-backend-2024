FROM --platform=arm64 registry.access.redhat.com/ubi8/openjdk-21

WORKDIR /rinha-backend-2024
CMD ["ls"]
COPY  build/libs/rinha-backend-2024-1.0.0-all.jar app.jar

COPY conf conf

EXPOSE 8080

CMD ["java","-XX:+UseParallelGC","-server","-XX:InitialRAMPercentage=75", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
