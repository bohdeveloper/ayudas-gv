package com.ayudasgv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Punto de entrada de la aplicación Spring Boot.
 *
 * @SpringBootApplication activa el escaneo automático de componentes,
 * la autoconfiguración y la configuración de beans en el contexto de Spring.
 *
 * @EnableAspectJAutoProxy habilita el soporte de AOP (Programación Orientada a Aspectos),
 * necesario para el módulo de auditoría transversal (Semana 6) que registra
 * acciones de login y consultas sin mezclar esa lógica con el código de negocio.
 */
@SpringBootApplication
@EnableAspectJAutoProxy
public class AyudasGvApplication {

    /**
     * Arranca el contenedor de Spring Boot, que a su vez:
     * 1. Lee application.yml para obtener la configuración (BD, puerto, JWT...).
     * 2. Aplica las migraciones de Flyway (crea/actualiza las tablas de BD).
     * 3. Levanta el servidor HTTP embebido (Tomcat) en el puerto configurado.
     */
    public static void main(String[] args) {
        SpringApplication.run(AyudasGvApplication.class, args);
    }
}
