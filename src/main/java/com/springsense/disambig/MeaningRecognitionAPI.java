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
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;

/**
 * SpringSense Meaning Recognition API binding class
 */
public class MeaningRecognitionAPI {

	private static final int DEFAULT_NUMBER_OF_RETRIES = 3;
	private static final int DEFAULT_WAIT_BETWEEN_RETRIES = 2000;
	private static final int DEFAULT_MAX_CACHE_SIZE = 10000;
	private static final int DEFAULT_NUMBER_OF_CONCURRENT_THREADS = 4;
	
	private String url;
	private String customerId;
	private String apiKey;
	private Proxy proxy;

	private int numberOfRetries = DEFAULT_NUMBER_OF_RETRIES;
	private long waitBetweenRetries = DEFAULT_WAIT_BETWEEN_RETRIES;
	
	private final ConcurrentMap<String, DisambiguationResult> cache;

	/**
	 * Create a Meaning Recognition API entry point with the specified end-point
	 * URL, customer id and API key
	 * 
	 * @param url
	 *            The end-point URL to use. Most likeley
	 *            http://api.springsense.com/disambiguate
	 * @param customerId
	 *            Your customer id, get yours at http://springsense.com/api
	 * @param apiKey
	 *            Your secret API key
	 */
	public MeaningRecognitionAPI(String url, String customerId, String apiKey) {
		this(url, customerId, apiKey, null, DEFAULT_MAX_CACHE_SIZE, DEFAULT_NUMBER_OF_CONCURRENT_THREADS);
	}

	/**
	 * Create a Meaning Recognition API entry point with the specified end-point
	 * URL, customer id and API key, going through the specified proxy
	 * 
	 * @param url
	 *            The end-point URL to use. Most likeley
	 *            http://api.springsense.com/disambiguate
	 * @param customerId
	 *            Your customer id, get yours at http://springsense.com/api
	 * @param apiKey
	 *            Your secret API key
	 * @param proxy
	 *            The Proxy to use for communications
	 * @param maxCacheSize TODO
	 * @param expectedNumberOfConcurrentThreads TODO
	 */
	public MeaningRecognitionAPI(String url, String customerId, String apiKey,
			Proxy proxy, int maxCacheSize, int expectedNumberOfConcurrentThreads) {
		this.url = url;
		this.customerId = customerId;
		this.apiKey = apiKey;
		this.proxy = proxy;
		
		cache = buildLRUCache(maxCacheSize, expectedNumberOfConcurrentThreads);
	}

	protected ConcurrentMap<String, DisambiguationResult> buildLRUCache(
			int maxCacheSize, int expectedNumberOfConcurrentThreads) {
		return new MapMaker()
	       .concurrencyLevel(expectedNumberOfConcurrentThreads)
	       .maximumSize(maxCacheSize)
	       .expireAfterWrite(365, TimeUnit.DAYS)
	       .makeComputingMap(
	           new Function<String, DisambiguationResult>() {
	             public DisambiguationResult apply(String key) {
	               return recognizeUncached(key);
	             }
	           });
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
	 * Makes a cached remote procedure call to the Meaning Recognition API server and
	 * attempts to disambiguate the specified text
	 * 
	 * @param textToRecognize
	 *            The text to recognize, limited to 512 characters.
	 * @return The disambiguated (recognized) result
	 * @throws Exception
	 *             In case of a communications or security error
	 */
	public DisambiguationResult recognize(String textToRecognize) {
		return cache.get(textToRecognize);
	}
	
	public DisambiguationResult recognizeUncached(String textToRecognize) {
        String jsonResponse = null;
        int attempt = 0;
        
        while (jsonResponse == null) { 
        	attempt++;
        	try {
        		jsonResponse = callRestfulWebService(getAuthorizationParameters(), textToRecognize);
        	} catch (Exception e) {
        		if (attempt > getNumberOfRetries()) {
        			throw new RuntimeException(String.format("Tried %d times, but still could not disambiguate '%s'. Latest error attached.", attempt, textToRecognize), e);
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

		map.put("customerId", getCustomerId());
		map.put("apiKey", getApiKey());

		return map;
	}

	protected String buildWebQuery(Map<String, String> parameters)
			throws Exception {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			String key = URLEncoder.encode(entry.getKey(), "UTF-8");
			String value = URLEncoder.encode(entry.getValue(), "UTF-8");
			sb.append(key).append("=").append(value).append("&");
		}
		return sb.toString().substring(0, sb.length() - 1);
	}

	protected String callRestfulWebService(Map<String, String> parameters,
			String body) throws Exception {
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
			br = new BufferedReader(new InputStreamReader(
					connection.getInputStream(), "UTF-8"));
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
