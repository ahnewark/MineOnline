package gg.codie.minecraft.api;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MojangAuthService {
    private static final String BASE_URL = "https://authserver.mojang.com";

    public JSONObject authenticate(String username, String password, String clientToken) throws IOException {
        HttpURLConnection connection;

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", username);
        jsonObject.put("password", password);

        JSONObject agentObject = new JSONObject();
        agentObject.put("name", "Minecraft");
        agentObject.put("version", 1);

        jsonObject.put("agent", agentObject);
        jsonObject.put("clientToken", clientToken);
        jsonObject.put("requestUser", true);

        String json = jsonObject.toString();

        URL url = new URL(BASE_URL + "/authenticate");
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoInput(true);
        connection.setDoOutput(true);

        connection.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));
        connection.getOutputStream().flush();
        connection.getOutputStream().close();


        InputStream is = connection.getInputStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            response.append(line);
            response.append('\r');
        }
        rd.close();

        try {
            return new JSONObject(response.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            JSONObject errorObject = new JSONObject();
            errorObject.put("error", response.toString());
            return errorObject;
        }
    }

    public JSONObject refresh(String accessToken, String clientToken) throws IOException {
        HttpURLConnection connection;

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("accessToken", accessToken);
        jsonObject.put("clientToken", clientToken);
        jsonObject.put("requestUser", true);

        String json = jsonObject.toString();

        URL url = new URL(BASE_URL + "/refresh");
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoInput(true);
        connection.setDoOutput(true);

        connection.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));
        connection.getOutputStream().flush();
        connection.getOutputStream().close();

        InputStream is = connection.getInputStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            response.append(line);
            response.append('\r');
        }
        rd.close();

        try {
            return new JSONObject(response.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            JSONObject errorObject = new JSONObject();
            errorObject.put("error", response.toString());
            return errorObject;
        }
    }
}
