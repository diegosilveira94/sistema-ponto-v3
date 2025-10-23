package com.sistemaponto.handler;

import java.io.IOException;
import java.io.InputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class StaticFileHandler implements HttpHandler {
    private final String basePath = "/static"; // recursos em resources/static

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/") || path.equals("/index.html")) path = "/index.html";
        // remove leading slash
        String resource = basePath + path;
        resource = resource.replaceAll("//","/");
        InputStream is = getClass().getResourceAsStream(resource);
        if (is == null) {
            byte[] resp = "404 - not found".getBytes();
            exchange.sendResponseHeaders(404, resp.length);
            exchange.getResponseBody().write(resp);
            exchange.close();
            return;
        }
        String contentType = guessContentType(path);
        exchange.getResponseHeaders().add("Content-Type", contentType);
        byte[] data = is.readAllBytes();
        exchange.sendResponseHeaders(200, data.length);
        exchange.getResponseBody().write(data);
        exchange.close();
    }

    private String guessContentType(String path) {
        if (path.endsWith(".html")) return "text/html; charset=utf-8";
        if (path.endsWith(".js")) return "application/javascript; charset=utf-8";
        if (path.endsWith(".css")) return "text/css; charset=utf-8";
        return "application/octet-stream";
    }
}
