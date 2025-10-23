package com.sistemaponto;

import com.sistemaponto.handler.*;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.io.IOException;

public class MainServer {
    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Static files
        server.createContext("/", new StaticFileHandler());
        server.createContext("/index.html", new StaticFileHandler());
        server.createContext("/login.html", new StaticFileHandler());
        server.createContext("/cadastro.html", new StaticFileHandler());
        server.createContext("/espelho.html", new StaticFileHandler());
        server.createContext("/gestor.html", new StaticFileHandler());
        server.createContext("/js/", new StaticFileHandler());

        // API
        server.createContext("/api/auth", new AuthHandler());
        server.createContext("/api/usuario", new UsuarioHandler());
        server.createContext("/api/ponto", new PontoHandler());
        server.createContext("/api/espelho", new EspelhoHandler());
        server.createContext("/api/jornada", new JornadaHandler());

        server.setExecutor(null);
        server.start();
        System.out.printf("Servidor rodando em http://localhost:%d%n", port);
    }
}
