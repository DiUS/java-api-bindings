package com.springsense.disambig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Proxy;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;

public class CachedMeaningRecognitionAPI extends MeaningRecognitionAPI {
	private static final int CACHE_ENTRY_MAGIC = 0xDEAD;
	private static final int CACHE_ENTRY_VERSION = 0;

	private final ConcurrentMap<String, DisambiguationResult> cache;
	private final File cacheStoreDir;

	/**
	 * Creates a cached Meaning Recognition API entry point with the specified
	 * end-point URL, customer id and API key, going through the specified proxy
	 * 
	 * @param url
	 *            The end-point URL to use. Most likely
	 *            http://api.springsense.com/disambiguate
	 * @param customerId
	 *            Your customer id, get yours at http://springsense.com/api
	 * @param apiKey
	 *            Your secret API key
	 * @param proxy
	 *            The proxy to use for communications
	 * @param maxCacheSize
	 *            Maximum number of entries to store in the in-memory cache
	 * @param expectedNumberOfConcurrentThreads
	 *            Hint value to cache with the number of threads that will be
	 *            accessing the in-memory cache
	 * @param cacheStoreDir
	 *            Directory to load from, and then store cache entries to.
	 *            Optional, may be null
	 */
	public CachedMeaningRecognitionAPI(String url, String customerId, String apiKey, Proxy proxy, int maxCacheSize, int expectedNumberOfConcurrentThreads,
			File cacheStoreDir) {
		super(url, customerId, apiKey, proxy);

		cache = buildLRUCache(maxCacheSize, expectedNumberOfConcurrentThreads);
		this.cacheStoreDir = cacheStoreDir;

		loadStoredCacheEntries();
	}

	protected ConcurrentMap<String, DisambiguationResult> buildLRUCache(int maxCacheSize, int expectedNumberOfConcurrentThreads) {
		return new MapMaker().concurrencyLevel(expectedNumberOfConcurrentThreads).maximumSize(maxCacheSize).expireAfterWrite(365, TimeUnit.DAYS)
				.makeComputingMap(new Function<String, DisambiguationResult>() {
					public DisambiguationResult apply(String key) {
						return recognizeAndStore(key);
					}
				});
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

	protected DisambiguationResult recognizeAndStore(String textToRecognize) {
		DisambiguationResult recognitionResult = super.recognize(textToRecognize);

		try {
			store(textToRecognize, recognitionResult);
		} catch (IOException e) {
			throw new RuntimeException("Failed to write cache for recognition result due to an error", e);
		}

		return recognitionResult;
	}

	private void store(String textToRecognize, DisambiguationResult recognitionResult) throws IOException {
		if (cacheStoreDir == null) {
			return;
		}

		long timestamp = System.nanoTime();
		String cacheEntryFilename = String.format("%d-%d.entry", timestamp, textToRecognize.hashCode());
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
			throw new IOException(String.format("Couldn't store cache entry in file '%s' due to an error", cacheEntryFile.getAbsolutePath()), e);
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					throw new IOException(String.format("Couldn't flush/close file '%s' due to an error", cacheEntryFile.getAbsolutePath()), e);
				}
			}
		}
	}

	private void loadStoredCacheEntries() {
		Logger logger = Logger.getLogger(getClass().getName());

		if (cacheStoreDir == null) {
			return;
		}

		if (!cacheStoreDir.exists()) {
			if (!cacheStoreDir.mkdirs()) {
				throw new RuntimeException(String.format("Cache storage directory (cacheStoreDir) '%s' did not exist and could not be created.",
						cacheStoreDir.getAbsolutePath()));
			}
		}

		if (!cacheStoreDir.isDirectory()) {
			throw new RuntimeException(String.format("Cache storage directory (cacheStoreDir) '%s' is not a directory.", cacheStoreDir.getAbsolutePath()));
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
				logger.log(Level.WARNING,
						String.format("Could not load cache entry file: '%s' due to an error. Skipping it.", cacheEntryFile.getAbsolutePath()), e);
				;
			}
		}

		logger.info(String.format("Loaded %d cache entries from store.", cache.size()));
	}

	private void loadStoredCacheEntry(File cacheEntryFile) throws IOException {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(cacheEntryFile);

			ObjectInputStream objIn = new ObjectInputStream(fis);
			int cacheEntryMagic = objIn.readInt();
			if (CACHE_ENTRY_MAGIC != cacheEntryMagic) {
				throw new RuntimeException(String.format("Wrong version cache entry: %h", cacheEntryMagic));
			}

			int cacheEntryVersion = objIn.readInt();
			if (CACHE_ENTRY_VERSION != cacheEntryVersion) {
				throw new RuntimeException(String.format("Wrong version cache entry: %d", cacheEntryVersion));
			}

			@SuppressWarnings("unused")
			final Long timestamp = (Long) objIn.readObject();
			String textToRecognize = (String) objIn.readObject();
			DisambiguationResult recognitionResult = (DisambiguationResult) objIn.readObject();

			objIn.close();
			fis.close();

			cache.putIfAbsent(textToRecognize, recognitionResult);
		} catch (Exception e) {
			throw new IOException(String.format("Couldn't read cache entry file '%s' due to an error", cacheEntryFile.getAbsolutePath()), e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					throw new IOException(String.format("Couldn't close file '%s' due to an error", cacheEntryFile.getAbsolutePath()), e);
				}
			}
		}
	}

}
