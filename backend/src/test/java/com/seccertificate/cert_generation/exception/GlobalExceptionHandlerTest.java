package com.seccertificate.cert_generation.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleHttpMessageNotReadable_withOctetStream_returnsCustomMessage() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
                "application/octet-stream",
                (org.springframework.http.HttpInputMessage) null
        );
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> resp = handler.handleHttpMessageNotReadable(ex);
        assertThat(resp.getStatusCode().value()).isEqualTo(400);
        assertThat(resp.getBody()).isNotNull();
        String msg = resp.getBody().getMessage();
        assertThat(msg != null && (msg.contains("application/octet-stream") || msg.contains("Content-Type"))).isTrue();
    }

    @Test
    void handleHttpMessageNotReadable_generic_returnsBadRequest() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
                "json parse error",
                (org.springframework.http.HttpInputMessage) null
        );
        var resp = handler.handleHttpMessageNotReadable(ex);
        assertThat(resp.getStatusCode().value()).isEqualTo(400);
        assertThat(resp.getBody().getError()).isEqualTo("BAD_REQUEST");
    }

    @Test
    void handleTypeMismatch_returnsBadRequestWithParamInfo() {
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "val", Integer.class, "age", null, null
        );
        var resp = handler.handleTypeMismatch(ex);
        assertThat(resp.getStatusCode().value()).isEqualTo(400);
        assertThat(resp.getBody().getMessage()).contains("age").contains("Integer");
    }

    @Test
    void handleGenericException_returnsInternalServerError() {
        Exception ex = new Exception("boom");
        var resp = handler.handleGenericException(ex);
        assertThat(resp.getStatusCode().value()).isEqualTo(500);
        assertThat(resp.getBody().getMessage()).contains("boom");
    }

    @Test
    void handleAccessDenied_returnsForbidden() {
        AccessDeniedException ex = new AccessDeniedException("nope");
        var resp = handler.handleAccessDenied(ex);
        assertThat(resp.getStatusCode().value()).isEqualTo(403);
        assertThat(resp.getBody().getMessage()).contains("nope");
    }
}
