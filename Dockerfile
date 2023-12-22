FROM ghcr.io/navikt/poao-baseimages/java:17
COPY /target/veilarbveileder.jar app.jar
