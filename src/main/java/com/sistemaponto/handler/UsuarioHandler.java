package com.sistemaponto.handler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.sistemaponto.model.Usuario;
import com.sistemaponto.service.PontoService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class UsuarioHandler implements HttpHandler {
    private final Gson gson = new Gson();
    private final PontoService service = PontoService.getInstance();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if ("GET".equalsIgnoreCase(method)) {
            List<Usuario> users = service.listarUsuarios();
            byte[] resp = gson.toJson(users).getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type","application/json");
            exchange.sendResponseHeaders(200, resp.length);
            exchange.getResponseBody().write(resp);
            exchange.close();
            return;
        } else if ("POST".equalsIgnoreCase(method)) {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Usuario u = gson.fromJson(body, Usuario.class);
            Usuario created = service.createUsuario(u);
            if (created == null) {
                byte[] resp = gson.toJson(Map.of("ok", false, "msg", "CPF já cadastrado")).getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type","application/json");
                exchange.sendResponseHeaders(400, resp.length);
                exchange.getResponseBody().write(resp);
                exchange.close();
                return;
            }
            byte[] resp = gson.toJson(Map.of("ok", true, "usuario", created)).getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type","application/json");
            exchange.sendResponseHeaders(201, resp.length);
            exchange.getResponseBody().write(resp);
            exchange.close();
            return;
        }
        exchange.sendResponseHeaders(405, -1);
    }
}
