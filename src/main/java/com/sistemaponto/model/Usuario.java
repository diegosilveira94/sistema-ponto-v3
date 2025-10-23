package com.sistemaponto.model;

public class Usuario {
    private static long counter = 1;
    private final long id;
    private String nome;
    private String email;
    private String cpf;
    private String papel; // "GESTOR" ou "COLABORADOR"
    private String senha; // só para simular autenticação (não seguro)

    public Usuario() {
        this.id = counter++;
    }

    public Usuario(String nome, String email, String cpf, String papel, String senha) {
        this.id = counter++;
        this.nome = nome;
        this.email = email;
        this.cpf = cpf;
        this.papel = papel;
        this.senha = senha;
    }

    public long getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getPapel() { return papel; }
    public void setPapel(String papel) { this.papel = papel; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
}
