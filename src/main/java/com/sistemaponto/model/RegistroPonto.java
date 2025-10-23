package com.sistemaponto.model;

import java.time.ZonedDateTime;

public class RegistroPonto {
    private static long counter = 1;
    private final long id;
    private long usuarioId;
    private String tipo; // entrada, almoco_inicio, almoco_fim, saida
    private ZonedDateTime timestamp;

    public RegistroPonto() {
        this.id = counter++;
    }

    public RegistroPonto(long usuarioId, String tipo, ZonedDateTime timestamp) {
        this.id = counter++;
        this.usuarioId = usuarioId;
        this.tipo = tipo;
        this.timestamp = timestamp;
    }

    public long getId() { return id; }
    public long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(long usuarioId) { this.usuarioId = usuarioId; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public ZonedDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(ZonedDateTime timestamp) { this.timestamp = timestamp; }
}
