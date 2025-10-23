package com.sistemaponto.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import com.sistemaponto.model.RegistroPonto;
import com.sistemaponto.service.PontoService;

public class PontoHandler implements HttpHandler {
    private final PontoService service;
    private final Gson gson = new Gson();

    public PontoHandler(PontoService service) {
        this.service = service;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        addCORS(exchange);
        String method = exchange.getRequestMethod();
        if ("OPTIONS".equalsIgnoreCase(method)) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        URI uri = exchange.getRequestURI();
        String path = uri.getPath();

        if ("POST".equalsIgnoreCase(method) && path.equals("/api/ponto/registrar")) {
            try (InputStream is = exchange.getRequestBody()) {
                String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                JsonObject jo = gson.fromJson(body, JsonObject.class);
                String userId = jo.get("userId").getAsString();
                String type = jo.get("type").getAsString();
                Instant now = Instant.now();
                service.registroPonto(Long.parseLong(userId), type, now.atZone(java.time.ZoneId.systemDefault()));
                sendJson(exchange,200, "{\"status\":\"ok\",\"timestamp\":\""+now.toString()+"\"}");
            } catch (Exception e) {
                sendJson(exchange,400, "{\"error\":\"payload inválido\"}");
            }
            return;
        }

        if ("GET".equalsIgnoreCase(method) && path.equals("/api/ponto/listar")) {
            // expects ?userId=...&date=YYYY-MM-DD
            Map<String, String> qp = queryToMap(uri.getQuery());
            String userId = qp.get("userId");
            String date = qp.get("date");
            if (userId == null || date == null) {
                sendJson(exchange,400, "{\"error\":\"userId e date são obrigatórios\"}");
                return;
            }
            List<RegistroPonto> registros = service.listarRegistrosDoDia(Long.parseLong(userId), java.time.LocalDate.parse(date));
            // convert to JSON-friendly structure
            var out = registros.stream().map(r -> Map.of(
                    "userId", r.getUsuarioId(),
                    "tipo", r.getTipo(),
                    "timestamp", r.getTimestamp().toString()
            )).collect(Collectors.toList());
            sendJson(exchange,200, gson.toJson(out));
            return;
        }

        if ("GET".equalsIgnoreCase(method) && path.equals("/api/ponto/history")) {
            // ?userId=...&days=7
            Map<String,String> qp = queryToMap(uri.getQuery());
            String userId = qp.get("userId");
            int days = 7;
            try { if (qp.get("days") != null) days = Integer.parseInt(qp.get("days")); } catch(Exception ignored){}
            if (userId == null) {
                sendJson(exchange,400, "{\"error\":\"userId obrigatório\"}");
                return;
            }
            java.time.LocalDate today = java.time.LocalDate.now();
            List<RegistroPonto> hist = service.listarRegistrosPeriodo(Long.parseLong(userId), today.minusDays(days), today);
            Map<String, List<Map<String, String>>> out = hist.stream()
                    .collect(Collectors.groupingBy(
                            r -> r.getTimestamp().toLocalDate().toString(),
                            Collectors.mapping(r -> Map.of(
                                    "tipo", r.getTipo(),
                                    "timestamp", r.getTimestamp().toString()
                            ), Collectors.toList())
                    ));
            sendJson(exchange,200, gson.toJson(out));
            return;
        }

        sendJson(exchange,404, "{\"error\":\"endpoint não encontrado\"}");
    }

    private void sendJson(HttpExchange ex, int code, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type","application/json; charset=utf-8");
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }

    private Map<String,String> queryToMap(String query) {
        if (query == null || query.isBlank()) return Map.of();
        return java.util.Arrays.stream(query.split("&"))
                .map(s -> {
                    String[] parts = s.split("=",2);
                    String k = parts[0];
                    String v = parts.length>1?parts[1]:"";
                    return Map.entry(k, v);
                }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private void addCORS(HttpExchange ex) {
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().add("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
        ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }
}
