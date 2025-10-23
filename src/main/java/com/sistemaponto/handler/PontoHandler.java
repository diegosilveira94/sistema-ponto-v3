package com.sistemaponto.handler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.sistemaponto.model.RegistroPonto;
import com.sistemaponto.service.PontoService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class PontoHandler implements HttpHandler {
    private final Gson gson = new Gson();
    private final PontoService service = PontoService.getInstance();

    private void addCORS(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("[DEBUG] PontoHandler: Recebida requisição " + exchange.getRequestMethod() + " " + exchange.getRequestURI());
        
        // Adicionar headers CORS para todas as requisições
        addCORS(exchange);
        
        // Responder imediatamente para requisições OPTIONS (pre-flight CORS)
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            try (HttpExchange ignored = exchange) {
                exchange.sendResponseHeaders(204, -1);
            }
            return;
        }

        String method = exchange.getRequestMethod();
        if ("POST".equalsIgnoreCase(method)) {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            System.out.println("[DEBUG] Body recebido: " + body);
            Map<String,Object> obj = gson.fromJson(body, Map.class);
            System.out.println("[DEBUG] Objeto parseado: " + obj);
            
            // Tratamento mais robusto do usuarioId
            Object rawUserId = obj.get("usuarioId");
            long usuarioId;
            if (rawUserId instanceof Number) {
                usuarioId = ((Number)rawUserId).longValue();
            } else if (rawUserId instanceof String) {
                usuarioId = Long.parseLong((String)rawUserId);
            } else {
                throw new IllegalArgumentException("usuarioId inválido");
            }
            
            String tipo = (String) obj.get("tipo");
            String iso = (String) obj.getOrDefault("timestamp", null);
            ZonedDateTime ts;
            try {
                ts = iso == null ? ZonedDateTime.now() : ZonedDateTime.parse(iso);
            } catch (DateTimeParseException e) {
                ts = ZonedDateTime.now();
            }
            System.out.println("[DEBUG] PontoHandler: Registrando ponto - usuarioId=" + usuarioId + ", tipo=" + tipo);
            RegistroPonto r = service.registroPonto(usuarioId, tipo, ts);
            System.out.println("[DEBUG] PontoHandler: Ponto registrado com sucesso - " + r);
            byte[] resp = gson.toJson(Map.of("ok", true, "registro", r)).getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type","application/json");
            exchange.sendResponseHeaders(201, resp.length);
            exchange.getResponseBody().write(resp);
            exchange.close();
            return;
        }
        if ("GET".equalsIgnoreCase(method)) {
            // /api/ponto?usuarioId=1&date=2025-10-23
            String qs = exchange.getRequestURI().getQuery();
            System.out.println("[DEBUG] Query string recebida: " + qs);
            Map<String, String> params = Utils.queryToMap(qs);
            System.out.println("[DEBUG] Parâmetros parseados: " + params);
            System.out.println("[DEBUG] PontoHandler: Parâmetros da requisição - " + params);
            long usuarioId = Long.parseLong(params.getOrDefault("usuarioId","0"));
            String dateStr = params.get("date");
            System.out.println("[DEBUG] PontoHandler: Buscando registros - usuarioId=" + usuarioId + ", date=" + dateStr);
            List<RegistroPonto> lista = service.listarRegistrosDoDia(usuarioId, java.time.LocalDate.parse(dateStr));
            byte[] resp = gson.toJson(lista).getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type","application/json");
            exchange.sendResponseHeaders(200, resp.length);
            exchange.getResponseBody().write(resp);
            exchange.close();
            return;
        }
        exchange.sendResponseHeaders(405, -1);
    }
}
