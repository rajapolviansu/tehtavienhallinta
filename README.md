# Tehtävienhallinta

Tehtävienhallintasovellus, jolla hallitaan projekteja, tehtäviä ja tunnisteita. Tehty Vaadinilla ja Spring Bootilla. Java-kurssin harjoitustyö.

> **Tila:** taustajärjestelmä, kirjautuminen, roolit, kaikki neljä näkymää (tehtävät, projektit, tunnisteet, käyttäjät), käyttäjäprofiili ja validoinnit on tehty.

## Teknologiat

- Java 21
- Spring Boot 4.0.9
- Vaadin 25.3.5
- Spring Security (kirjautuminen)
- Spring Data JPA ja Bean Validation
- H2-muistitietokanta kehityksessä, PostgreSQL-ajuri mukana
- Maven (mukana `mvnw` ja `mvnw.cmd`)

## Käynnistys

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Linux / macOS:

```bash
./mvnw spring-boot:run
```

Sovellus avautuu osoitteeseen <http://localhost:8080> ja ohjaa kirjautumissivulle.

## Kirjautuminen

Kehityksessä luodaan käynnistyksessä kaksi käyttäjää: `ansu` ja `matti`. Heillä on sama salasana, eikä sitä ole kirjoitettu koodiin:

- Jos asetat ympäristömuuttujan `APP_DEV_PASSWORD`, salasana on sen arvo.
- Muuten salasana arvotaan joka käynnistyksessä ja tulostetaan lokiin (rivi "generoitu salasana").

PowerShellissä oman salasanan voi asettaa ennen käynnistystä:

```powershell
$env:APP_DEV_PASSWORD = "oma-salasana"
```

Salasanat tallennetaan tietokantaan vain tiivisteenä (BCrypt), ja niiden vähimmäispituus on 8 merkkiä.

Rooleja on kaksi: **USER** käyttää tehtäviä, projekteja ja tunnisteita, ja **ADMIN** hallinnoi lisäksi käyttäjiä (Käyttäjät-välilehti). `ansu` on ylläpitäjä ja `matti` tavallinen käyttäjä. Viimeistä ylläpitäjää ei voi poistaa eikä alentaa.

## Tietokanta

H2-konsoli on käytössä kehityksessä osoitteessa <http://localhost:8080/h2-console>. Konsoli vaatii kirjautumisen ylläpitäjänä (`ansu`). Kirjaudu ensin sovellukseen ja avaa sitten konsoli.

| Asetus | Arvo |
| --- | --- |
| JDBC URL | `jdbc:h2:mem:tehtavat` |
| User Name | `sa` |
| Password | (tyhjä) |

Tiedot häviävät, kun sovellus sammutetaan, ja testidata ladataan uudelleen joka käynnistyksessä.

Taulut: `APP_USER`, `USER_PROFILE`, `PROJECT`, `TASK`, `TAG` ja liitostaulu `TASK_TAG`. Esimerkkikysely relaatioiden tarkasteluun:

```sql
-- 1:1 käyttäjä ja profiili
SELECT u.USERNAME, p.FULL_NAME, p.JOB_TITLE
FROM APP_USER u LEFT JOIN USER_PROFILE p ON u.PROFILE_ID = p.ID;

-- 1:N projekti ja tehtävät
SELECT p.NAME, t.TITLE FROM PROJECT p JOIN TASK t ON t.PROJECT_ID = p.ID;

-- M:N tehtävä ja tunnisteet
SELECT t.TITLE, g.NAME FROM TASK t
JOIN TASK_TAG tt ON tt.TASK_ID = t.ID JOIN TAG g ON g.ID = tt.TAG_ID;
```

Huom: Bean Validation -säännöt (esimerkiksi värin muoto) toimivat vain sovelluksen kautta tallennettaessa, eivät suorassa SQL:ssä. Tietokanta valvoo vain `NOT NULL`- ja pituusrajoituksia.

## Testit

```powershell
.\mvnw.cmd test
```

