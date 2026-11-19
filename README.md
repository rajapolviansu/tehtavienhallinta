# Tehtävienhallinta

Vaadin + Spring Boot -pohjainen tehtävienhallintasovellus (SPA). Java-kurssin harjoitustyö.

> **Tila:** projektin aloitettu. Tietomalli (Project, Task) ja repositoriot on tehty, mutta näkymässä on toistaiseksi vain otsikko.

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
├── Project.java            Projekti-entiteetti (nimi, kuvaus, päivämäärät, arkistointi)
├── ProjectRepository.java  Projektien repositorio
├── Task.java               Tehtävä-entiteetti (kuuluu projektiin)
├── TaskRepository.java     Tehtävien repositorio
└── ui/
    └── MainView.java       Ainoa näkymä (reitti "")
src/main/resources/
└── application.properties  Konfiguraatio (H2 kehityskäytössä)
```

## Tietomalli

- **Project**: `name` (pakollinen), `description`, `startDate`, `dueDate`, `archived`, `createdAt`, `updatedAt`. Aikaleimat asetetaan automaattisesti.
- **Task**: `title` (pakollinen), `description`, `dueDate`, `done`.
- Yhteys: yksi projekti sisältää monta tehtävää (`Project.tasks` ↔ `Task.project`). Projektin poisto poistaa myös sen tehtävät, ja projektista poistettu tehtävä poistuu tietokannasta.

## Tekemättä

- Palvelukerros
- Käyttöliittymä tehtävien listaukseen, lisäykseen, muokkaukseen ja poistoon
- Välilehdet ja paneelit pääsivulle
- PostgreSQL-asetukset tuotantokäyttöön
