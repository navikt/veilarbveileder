Veilarbveileder
===============

Veileder-api

## Overblikkvisning

`POST /api/overblikkvisning` tar imot `{"overblikkVisning":["CV","Personalia"]}`.
Lovlige valg er `CV`, `Jobbønsker`, `Oppfølging`, `Personalia`,
`Registrering` og `Ytelser`. Andre skrivemåter, ukjente valg, nullverdier og et
manglende felt gir HTTP 400. En tom liste er tillatt. Listen lagres i oppgitt rekkefølge.
Forespørsel og respons bruker samme API-enum. API-laget og databaselaget har egne
typer som oversettes til og fra domenetypene. Domenetypene har ingen
JSON-annotasjoner eller kjennskap til databaseformatet. Internt og ved lagring brukes
`CV`, `JOBBØNSKER`, `OPPFØLGING`, `PERSONALIA`, `REGISTRERING` og `YTELSER`.
Responsen bruker samme skrivemåte som forespørselen, for eksempel `Personalia`.
Ved migrering må verdiene omgjøres til store bokstaver før innsetting.
Databaselaget godtar bare dette formatet.

# Henvendelser

Spørsmål knyttet til koden eller prosjektet kan stilles ved å opprette en issue i Porten.

## For NAV-ansatte

Interne henvendelser kan sendes via Slack i kanalen #team-obo-poao.

## Kode generert av GitHub Copilot

Dette repoet bruker GitHub Copilot til å generere kode.
