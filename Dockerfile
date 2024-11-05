FROM gradle:8.10-jdk11-jammy AS build_img
COPY . /usr/src/app
WORKDIR /usr/src/app
RUN gradle clean build


FROM eclipse-temurin:11
COPY --from=build_img /usr/src/app/build/libs /javabin
WORKDIR /javabin
ENTRYPOINT [ "java", "-jar", "/javabin/poc-driver-0.2-SNAPSHOT-all.jar" ]
