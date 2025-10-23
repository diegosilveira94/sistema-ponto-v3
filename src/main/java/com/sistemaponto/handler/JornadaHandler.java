package com.sistemaponto.handler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.sistemaponto.model.Jornada;
import com.sistemaponto.service.PontoService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class JornadaHandler implements HttpHandler {
    private final Gson gson = new Gson();
    private final PontoService service = PontoService.getInstance();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if ("GET".equalsIgnoreCase(method)) {
            List<Jornada> j = service.listarJornadas();
            byte[] resp = gson.toJson(j).getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type","application/json");
            exchange.sendResponseHeaders(200, resp.length);
            exchange.getResponseBody().write(resp);
            exchange.close();
            return;
        } else if ("POST".equalsIgnoreCase(method)) {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Jornada j = gson.fromJson(body, Jornada.class);
            Jornada created = service.createJornada(j);
            byte[] resp = gson.toJson(Map.of("ok", true, "jornada", created)).getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type","application/json");
            exchange.sendResponseHeaders(201, resp.length);
            exchange.getResponseBody().write(resp);
            exchange.close();
            return;
        }
        exchange.sendResponseHeaders(405, -1);
    }
}
