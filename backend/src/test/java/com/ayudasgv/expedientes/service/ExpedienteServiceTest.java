package com.ayudasgv.expedientes.service;

import com.ayudasgv.commons.exception.RecursoNoEncontradoException;
import com.ayudasgv.expedientes.dto.ExpedienteBusquedaCriteria;
import com.ayudasgv.expedientes.dto.PageResponseDto;
import com.ayudasgv.expedientes.dto.SeccionAyudaDto;
import com.ayudasgv.expedientes.dto.SeccionGeneralesDto;
import com.ayudasgv.expedientes.dto.SeccionInteresadosDto;
import com.ayudasgv.expedientes.repository.ExpedienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de {@link ExpedienteService}: normalización de paginación,
 * resolución del idioma de catálogo y los caminos 404 de cada sección del detalle.
 * {@link ExpedienteRepository} se simula — estas pruebas no tocan la base de datos
 * (eso ya lo cubre la propia demo funcional contra Postgres).
 */
@ExtendWith(MockitoExtension.class)
class ExpedienteServiceTest {

    @Mock
    private ExpedienteRepository expedienteRepository;

    @InjectMocks
    private ExpedienteService expedienteService;

    @Test
    void buscar_sinPageNiSize_aplicaValoresPorDefecto() {
        when(expedienteRepository.buscar(any(), any())).thenReturn(List.of());
        when(expedienteRepository.contar(any())).thenReturn(0L);

        expedienteService.buscar(null, null, null, null, null, null, null, null, null, Locale.forLanguageTag("es"));

        ArgumentCaptor<ExpedienteBusquedaCriteria> captor = ArgumentCaptor.forClass(ExpedienteBusquedaCriteria.class);
        verify(expedienteRepository).buscar(captor.capture(), eq("descripcion_es"));
        assertThat(captor.getValue().page()).isZero();
        assertThat(captor.getValue().size()).isEqualTo(10);
    }

    @Test
    void buscar_conSizeMayorQueMaximo_loLimitaA100() {
        when(expedienteRepository.buscar(any(), any())).thenReturn(List.of());
        when(expedienteRepository.contar(any())).thenReturn(0L);

        expedienteService.buscar(null, null, null, null, null, null, 0, 500, null, Locale.forLanguageTag("es"));

        ArgumentCaptor<ExpedienteBusquedaCriteria> captor = ArgumentCaptor.forClass(ExpedienteBusquedaCriteria.class);
        verify(expedienteRepository).buscar(captor.capture(), any());
        assertThat(captor.getValue().size()).isEqualTo(100);
    }

    @Test
    void buscar_conLocaleEuskera_usaColumnaDescripcionEu() {
        when(expedienteRepository.buscar(any(), any())).thenReturn(List.of());
        when(expedienteRepository.contar(any())).thenReturn(0L);

        expedienteService.buscar(null, null, null, null, null, null, null, null, null, Locale.forLanguageTag("eu"));

        verify(expedienteRepository).buscar(any(), eq("descripcion_eu"));
    }

    @Test
    void buscar_devuelvePageResponseConElTotalDelRepositorio() {
        when(expedienteRepository.buscar(any(), any())).thenReturn(List.of());
        when(expedienteRepository.contar(any())).thenReturn(23L);

        PageResponseDto<?> resultado = expedienteService.buscar(
                null, null, null, null, null, null, 0, 10, null, Locale.forLanguageTag("es"));

        assertThat(resultado.totalElements()).isEqualTo(23L);
        assertThat(resultado.totalPages()).isEqualTo(3);
    }

    @Test
    void obtenerGenerales_conIdExistente_devuelveLaSeccion() {
        var dto = new SeccionGeneralesDto(1L, "EXP-2024-001", null, null, null, null, null);
        when(expedienteRepository.buscarGenerales(eq(1L), any())).thenReturn(Optional.of(dto));

        SeccionGeneralesDto resultado = expedienteService.obtenerGenerales(1L, Locale.forLanguageTag("es"));

        assertThat(resultado.numeroExpediente()).isEqualTo("EXP-2024-001");
    }

    @Test
    void obtenerGenerales_conIdInexistente_lanza404() {
        when(expedienteRepository.buscarGenerales(eq(99L), any())).thenReturn(Optional.empty());

        // La excepción lleva la CLAVE del mensaje (para que GlobalExceptionHandler la
        // resuelva vía MessageSource según Accept-Language), no el texto ya traducido.
        assertThatThrownBy(() -> expedienteService.obtenerGenerales(99L, Locale.forLanguageTag("es")))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .satisfies(ex -> {
                    var notFound = (RecursoNoEncontradoException) ex;
                    assertThat(notFound.getMessageKey()).isEqualTo("expediente.notfound");
                    assertThat(notFound.getArgs()).containsExactly(99L);
                });
    }

    @Test
    void obtenerInteresados_conExpedienteInexistente_lanza404YNoConsultaInteresados() {
        when(expedienteRepository.existeExpediente(99L)).thenReturn(false);

        assertThatThrownBy(() -> expedienteService.obtenerInteresados(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(expedienteRepository, never()).buscarInteresados(anyLong());
    }

    @Test
    void obtenerInteresados_conExpedienteExistente_devuelveLaSeccion() {
        var dto = new SeccionInteresadosDto(null, null, List.of());
        when(expedienteRepository.existeExpediente(1L)).thenReturn(true);
        when(expedienteRepository.buscarInteresados(1L)).thenReturn(dto);

        assertThat(expedienteService.obtenerInteresados(1L)).isSameAs(dto);
    }

    @Test
    void obtenerContacto_conExpedienteSinDatosDeContacto_lanza404() {
        when(expedienteRepository.existeExpediente(1L)).thenReturn(true);
        when(expedienteRepository.buscarContacto(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expedienteService.obtenerContacto(1L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .satisfies(ex -> assertThat(((RecursoNoEncontradoException) ex).getMessageKey())
                        .isEqualTo("expediente.contacto.notfound"));
    }

    @Test
    void obtenerAyuda_conExpedienteSinSolicitud_lanza404() {
        when(expedienteRepository.existeExpediente(1L)).thenReturn(true);
        when(expedienteRepository.buscarAyuda(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expedienteService.obtenerAyuda(1L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .satisfies(ex -> assertThat(((RecursoNoEncontradoException) ex).getMessageKey())
                        .isEqualTo("expediente.ayuda.notfound"));
    }

    @Test
    void obtenerAyuda_conSolicitudRegistrada_devuelveLaSeccion() {
        var dto = new SeccionAyudaDto(null, null, null, null);
        when(expedienteRepository.existeExpediente(1L)).thenReturn(true);
        when(expedienteRepository.buscarAyuda(1L)).thenReturn(Optional.of(dto));

        assertThat(expedienteService.obtenerAyuda(1L)).isSameAs(dto);
    }
}
