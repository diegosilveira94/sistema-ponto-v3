package com.sistemaponto.handler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.sistemaponto.model.RegistroPonto;
import com.sistemaponto.service.PontoService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class EspelhoHandler implements HttpHandler {
    private final Gson gson = new Gson();
    private final PontoService service = PontoService.getInstance();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }
        Map<String,String> params = Utils.queryToMap(exchange.getRequestURI().getQuery());
        long usuarioId = Long.parseLong(params.getOrDefault("usuarioId","0"));
        LocalDate from = LocalDate.parse(params.getOrDefault("from", LocalDate.now().toString()));
        LocalDate to = LocalDate.parse(params.getOrDefault("to", LocalDate.now().toString()));
        List<RegistroPonto> registros = service.listarRegistrosPeriodo(usuarioId, from, to);
        // Aqui devolvemos os registros; cálculo de horas pode ser feito no front ou aqui (simplifiquei)
        byte[] resp = gson.toJson(Map.of("registros", registros)).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type","application/json");
        exchange.sendResponseHeaders(200, resp.length);
        exchange.getResponseBody().write(resp);
        exchange.close();
    }
}
