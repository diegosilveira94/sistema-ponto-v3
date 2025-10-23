package com.sistemaponto.model;

public class Jornada {
    private static long counter = 1;
    private final long id;
    private String nome;
    private int horasDiarias; // exemplo simplificado

    public Jornada() { this.id = counter++; }
    public Jornada(String nome, int horasDiarias) {
        this.id = counter++;
        this.nome = nome;
        this.horasDiarias = horasDiarias;
    }

    public long getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public int getHorasDiarias() { return horasDiarias; }
    public void setHorasDiarias(int horasDiarias) { this.horasDiarias = horasDiarias; }
}
