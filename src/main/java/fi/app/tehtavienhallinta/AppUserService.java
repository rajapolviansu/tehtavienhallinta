package fi.app.tehtavienhallinta;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Käyttäjien hallinta. Kaikki toiminnot vaativat ADMIN-roolin.
 */
@Service
@Transactional
@PreAuthorize("hasRole('ADMIN')")
public class AppUserService {

    public static final int MIN_PASSWORD_LENGTH = 8;

    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;

    public AppUserService(AppUserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Itserekisteröityminen: avoin kaikille, mutta luo aina tavallisen käyttäjän (rooli USER),
     * jottei rekisteröityjä voi korottaa ylläpitäjäksi.
     */
    @PreAuthorize("permitAll()")
    public AppUser register(String username, String email, String rawPassword) {
        return create(username, email, rawPassword, Role.USER, null);
    }

    /** Luo tavallisen käyttäjän (rooli USER). */
    public AppUser create(String username, String email, String rawPassword) {
        return create(username, email, rawPassword, Role.USER);
    }

    public AppUser create(String username, String email, String rawPassword, Role role) {
        return create(username, email, rawPassword, role, null);
    }

    /** Luo käyttäjän ja sen profiilin (1:1) yhdessä transaktiossa. */
    public AppUser create(String username, String email, String rawPassword, Role role,
                          UserProfile profile) {
        if (rawPassword == null || rawPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                    "Salasanan tulee olla vähintään " + MIN_PASSWORD_LENGTH + " merkkiä pitkä");
        }
        if (users.existsByUsername(username)) {
            throw new IllegalArgumentException("Käyttäjätunnus on jo käytössä: " + username);
        }
        if (users.existsByEmail(email)) {
            throw new IllegalArgumentException("Sähköposti on jo käytössä: " + email);
        }
        AppUser user = new AppUser(username, email, passwordEncoder.encode(rawPassword), role);
        if (profile != null) {
            user.setProfile(copyOf(profile, new UserProfile()));
        }
        return users.save(user);
    }

    @Transactional(readOnly = true)
    public List<AppUser> findAll() {
        return users.findAll();
    }

    @Transactional(readOnly = true)
    public AppUser get(Long id) {
        return users.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Käyttäjää ei löytynyt: " + id));
    }

    /** Päivittää tunnuksen ja sähköpostin; rooli säilyy ennallaan. */
    public AppUser update(Long id, String username, String email) {
        return update(id, username, email, get(id).getRole());
    }

    public AppUser update(Long id, String username, String email, Role role) {
        return update(id, username, email, role, get(id).getProfile());
    }

    /** Päivittää käyttäjän ja profiilin. Profiili luodaan, jos käyttäjällä ei vielä ole sitä. */
    public AppUser update(Long id, String username, String email, Role role, UserProfile profile) {
        AppUser user = get(id);
        if (!username.equals(user.getUsername()) && users.existsByUsername(username)) {
            throw new IllegalArgumentException("Käyttäjätunnus on jo käytössä: " + username);
        }
        if (!email.equals(user.getEmail()) && users.existsByEmail(email)) {
            throw new IllegalArgumentException("Sähköposti on jo käytössä: " + email);
        }
        if (user.getRole() == Role.ADMIN && role != Role.ADMIN) {
            requireAnotherAdmin();
        }
        user.setUsername(username);
        user.setEmail(email);
        user.setRole(role);
        if (profile != null && profile != user.getProfile()) {
            user.setProfile(copyOf(profile, user.getProfile() == null ? new UserProfile() : user.getProfile()));
        }
        return users.save(user);
    }

    private static UserProfile copyOf(UserProfile from, UserProfile to) {
        to.setFullName(from.getFullName());
        to.setPhone(from.getPhone());
        to.setJobTitle(from.getJobTitle());
        to.setBio(from.getBio());
        to.setWebsite(from.getWebsite());
        return to;
    }

    public void delete(Long id) {
        AppUser user = get(id);
        if (user.getRole() == Role.ADMIN) {
            requireAnotherAdmin();
        }
        users.delete(user);
    }

    /** Estää viimeisen ylläpitäjän poistamisen tai alentamisen, jottei kukaan lukitse itseään ulos. */
    private void requireAnotherAdmin() {
        if (users.countByRole(Role.ADMIN) <= 1) {
            throw new IllegalArgumentException("Viimeistä ylläpitäjää ei voi poistaa tai alentaa");
        }
    }
}
