package com.ayudasgv.auth.model;

import java.util.List;

/**
 * Modelo de dominio que representa a un usuario interno del sistema
 * tal como se recupera de la base de datos, incluyendo sus roles.
 *
 * Se usa Java record (Java 16+) porque es un contenedor de datos inmutable:
 * el compilador genera automáticamente constructor, getters, equals, hashCode y toString.
 * No tiene lógica de negocio — eso va en AuthService.
 *
 * Campos:
 *   - passwordHash: el hash BCrypt almacenado en BD; NUNCA se devuelve al cliente.
 *   - roles: lista de códigos de rol (ej. "ADMIN", "INSTRUCTOR") obtenida con JOIN.
 *   - idiomaPreferido: 'es' o 'eu', se incluye en el JWT para que el frontend lo use.
 */
public record Usuario(
    Long id,
    String username,
    String passwordHash,
    String nombre,
    String apellidos,
    String email,
    String idiomaPreferido,
    boolean activo,
    List<String> roles
) {}
