package com.sistemaponto.handler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.google.gson.Gson;
import com.sistemaponto.model.Usuario;
import com.sistemaponto.service.PontoService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class AuthHandler implements HttpHandler {
    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String,String> obj = gson.fromJson(body, Map.class);
        String email = obj.get("email");
        String senha = obj.get("senha");
        Usuario u = PontoService.getInstance().autenticar(email, senha);
        if (u == null) {
            byte[] resp = gson.toJson(Map.of("ok", false, "msg", "Credenciais inválidas")).getBytes();
            exchange.getResponseHeaders().add("Content-Type","application/json");
            exchange.sendResponseHeaders(401, resp.length);
            exchange.getResponseBody().write(resp);
            exchange.close();
            return;
        }
        byte[] resp = gson.toJson(Map.of("ok", true, "usuario", u)).getBytes();
        exchange.getResponseHeaders().add("Content-Type","application/json");
        exchange.sendResponseHeaders(200, resp.length);
        exchange.getResponseBody().write(resp);
        exchange.close();
    }
}
