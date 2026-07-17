package com.ayudasgv.commons.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca un método de controlador cuya ejecución exitosa debe quedar registrada
 * en la tabla {@code auditoria}. {@link AuditoriaAspect} intercepta las llamadas
 * a métodos anotados y crea el registro correspondiente.
 *
 * Se anota a nivel de controlador (no de servicio) porque ahí están disponibles
 * tanto los datos de la petición (para resolver el id de la entidad) como la
 * respuesta ya construida, sin acoplar la capa de negocio a la auditoría.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Auditable {

    /** LOGIN | READ | CREATE | UPDATE | DELETE | TRAMITE (ver V3__auditoria_indices.sql). */
    String accion();

    /** EXPEDIENTE | USUARIO | TRAMITE ... */
    String entidad();
}
