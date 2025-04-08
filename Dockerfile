FROM gradle:8.10-jdk17-jammy AS build_img
COPY . /usr/src/app
WORKDIR /usr/src/app
RUN ./gradlew clean build


FROM eclipse-temurin:17
COPY --from=build_img /usr/src/app/build/libs /javabin
WORKDIR /javabin
ENTRYPOINT [ "java", "-jar", "/javabin/POCDriver.jar" ]