Testit kattavat palvelut (käyttäjät, projektit, tehtävät, tunnisteet, haku), kirjautumisen, käyttäjän ja profiilin 1:1-relaation sekä entiteettien validoinnit.

## Projektin rakenne

Kaikki koodi on paketissa `src/main/java/fi/app/tehtavienhallinta/`.

| Tiedosto | Tehtävä |
| --- | --- |
| `AppUser`, `UserProfile`, `Project`, `Task`, `Tag` | Tietomallin luokat |
| `*Repository` | Tietokantahaut |
| `*Service` | Sovelluksen toiminnot (lisäys, muokkaus, poisto, haku) |
| `TaskFilter`, `TaskSpecifications` | Tehtävien suodatus |
| `SecurityConfig` | Kirjautumisen asetukset |
| `AppUserDetailsService` | Hakee kirjautuvan käyttäjän tietokannasta |
| `TestDataLoader` | Lataa kehityksessä esimerkkidatan |
| `ui/LoginView` | Kirjautumissivu |
| `ui/MainLayout` | AppLayout: header, sivuvalikko ja footer; kaikki näkymät käyttävät `@Route(layout = MainLayout.class)` |
| `ui/TasksView` | Tehtävälista: haku, lisäys, muokkaus ja poisto |
| `ui/TaskDialog` | Tehtävän lisäys- ja muokkauslomake |
| `ui/ProjectsView`, `ui/ProjectDialog` | Projektilista ja projektin lomake |
| `ui/TagsView`, `ui/TagDialog` | Tunnisteiden lista ja tunnisteen lomake |
| `ui/UsersView`, `ui/UserDialog` | Käyttäjien hallinta (vain ylläpitäjälle) |
| `ui/Ui` | Yhteiset ilmoitukset ja poistovahvistus |

Asetukset ovat tiedostossa `src/main/resources/application.properties`.

## Tietomalli

Entiteetit ja niiden validoitavat kentät (Bean Validation, lomakkeilla vastaavat tarkistukset suomenkielisin virheilmoituksin):

- **AppUser**: käyttäjätunnus (3–50 merkkiä, vain kirjaimia, numeroita ja `. _ -`), sähköposti, salasanatiiviste, rooli (USER tai ADMIN) ja luontiaika. Käyttäjätunnus ja sähköposti ovat yksilöllisiä.
- **UserProfile**: koko nimi (pakollinen, 2–100 merkkiä), puhelin, tehtävänimike, verkkosivu (`http://` tai `https://`) ja esittely (enintään 500 merkkiä).
- **Project**: nimi (pakollinen, vähintään 3 merkkiä), kuvaus, alkupäivä ja eräpäivä (pakollisia, eräpäivä ei voi olla ennen alkua), budjetti (ei negatiivinen, enintään 2 desimaalia) sekä arkistointi.
- **Task**: otsikko (pakollinen, vähintään 3 merkkiä), kuvaus, eräpäivä (pakollinen), tärkeys (1–5), arvioitu työmäärä (0–1000 h) ja valmis-tieto.
- **Tag**: nimi (pakollinen, 2–50 merkkiä, yksilöllinen), väri (pakollinen, muoto `#rrggbb`), kuvaus, luokka ja järjestys (0–999).

Relaatiot:

- **1:1** AppUser – UserProfile. Profiili luodaan ja poistetaan käyttäjän mukana. Näkyy Käyttäjät-välilehden gridissä (nimi ja tehtävänimike).
- **1:N** Project – Task. Projektilla on monta tehtävää, ja projektin poisto poistaa myös sen tehtävät. Näkyy tehtävälistan Projekti-sarakkeessa.
- **M:N** Task – Tag. Tehtävällä voi olla monta tunnistetta, ja tunniste voi olla monella tehtävällä. Näkyy tehtävälistan Tunnisteet-sarakkeessa.

## Tekemättä

- Rekisteröityminen (käyttäjiä voi nyt lisätä vain ylläpitäjä)
- Salasanan vaihto
- Projektien omistaminen käyttäjälle (nyt kaikki näkevät kaiken)
- PostgreSQL-asetukset tuotantokäyttöön
