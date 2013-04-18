package com.springsense.disambig;

import org.apache.http.client.utils.URIBuilder;

import com.mashape.unicorn.http.HttpResponse;
import com.mashape.unicorn.http.Unicorn;

/**
 * SpringSense Meaning Recognition API binding class
 */
public class MeaningRecognitionAPI {

	private static final int DEFAULT_NUMBER_OF_RETRIES = 3;
	private static final int DEFAULT_WAIT_BETWEEN_RETRIES = 2000;

	private String url;
	private String apiKey;

	private int numberOfRetries = DEFAULT_NUMBER_OF_RETRIES;
	private long waitBetweenRetries = DEFAULT_WAIT_BETWEEN_RETRIES;
	

	/**
	 * Creates a Meaning Recognition API entry point with the specified end-point
	 * URL, customer id and API key
	 * 
	 * @param url
	 *            The end-point URL to use. Most likely
	 *            https://springsense.p.mashape.com/disambiguate
	 * @param apiKey
	 *            Your private key
	 */
	public MeaningRecognitionAPI(String url, String apiKey) {
		this.url = url;
		this.apiKey = apiKey;
	}

	String getApiKey() {
		return apiKey;
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
				jsonResponse = callRestfulWebService(textToRecognize);
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

	protected String callRestfulWebService(String body) throws Exception {
		URIBuilder uriBuilder = new URIBuilder(getUrl());
		uriBuilder.addParameter("body", body);
        final String urlString = uriBuilder.build().toString();
        
		HttpResponse<String> response = Unicorn.get(urlString)
        		  .header("accept", "application/json")
        		  .header("X-Mashape-Authorization", getApiKey())
        		  .asString();
        
		if (response.getCode() != 200) {
			throw new RuntimeException(String.format("%d error received from server: '%s'", response.getCode(), response.getBody()));
		}
		return response.getBody();
	}
	
}
