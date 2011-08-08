package com.springsense.disambig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MeaningRecognitionAPICachePersistenceTest {

	private MeaningRecognitionAPI api;

	private final String jsonResponse = "[{\"terms\": [{\"lemma\": \"hello\", \"word\": \"hello\", \"POS\": \"UH\", \"meanings\": []}, {\"lemma\": \"world\", \"word\": \"world\", \"POS\": \"NN\", \"meanings\": [{\"definition\": \"everything that exists anywhere\", \"meaning\": \"universe_n_01\"}, {\"definition\": \"people in general; especially a distinctive group of people with some shared interest\", \"meaning\": \"world_n_02\"}, {\"definition\": \"all of your experiences that determine how things appear to you\", \"meaning\": \"world_n_03\"}]}], \"scores\": [0.33333340921091204, 0.33333334712849727, 0.33333324366059075]}]";

	int calls = 0;

	private final Logger logger = Logger.getLogger(getClass().getName());

	private File tempCacheDir;

	@Before
	public void setUp() throws IOException {

		tempCacheDir = new File(File.createTempFile("cache", "dir")
				.getParentFile(), "cache.dir");
		tempCacheDir.mkdir();
		tempCacheDir.deleteOnExit();

		logger.info(String.format("Cache dir is '%s'", tempCacheDir.getPath()));

		api = createApiInstance();

	}

	private MeaningRecognitionAPI createApiInstance() {
		return new MeaningRecognitionAPI("no url", "fake id", "fake password",
				null, 10, 4, tempCacheDir) {
			protected String callRestfulWebService(
					Map<String, String> parameters, String body)
					throws Exception {
				calls++;
				return jsonResponse;
			}

		};
	}

	@After
	public void tearDown() {
		File[] tempFilesCreated = tempCacheDir.listFiles();

		for (File file : tempFilesCreated) {
			file.delete();
		}
	}

	@Test
	public void testSerialisation() throws IOException {
		File tempFile = File.createTempFile("temp", "file");
		tempFile.deleteOnExit();

		FileOutputStream fos = new FileOutputStream(tempFile);

		ObjectOutputStream objOut = new ObjectOutputStream(fos);
		objOut.writeInt(1);
		objOut.close();

		FileInputStream fis = null;
		fis = new FileInputStream(tempFile);
		
		ObjectInputStream objIn = new ObjectInputStream(fis);
		int check = objIn.readInt();
		
		objIn.close();
		
		assertEquals(1, check);
	}

	@Test(expected = RuntimeException.class)
	public void constructorShouldThrowExceptionIfCacheStoreDirIsNotADir()
			throws Exception {
		new MeaningRecognitionAPI("no url", "fake id", "fake password", null,
				10, 4, File.createTempFile("cache", "ignore"));
	}

	@Test
	public void cacheShouldBeSerializeable() {
		int expectedNumberOfCacheEntries = 8;
		for (int i = 0; i < 100000; i++) {
			String textToDisambiguate = String.format("hello world %d", i
					% expectedNumberOfCacheEntries);
			api.recognize(textToDisambiguate);
		}

		assertEquals(expectedNumberOfCacheEntries,
				tempCacheDir.listFiles().length);

		MeaningRecognitionAPI loadedCacheAPI = createApiInstance();

		for (int i = 0; i < 100000; i++) {
			String textToDisambiguate = String.format("hello world %d", i
					% expectedNumberOfCacheEntries);
			loadedCacheAPI.recognize(textToDisambiguate);
		}

		assertEquals(expectedNumberOfCacheEntries,
				tempCacheDir.listFiles().length);
	}

}
