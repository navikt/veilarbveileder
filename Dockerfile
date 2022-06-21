FROM ghcr.io/navikt/pus-nais-java-app/pus-nais-java-app:java17
COPY /target/veilarbveileder.jar app.jar
