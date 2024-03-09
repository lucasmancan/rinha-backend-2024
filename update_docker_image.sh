./gradlew clean build
docker buildx build --platform linux/amd64 -t lucasmancan/rinhabackend-2024:latest .
docker push lucasmancan/rinhabackend-2024:latest
