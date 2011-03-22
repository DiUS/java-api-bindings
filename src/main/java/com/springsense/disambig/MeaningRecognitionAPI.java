package com.springsense.disambig;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

/**
 * SpringSense Meaning Recognition API binding class
 */
public class MeaningRecognitionAPI {

    private String url;
    private String customerId;
    private String apiKey;
    private Proxy proxy;

    /**
     * Create a Meaning Recognition API entry point with the specified end-point URL, customer id and API key
     * @param url The end-point URL to use. Most likeley http://api.springsense.com/disambiguate
     * @param customerId Your customer id, get yours at http://springsense.com/api
     * @param apiKey Your secret API key
     */
    public MeaningRecognitionAPI(String url, String customerId, String apiKey) {
        this(url, customerId, apiKey, null);
    }

    /**
     * Create a Meaning Recognition API entry point with the specified end-point URL, customer id and API key, going through the specified proxy
     * @param url The end-point URL to use. Most likeley http://api.springsense.com/disambiguate
     * @param customerId Your customer id, get yours at http://springsense.com/api
     * @param apiKey Your secret API key
     * @param proxy The Proxy to use for communications
     */
    public MeaningRecognitionAPI(String url, String customerId, String apiKey, Proxy proxy) {
        this.url = url;
        this.customerId = customerId;
        this.apiKey = apiKey;
        this.proxy = proxy;
    }

    String getApiKey() {
        return apiKey;
    }

    String getCustomerId() {
        return customerId;
    }

    String getUrl() {
        return url;
    }

    /**
     * Makes a remote procedure call to the Meaning Recognition API server and attempts to disambiguate the specified text
     * @param textToRecognize The text to recognize, limited to 512 characters.
     * @return The disambiguated (recognized) result
     * @throws Exception In case of a communications or security error
     */
    public DisambiguationResult recognize(String textToRecognize) throws Exception {
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
