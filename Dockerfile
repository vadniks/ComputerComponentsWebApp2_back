FROM openjdk:11
EXPOSE 8080:8080
RUN mkdir /app
COPY ./build/libs/*-all.jar /app/ktor.jar
COPY ./src/main/resources/static/res_back /res_back
ENTRYPOINT ["java","-jar","/app/ktor.jar"]