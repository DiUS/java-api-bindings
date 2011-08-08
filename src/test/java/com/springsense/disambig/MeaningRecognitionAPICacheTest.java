package com.springsense.disambig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class MeaningRecognitionAPICacheTest {

	private MeaningRecognitionAPI api;

	private final String jsonResponse = "[{\"terms\": [{\"lemma\": \"hello\", \"word\": \"hello\", \"POS\": \"UH\", \"meanings\": []}, {\"lemma\": \"world\", \"word\": \"world\", \"POS\": \"NN\", \"meanings\": [{\"definition\": \"everything that exists anywhere\", \"meaning\": \"universe_n_01\"}, {\"definition\": \"people in general; especially a distinctive group of people with some shared interest\", \"meaning\": \"world_n_02\"}, {\"definition\": \"all of your experiences that determine how things appear to you\", \"meaning\": \"world_n_03\"}]}], \"scores\": [0.33333340921091204, 0.33333334712849727, 0.33333324366059075]}]";

	int calls = 0;

	@Before
	public void setUp() throws IOException {

		api = new MeaningRecognitionAPI("no url", "fake id", "fake password",
				null, 10, 4, null) {
			protected String callRestfulWebService(
					Map<String, String> parameters, String body)
					throws Exception {
				calls++;
				return jsonResponse;
			}

		};

	}

	@Test
	public void cacheShouldReduceTheNumberOfActualCallsToServer()
			throws Exception {
		calls = 0;

		DisambiguationResult result1 = api.recognize("hello world");
		assertSame(result1, api.recognize("hello world"));
		assertSame(result1, api.recognize("hello world"));
		assertSame(result1, api.recognize("hello world"));
		assertSame(result1, api.recognize("hello world"));

		assertEquals(1, calls);
	}

	@Test
	public void cacheShouldReduceTheNumberOfActualCallsToServerMassively()
			throws Exception {
		calls = 0;

		int expectedNumberOfCalls = 8; // Must be ~80% or less of cache size to
										// avoid evictions

		for (int i = 0; i < 100000; i++) {
			String textToDisambiguate = String.format("hello world %d", i
					% expectedNumberOfCalls);
			api.recognize(textToDisambiguate);
		}

		assertEquals(expectedNumberOfCalls, calls);
	}

}
