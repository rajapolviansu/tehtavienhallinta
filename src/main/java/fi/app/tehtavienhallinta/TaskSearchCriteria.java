package fi.app.tehtavienhallinta;

import java.time.LocalDate;

/**
 * Edistyneen haun ehdot. Kaikki kentät ovat valinnaisia: null tai tyhjä teksti = ei rajausta.
 *
 * @param text         hakusana, joka löytyy otsikosta TAI kuvauksesta (LIKE)
 * @param projectId    tehtävän projekti (valittu listasta)
 * @param projectName  projektin nimen osa (JOIN projektiin)
 * @param tagId        tehtävän tunniste (JOIN tunnisteisiin)
 * @param tagName      tunnisteen nimen osa (JOIN tunnisteisiin)
 * @param done         valmis / ei valmis
 * @param minPriority  pienin tärkeys
 * @param dueFrom      eräpäivä aikaisintaan
 * @param dueTo        eräpäivä viimeistään
 */
public record TaskSearchCriteria(
        String text,
        Long projectId,
        String projectName,
        Long tagId,
        String tagName,
        Boolean done,
        Integer minPriority,
        LocalDate dueFrom,
        LocalDate dueTo) {

    public static TaskSearchCriteria none() {
        return new TaskSearchCriteria(null, null, null, null, null, null, null, null, null);
    }
}
