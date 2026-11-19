# Tehtävienhallinta

Tehtävienhallintasovellus, jolla hallitaan projekteja, tehtäviä ja tunnisteita. Tehty Vaadinilla ja Spring Bootilla. Java-kurssin harjoitustyö.

> **Tila:** taustajärjestelmä, kirjautuminen ja tehtävälista toimivat. Projektien ja tunnisteiden näkymät puuttuvat vielä.

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

## Tietokanta

H2-konsoli on käytössä kehityksessä osoitteessa `/h2-console`. JDBC-osoite on `jdbc:h2:mem:tehtavat`, käyttäjä `sa` ja salasana tyhjä. Tiedot häviävät, kun sovellus sammutetaan. Konsoli vaatii nyt kirjautumisen, ja sen toimivuus selaimessa on vielä tarkistamatta.

## Testit

```powershell
.\mvnw.cmd test
```

Testit kattavat palvelut (käyttäjät, projektit, tehtävät, tunnisteet, haku) ja kirjautumisen.

## Projektin rakenne

Kaikki koodi on paketissa `src/main/java/fi/app/tehtavienhallinta/`.

| Tiedosto | Tehtävä |
| --- | --- |
| `AppUser`, `Project`, `Task`, `Tag` | Tietomallin luokat |
| `*Repository` | Tietokantahaut |
| `*Service` | Sovelluksen toiminnot (lisäys, muokkaus, poisto, haku) |
| `TaskFilter`, `TaskSpecifications` | Tehtävien suodatus |
| `SecurityConfig` | Kirjautumisen asetukset |
| `AppUserDetailsService` | Hakee kirjautuvan käyttäjän tietokannasta |
| `TestDataLoader` | Lataa kehityksessä esimerkkidatan |
| `ui/LoginView` | Kirjautumissivu |
| `ui/MainView` | Pääsivu: otsikko, uloskirjautuminen ja välilehdet |
| `ui/TasksPanel` | Tehtävälista: haku, lisäys, muokkaus ja poisto |
| `ui/TaskDialog` | Tehtävän lisäys- ja muokkauslomake |

Asetukset ovat tiedostossa `src/main/resources/application.properties`.

## Tietomalli

- **AppUser**: käyttäjätunnus, sähköposti ja salasanatiiviste. Käyttäjätunnus ja sähköposti ovat yksilöllisiä.
- **Project**: nimi (pakollinen), kuvaus, alku- ja eräpäivä, arkistointi.
- **Task**: otsikko (pakollinen), kuvaus, eräpäivä, valmis-tieto.
- **Tag**: nimi ja väri.
- Projektilla on monta tehtävää. Projektin poisto poistaa myös sen tehtävät.
- Tehtävällä voi olla monta tunnistetta, ja tunniste voi olla monella tehtävällä.

## Tekemättä

- Käyttöliittymä projektien ja tunnisteiden hallintaan
- Rekisteröityminen ja roolit (nyt kaikilla on sama rooli)
- Projektien omistaminen käyttäjälle (nyt kaikki näkevät kaiken)
- PostgreSQL-asetukset tuotantokäyttöön
