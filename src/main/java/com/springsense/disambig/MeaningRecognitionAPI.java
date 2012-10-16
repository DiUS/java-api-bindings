package com.springsense.disambig;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * SpringSense Meaning Recognition API binding class
 */
public class MeaningRecognitionAPI {

	private static final int DEFAULT_NUMBER_OF_RETRIES = 3;
	private static final int DEFAULT_WAIT_BETWEEN_RETRIES = 2000;

	private String url;
	private String appId;
	private String appKey;
	private Proxy proxy;

	private int numberOfRetries = DEFAULT_NUMBER_OF_RETRIES;
	private long waitBetweenRetries = DEFAULT_WAIT_BETWEEN_RETRIES;

	/**
	 * Creates a Meaning Recognition API entry point with the specified end-point
	 * URL, customer id and API key
	 * 
	 * @param url
	 *            The end-point URL to use. Most likely
	 *            http://api.springsense.com:8081/v1/disambiguate
	 * @param appId
	 *            Your application id, get yours at http://springsense.com/api
	 * @param appKey
	 *            Your secret application key
	 */
	public MeaningRecognitionAPI(String url, String appId, String appKey) {
		this(url, appId, appKey, null);
	}

	/**
	 * Create a Meaning Recognition API entry point with the specified end-point
	 * URL, customer id and API key, going through the specified proxy
	 * 
	 * @param url
	 *            The end-point URL to use. Most likely
	 *            http://api.springsense.com:8081/v1/disambiguate
	 * @param appId
	 *            Your application id, get yours at http://springsense.com/api
	 * @param appKey
	 *            Your secret application key
	 * @param proxy
	 *            The Proxy to use for communications, optional.
	 */
	public MeaningRecognitionAPI(String url, String appId, String appKey, Proxy proxy) {
		this.url = url;
		this.appId = appId;
		this.appKey = appKey;
		this.proxy = proxy;
	}

	String getAppKey() {
		return appKey;
	}

	String getAppId() {
		return appId;
	}

	String getUrl() {
		return url;
	}

	public int getNumberOfRetries() {
		return numberOfRetries;
	}

	public void setNumberOfRetries(int numberOfRetries) {
		this.numberOfRetries = numberOfRetries;
	}

	public long getWaitBetweenRetries() {
		return waitBetweenRetries;
	}

	public void setWaitBetweenRetries(long waitBetweenRetries) {
		this.waitBetweenRetries = waitBetweenRetries;
	}

	/**
	 * Makes a cached remote procedure call to the Meaning Recognition API
	 * server and attempts to disambiguate the specified text
	 * 
	 * @param textToRecognize
	 *            The text to recognize, limited to 512 characters.
	 * @return The disambiguated (recognized) result
	 * @throws Exception
	 *             In case of a communications or security error
	 */
	public DisambiguationResult recognize(String textToRecognize) {
		String jsonResponse = null;
		int attempt = 0;

		while (jsonResponse == null) {
			attempt++;
			try {
				jsonResponse = callRestfulWebService(getAuthorizationParameters(), textToRecognize);
			} catch (Exception e) {
				if (attempt > getNumberOfRetries()) {
					throw new RuntimeException(String.format("Tried %d times, but still could not disambiguate '%s'. Latest error attached.", attempt,
							textToRecognize), e);
				}
				try {
					Thread.sleep(waitBetweenRetries);
				} catch (InterruptedException e1) {
					// Ignore sleep interruption, simply proceed to next retry
				}
			}

		}

		return DisambiguationResult.fromJson(jsonResponse);
	}

	protected Map<String, String> getAuthorizationParameters() {
		Map<String, String> map = new HashMap<String, String>();

		if (getAppId() != null) {
			map.put("app_id", getAppId());
		}
		if (getAppKey() != null) {
			map.put("app_key", getAppKey());
		}

		return map;
	}

	protected String buildWebQuery(Map<String, String> parameters) throws Exception {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			String key = URLEncoder.encode(entry.getKey(), "UTF-8");
			String value = URLEncoder.encode(entry.getValue(), "UTF-8");
			sb.append(key).append("=").append(value).append("&");
		}
		return sb.toString().substring(0, sb.length() - 1);
	}

	protected String callRestfulWebService(Map<String, String> parameters, String body) throws Exception {
		String response = null;

		HashMap<String, String> mergedParameters = new HashMap<String, String>(parameters);
		mergedParameters.put("body", body);
		
		final String queryString = parameters.isEmpty() ? "" : buildWebQuery(mergedParameters);

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
