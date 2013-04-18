package com.springsense.disambig;

import com.google.gson.Gson;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class MeaningRecognitionAPIIntegrationTest {

	private static final String API_KEY = System.getenv("MASHAPE_KEY");
	
	private MeaningRecognitionAPI api;

	@Before
	public void setUp() {
		api = new MeaningRecognitionAPI("https://springsense.p.mashape.com/disambiguate", API_KEY);
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testConstructor() {
		assertEquals("https://springsense.p.mashape.com/disambiguate", api.getUrl());
		assertEquals(API_KEY, api.getApiKey());
	}

	@Test
	public void testRecognize() throws Exception {
		String textToRecognize = "black box";

		final String expectedResponseJson = "[{\"terms\":[{\"term\":\"black box\",\"lemma\":\"black_box\",\"word\":\"black_box\",\"POS\":\"NN\",\"offset\":0,\"meanings\":[{\"definition\":\"equipment that records information about the performance of an aircraft during flight\",\"meaning\":\"black_box_n_01\"}]}],\"scores\":[1.0]}]";
		DisambiguationResult expectedResult = DisambiguationResult.fromJson(expectedResponseJson);
		final String expectedResultNormalized = new Gson().toJson(expectedResult);

		DisambiguationResult result = api.recognize(textToRecognize);
		final String resultNormalized = new Gson().toJson(result);

		assertEquals(expectedResultNormalized, resultNormalized);
	}

}