package com.springsense.disambig;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

/**
 * API binding class
 */
class MeaningRecognitionAPI {

    private String url;
    private String customerId;
    private String apiKey;
    private Proxy proxy;

    public MeaningRecognitionAPI(String url, String customerId, String apiKey) {
        this(url, customerId, apiKey, null);
    }

    public MeaningRecognitionAPI(String url, String customerId, String apiKey, Proxy proxy) {
        this.url = url;
        this.customerId = customerId;
        this.apiKey = apiKey;
        this.proxy = proxy;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getUrl() {
        return url;
    }

    DisambiguationResult recognize(String textToRecognize) throws Exception {
        final String jsonResponse = callRestfulWebService(getAuthorizationParameters(), textToRecognize);
        return DisambiguationResult.fromJson(jsonResponse);
    }

    private Map<String, String> getAuthorizationParameters()
    {
        Map<String, String> map = new HashMap<String, String>();

        map.put("customerId", getCustomerId());
        map.put("apiKey", getApiKey());

        return map;
    }

    private String buildWebQuery(Map<String, String> parameters) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            String key = URLEncoder.encode(entry.getKey(), "UTF-8");
            String value = URLEncoder.encode(entry.getValue(), "UTF-8");
            sb.append(key).append("=").append(value).append("&");
        }
        return sb.toString().substring(0, sb.length() - 1);
    }

    private String callRestfulWebService(Map<String, String> parameters, String body) throws Exception {
        String response = null;
        final String queryString = buildWebQuery(parameters);

        URL netUrl = new URL(url + "?" + queryString);

        // Make post mode connection
        URLConnection connection = null;
        if (proxy == null) {
            connection = netUrl.openConnection();
        } else {
            connection = netUrl.openConnection(proxy);
        }
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setAllowUserInteraction(false);
        connection.setUseCaches(false);

        // Send query
        PrintStream ps = new PrintStream(connection.getOutputStream());
        ps.print(body);
        ps.close();

        // Retrieve result
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            response = sb.toString();
        } finally {
            if (br != null) {
                br.close();
            }
        }

        return response;
    }
}
