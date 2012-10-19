package com.springsense.disambig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mashape.client.authentication.Authentication;
import com.mashape.client.authentication.MashapeAuthentication;
import com.mashape.client.http.ContentType;
import com.mashape.client.http.HttpClient;
import com.mashape.client.http.HttpMethod;
import com.mashape.client.http.MashapeResponse;
import com.mashape.client.http.ResponseType;

/**
 * SpringSense Meaning Recognition API binding class
 */
public class MeaningRecognitionAPI {

	private static final int DEFAULT_NUMBER_OF_RETRIES = 3;
	private static final int DEFAULT_WAIT_BETWEEN_RETRIES = 2000;

	private String url;
	private String publicKey;
	private String privateKey;

	private List<Authentication> authenticationHandlers;

	private int numberOfRetries = DEFAULT_NUMBER_OF_RETRIES;
	private long waitBetweenRetries = DEFAULT_WAIT_BETWEEN_RETRIES;
	

	/**
	 * Creates a Meaning Recognition API entry point with the specified end-point
	 * URL, customer id and API key
	 * 
	 * @param url
	 *            The end-point URL to use. Most likely
	 *            https://springsense.p.mashape.com/disambiguate
	 * @param publicKey
	 *            Your public Mashape key, get yours at https://www.mashape.com/springsense/springsense-meaning-recognition
	 * @param privateKey
	 *            Your private key
	 */
	public MeaningRecognitionAPI(String url, String publicKey, String privateKey) {
		this.url = url;
		this.publicKey = publicKey;
		this.privateKey = privateKey;
	}

	String getPrivateKey() {
		return privateKey;
	}

	String getPublicKey() {
		return publicKey;
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
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("body", body);

        MashapeResponse<String> response = HttpClient.doRequest(String.class,
                HttpMethod.GET,
                getUrl(),
                parameters,
                ContentType.FORM,
                ResponseType.STRING,
                getAuthenticationHandlers());
		
		return response.getBody();
	}

	private List<Authentication> getAuthenticationHandlers() {
		if (authenticationHandlers == null) {
			authenticationHandlers = new ArrayList<Authentication>(1);
			
			authenticationHandlers.add(new MashapeAuthentication(getPublicKey(), getPrivateKey()));
		}
		
		return authenticationHandlers;
	}

	
}
