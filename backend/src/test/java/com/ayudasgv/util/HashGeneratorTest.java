package com.ayudasgv.util;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utilidad temporal para verificar y generar hashes BCrypt.
 * Se puede eliminar una vez establecida la contraseña de prueba correcta.
 */
class HashGeneratorTest {

    @Test
    void verificarYGenerarHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
        String storedHash = "$2a$10$5QScUkS.9ejdgv1VHB12g.Mx47R09oTvZY7Ke51WPp6wc48ie8K6i";
        String password = "admin123";

        boolean matches = encoder.matches(password, storedHash);
        System.out.println("=== VERIFICACION HASH BCRYPT ===");
        System.out.println("Hash en BD:      " + storedHash);
        System.out.println("Password testado: " + password);
        System.out.println("matches(): " + matches);
        System.out.println("Hash nuevo para 'admin123': " + encoder.encode(password));
    }
}
