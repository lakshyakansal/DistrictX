FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/districtx-api-1.0.0.jar app.jar
EXPOSE 8080
# Render PostgreSQL commonly provides postgres://; Spring expects jdbc:postgresql://.
ENTRYPOINT ["sh", "-c", "DB_URL=\"${DATABASE_URL:-jdbc:postgresql://localhost:5432/districtx}\"; case \"$DB_URL\" in postgres://*) DB_URL=\"jdbc:postgresql://${DB_URL#postgres://}\";; postgresql://*) DB_URL=\"jdbc:postgresql://${DB_URL#postgresql://}\";; esac; export SPRING_DATASOURCE_URL=\"$DB_URL\"; exec java -jar /app/app.jar"]
