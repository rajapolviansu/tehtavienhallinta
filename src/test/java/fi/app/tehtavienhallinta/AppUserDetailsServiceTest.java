package fi.app.tehtavienhallinta;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AppUserDetailsServiceTest {

    private static final String PASSWORD = UUID.randomUUID().toString();

    @Autowired
    UserDetailsService userDetailsService;

    @Autowired
    AppUserService users;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    void usesDatabaseBackedService() {
        assertInstanceOf(AppUserDetailsService.class, userDetailsService);
    }

    @Test
    void loadsExistingUser() {
        users.create("kirjautuja", "kirjautuja@example.com", PASSWORD);

        UserDetails details = userDetailsService.loadUserByUsername("kirjautuja");

        assertEquals("kirjautuja", details.getUsername());
        assertTrue(passwordEncoder.matches(PASSWORD, details.getPassword()));
        assertTrue(details.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void unknownUserThrows() {
        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("ei-ole-olemassa"));
    }
}
