package com.springsense.disambig;

import com.google.gson.Gson;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class MeaningRecognitionAPIIntegrationTest {

	private static final String APP_KEY = "c1f02a931ae759f8d6584812ef9e1859";
	private static final String APP_ID = "0b331fdb";
	
	private MeaningRecognitionAPI api;

	@Before
	public void setUp() {
		api = new MeaningRecognitionAPI("http://api.springsense.com:8081/v1/disambiguate", APP_ID, APP_KEY, null);
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testConstructor() {
		assertEquals("http://api.springsense.com:8081/v1/disambiguate", api.getUrl());
		assertEquals(APP_ID, api.getAppId());
		assertEquals(APP_KEY, api.getAppKey());
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