package com.sistemaponto.service;

import com.sistemaponto.model.*;
import java.time.ZonedDateTime;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public class PontoService {
    private static PontoService INSTANCE;
    private final List<Usuario> usuarios = new ArrayList<>();
    private final List<RegistroPonto> registros = new ArrayList<>();
    private final List<Jornada> jornadas = new ArrayList<>();

    private PontoService() {
        // populando com 1 gestor e 1 colaborador exemplo
        usuarios.add(new Usuario("Gestor Exemplo", "gestor@exemplo.com", "11111111111", "GESTOR", "123"));
        usuarios.add(new Usuario("Colaborador Exemplo", "colab@exemplo.com", "22222222222", "COLABORADOR", "123"));
        jornadas.add(new Jornada("Padrão 8h", 8));
    }

    public static synchronized PontoService getInstance() {
        if (INSTANCE == null) INSTANCE = new PontoService();
        return INSTANCE;
    }

    public Usuario createUsuario(Usuario u) {
        // validação simples de CPF único
        boolean exists = usuarios.stream().anyMatch(x -> x.getCpf().equals(u.getCpf()));
        if (exists) return null;
        usuarios.add(u);
        return u;
    }

    public List<Usuario> listarUsuarios() { return Collections.unmodifiableList(usuarios); }

    public Usuario autenticar(String email, String senha) {
        return usuarios.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email) && u.getSenha().equals(senha))
                .findFirst().orElse(null);
    }

    public RegistroPonto registroPonto(long usuarioId, String tipo, ZonedDateTime ts) {
        RegistroPonto r = new RegistroPonto(usuarioId, tipo, ts);
        registros.add(r);
        return r;
    }

    public List<RegistroPonto> listarRegistrosDoDia(long usuarioId, LocalDate dia) {
        return registros.stream()
                .filter(r -> r.getUsuarioId() == usuarioId)
                .filter(r -> r.getTimestamp().withZoneSameInstant(ZoneId.systemDefault()).toLocalDate().equals(dia))
                .collect(Collectors.toList());
    }

    public List<RegistroPonto> listarRegistrosPeriodo(long usuarioId, LocalDate from, LocalDate to) {
        return registros.stream()
                .filter(r -> r.getUsuarioId() == usuarioId)
                .filter(r -> {
                    LocalDate d = r.getTimestamp().withZoneSameInstant(ZoneId.systemDefault()).toLocalDate();
                    return (d.isEqual(from) || d.isAfter(from)) && (d.isEqual(to) || d.isBefore(to));
                }).collect(Collectors.toList());
    }

    public Jornada createJornada(Jornada j) {
        jornadas.add(j);
        return j;
    }

    public List<Jornada> listarJornadas() { return Collections.unmodifiableList(jornadas); }
}
