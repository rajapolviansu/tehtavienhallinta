package fi.app.tehtavienhallinta;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Tietoturva-asetukset. Tässä vaiheessa vain salasanojen tiivistys;
 * suodatinketju ja kirjautumisnäkymä lisätään myöhemmissä vaiheissa.
 */
@Configuration
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
