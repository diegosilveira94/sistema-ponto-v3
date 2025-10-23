package com.sistemaponto.handler;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.sistemaponto.model.RegistroPonto;
import com.sistemaponto.service.PontoService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class PontoHandler implements HttpHandler {
    private final Gson gson = new GsonBuilder()
        .registerTypeAdapter(ZonedDateTime.class, new JsonSerializer<ZonedDateTime>() {
            @Override
            public JsonElement serialize(ZonedDateTime src, Type typeOfSrc, JsonSerializationContext context) {
                return new JsonPrimitive(src.toInstant().toString());
            }
        })
        .create();
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
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        String method = exchange.getRequestMethod();
        if ("POST".equalsIgnoreCase(method)) {
            try {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                System.out.println("[DEBUG] Body recebido: " + body);
                Map<String,Object> obj = gson.fromJson(body, Map.class);
                Object usuarioIdObj = obj.get("usuarioId");
                long usuarioId;
                if (usuarioIdObj instanceof Number) {
                    usuarioId = ((Number) usuarioIdObj).longValue();
                } else if (usuarioIdObj instanceof String) {
                    usuarioId = Long.parseLong((String) usuarioIdObj);
                } else {
                    throw new IllegalArgumentException("usuarioId inválido: " + usuarioIdObj);
                }
                String tipo = obj.get("tipo").toString();
                RegistroPonto r = service.registroPonto(usuarioId, tipo, ZonedDateTime.now());
                byte[] resp = gson.toJson(Map.of("ok", true, "registro", r)).getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type","application/json");
                exchange.sendResponseHeaders(201, resp.length);
                exchange.getResponseBody().write(resp);
            } catch (Exception e) {
                e.printStackTrace();
                byte[] resp = ("{\"ok\":false,\"erro\":\"" + e.getMessage() + "\"}").getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type","application/json");
                exchange.sendResponseHeaders(400, resp.length);
                exchange.getResponseBody().write(resp);
            } finally {
                exchange.close();
            }
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
