package com.springsense.disambig;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;

/**
 * SpringSense Meaning Recognition API binding class
 */
public class MeaningRecognitionAPI {

	private static final int CACHE_ENTRY_MAGIC = 0xDEAD;
	private static final int CACHE_ENTRY_VERSION = 0;
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

	private final File cacheStoreDir;

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
		this(url, customerId, apiKey, null, DEFAULT_MAX_CACHE_SIZE,
				DEFAULT_NUMBER_OF_CONCURRENT_THREADS, null);
	}

	/**
	 * Create a Meaning Recognition API entry point with the specified end-point
	 * URL, customer id and API key, going through the specified proxy
	 * 
	 * @param url
	 *            The end-point URL to use. Most likely
	 *            http://api.springsense.com/disambiguate
	 * @param customerId
	 *            Your customer id, get yours at http://springsense.com/api
	 * @param apiKey
	 *            Your secret API key
	 * @param proxy
	 *            The Proxy to use for communications
	 * @param maxCacheSize
	 *            Maximum number of entries to store in the in-memory cache
	 * @param expectedNumberOfConcurrentThreads
	 *            Hint value to cache with the number of threads that will be
	 *            accessing the in-memory cache
	 * @param cacheStoreDir
	 *            Directory to load from, and then store cache entries to.
	 *            Optional, may be null
	 */
	public MeaningRecognitionAPI(String url, String customerId, String apiKey,
			Proxy proxy, int maxCacheSize,
			int expectedNumberOfConcurrentThreads, File cacheStoreDir) {
		this.url = url;
		this.customerId = customerId;
		this.apiKey = apiKey;
		this.proxy = proxy;

		cache = buildLRUCache(maxCacheSize, expectedNumberOfConcurrentThreads);
		this.cacheStoreDir = cacheStoreDir;
		
		loadStoredCacheEntries();
	}

	protected ConcurrentMap<String, DisambiguationResult> buildLRUCache(
			int maxCacheSize, int expectedNumberOfConcurrentThreads) {
		return new MapMaker()
				.concurrencyLevel(expectedNumberOfConcurrentThreads)
				.maximumSize(maxCacheSize).expireAfterWrite(365, TimeUnit.DAYS)
				.makeComputingMap(new Function<String, DisambiguationResult>() {
					public DisambiguationResult apply(String key) {
						return recogniseAndStore(key);
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
		return cache.get(textToRecognize);
	}

	protected DisambiguationResult recogniseAndStore(String textToRecognize) {
		DisambiguationResult recognitionResult = recognizeUncached(textToRecognize);

		try {
			store(textToRecognize, recognitionResult);
		} catch (IOException e) {
			throw new RuntimeException(
					"Failed to write cache for recognition result due to an error",
					e);
		}

		return recognitionResult;
	}

	private void store(String textToRecognize,
			DisambiguationResult recognitionResult) throws IOException {
		if (cacheStoreDir == null) {
			return;
		}

		long timestamp = System.nanoTime();
		String cacheEntryFilename = String.format("%d-%d.entry", timestamp,
				textToRecognize.hashCode());
		File cacheEntryFile = new File(cacheStoreDir, cacheEntryFilename);

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(cacheEntryFile);

			ObjectOutputStream objOut = new ObjectOutputStream(fos);
			objOut.writeInt(CACHE_ENTRY_MAGIC);
			objOut.writeInt(CACHE_ENTRY_VERSION);
			objOut.writeObject(timestamp);
			objOut.writeObject(textToRecognize);
			objOut.writeObject(recognitionResult);

			objOut.flush();
			objOut.close();

			fos.flush();
			fos.close();
		} catch (Exception e) {
			throw new IOException(String.format(
					"Couldn't store cache entry in file '%s' due to an error",
					cacheEntryFile.getAbsolutePath()), e);
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					throw new IOException(String.format(
							"Couldn't flush/close file '%s' due to an error",
							cacheEntryFile.getAbsolutePath()), e);
				}
			}
		}
	}

	private void loadStoredCacheEntries() {
		if (cacheStoreDir == null) {
			return;
		}
		
		if (!cacheStoreDir.isDirectory()) {
			throw new RuntimeException(
					String.format(
							"Cache storage directory (cacheStoreDir) '%s' is not a directory.",
							cacheStoreDir.getAbsolutePath()));
		}
		
		File[] cacheEntryFiles = cacheStoreDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".entry");
			}
		});

		for (File cacheEntryFile : cacheEntryFiles) {
			try {
				loadStoredCacheEntry(cacheEntryFile);
			} catch (IOException e) {
				Logger.getLogger(getClass().getName())
						.log(Level.WARNING,
								String.format(
										"Could not load cache entry file: '%s' due to an error. Skipping it.",
										cacheEntryFile.getAbsolutePath()), e);
				;
			}
		}
	}

	private void loadStoredCacheEntry(File cacheEntryFile) throws IOException {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(cacheEntryFile);
			
			ObjectInputStream objIn = new ObjectInputStream(fis);
			int cacheEntryMagic = objIn.readInt();
			if (CACHE_ENTRY_MAGIC != cacheEntryMagic) {
				throw new RuntimeException(String.format(
						"Wrong version cache entry: %h", cacheEntryMagic));
			}
			
			int cacheEntryVersion = objIn.readInt();
			if (CACHE_ENTRY_VERSION != cacheEntryVersion) {
				throw new RuntimeException(String.format(
						"Wrong version cache entry: %d", cacheEntryVersion));
			}

			Long timestamp = (Long) objIn.readObject();
			String textToRecognize = (String) objIn.readObject();
			DisambiguationResult recognitionResult = (DisambiguationResult) objIn
					.readObject();

			objIn.close();
			fis.close();

			cache.putIfAbsent(textToRecognize, recognitionResult);
		} catch (Exception e) {
			throw new IOException(String.format(
					"Couldn't read cache entry file '%s' due to an error",
					cacheEntryFile.getAbsolutePath()), e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					throw new IOException(String.format(
							"Couldn't close file '%s' due to an error",
							cacheEntryFile.getAbsolutePath()), e);
				}
			}
		}
	}

	public DisambiguationResult recognizeUncached(String textToRecognize) {
		String jsonResponse = null;
		int attempt = 0;

		while (jsonResponse == null) {
			attempt++;
			try {
				jsonResponse = callRestfulWebService(
						getAuthorizationParameters(), textToRecognize);
			} catch (Exception e) {
				if (attempt > getNumberOfRetries()) {
					throw new RuntimeException(
							String.format(
									"Tried %d times, but still could not disambiguate '%s'. Latest error attached.",
									attempt, textToRecognize), e);
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
