# Tehtävienhallinta

Vaadin + Spring Boot -pohjainen tehtävienhallintasovellus (SPA). Java-kurssin harjoitustyö.

> **Tila:** projektin aloitettu. Näkymässä on toistaiseksi vain otsikko.

## Teknologiat

- Java 21
- Spring Boot 4.0.9
- Vaadin 25.3.5
- Spring Data JPA ja Bean Validation
- H2 (muistitietokanta kehityksessä), PostgreSQL-ajuri mukana
- Maven (mukana `mvnw` / `mvnw.cmd`)

## Käynnistys

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Linux / macOS:

```bash
./mvnw spring-boot:run
```

Sovellus avautuu osoitteeseen <http://localhost:8080>. Selainta ei avata automaattisesti (`vaadin.launch-browser=false`).

H2-konsoli on käytössä kehityksessä (`spring.h2.console.enabled=true`). JDBC-osoite on `jdbc:h2:mem:tehtavat`, käyttäjä `sa` ja salasana tyhjä.

## Testit

```powershell
.\mvnw.cmd test
```

Tällä hetkellä testeissä on vain `ApplicationTests.contextLoads`, joka tarkistaa että Spring-konteksti käynnistyy.

## Projektin rakenne

```
src/main/java/fi/example/app/
├── Application.java        Sovelluksen käynnistysluokka
└── ui/
    └── MainView.java       Ainoa näkymä (reitti "")
src/main/resources/
└── application.properties  Konfiguraatio (H2 kehityskäytössä)
```

## Tekemättä

- Tehtävä-entiteetti, repositorio ja palvelukerros
- Käyttöliittymä tehtävien listaukseen, lisäykseen, muokkaukseen ja poistoon
- Välilehdet ja paneelit pääsivulle
- PostgreSQL-asetukset tuotantokäyttöön
