# Volley League · Backend

API REST Spring Boot 3.4 (Java 17) della piattaforma Volley League per la gestione di campionati di pallavolo. Il frontend Angular è in un repository separato.

## Avvio

1. Database MySQL (schema `webapp`, utente `webappuser` / `webapppassword`): `docker compose up -d db` (oppure `start-db.cmd` / `./start-db.sh`).
2. Backend su http://localhost:8080: `./gradlew bootRun` (Windows: `gradlew.bat bootRun`). Al primo avvio vengono create le tabelle e caricati i dati dimostrativi.
3. Test: `./gradlew test` (usa H2, non serve MySQL).

Account demo (password `password`): admin@volley.it, dirigente@4torri.it, arbitro@fipav.it, giocatore@4torri.it.

## Materiale d'esame

Tutto in `relazione_esame/`: relazione PDF, video demo, Dockerfile, `LINKS.txt` e dump del database `webapp_dump.sql`.
