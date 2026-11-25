package fi.app.tehtavienhallinta;

/**
 * Käyttäjän rooli. Roolit muodostavat hierarkian ADMIN &gt; SUPER &gt; USER (ks. SecurityConfig):
 * ylempi rooli saa kaiken, minkä alempi saa.
 * <ul>
 *   <li>USER – käyttää projekteja, tehtäviä ja tunnisteita</li>
 *   <li>SUPER – laajennetut oikeudet tavalliseen käyttäjään nähden</li>
 *   <li>ADMIN – hallinnoi lisäksi käyttäjiä</li>
 * </ul>
 */
public enum Role {
    USER("Käyttäjä"),
    SUPER("Superkäyttäjä"),
    ADMIN("Ylläpitäjä");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** Spring Securityn käyttämä authority-nimi, esim. ROLE_ADMIN. */
    public String authority() {
        return "ROLE_" + name();
    }
}
