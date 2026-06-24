package com.synergymarket.swing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

/**
 * Utilitário central para chamadas à API REST.
 * Armazena o token JWT após o login e o inclui automaticamente nos headers.
 */
public class ApiClient {

    private static final String BASE_URL = "http://localhost:8080/api";
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static String jwtToken = null;
    private static String loggedUser = null;
    private static String loggedPerfil = null;

    // -------------------------------------------------------------------------
    // Autenticação
    // -------------------------------------------------------------------------

    public static boolean login(String username, String senha) throws Exception {
        ObjectNode body = MAPPER.createObjectNode();
        body.put("username", username);
        body.put("senha", senha);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Map<?, ?> map = MAPPER.readValue(response.body(), Map.class);
            jwtToken   = (String) map.get("token");
            loggedUser = (String) map.get("username");
            loggedPerfil = (String) map.get("perfil");
            return true;
        }
        return false;
    }

    public static void logout() {
        jwtToken = null;
        loggedUser = null;
        loggedPerfil = null;
    }

    public static String getLoggedUser()   { return loggedUser; }
    public static String getLoggedPerfil() { return loggedPerfil; }
    public static boolean isAdmin()        { return "ROLE_ADMIN".equals(loggedPerfil); }

    // -------------------------------------------------------------------------
    // Métodos HTTP genéricos
    // -------------------------------------------------------------------------

    public static HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Authorization", "Bearer " + jwtToken)
                .header("Content-Type", "application/json")
                .GET()
                .build();
        return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static HttpResponse<String> post(String path, Object bodyObject) throws Exception {
        String json = MAPPER.writeValueAsString(bodyObject);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Authorization", "Bearer " + jwtToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static HttpResponse<String> put(String path, Object bodyObject) throws Exception {
        String json = MAPPER.writeValueAsString(bodyObject);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Authorization", "Bearer " + jwtToken)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static HttpResponse<String> delete(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Authorization", "Bearer " + jwtToken)
                .DELETE()
                .build();
        return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static ObjectMapper getMapper() { return MAPPER; }
}
