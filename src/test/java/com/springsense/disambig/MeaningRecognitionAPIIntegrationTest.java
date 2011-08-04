package com.springsense.disambig;

import com.google.gson.Gson;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class MeaningRecognitionAPIIntegrationTest {

    private MeaningRecognitionAPI api;

    @Before
    public void setUp() {
        api = new MeaningRecognitionAPI("http://api.dev.springsense.com/disambiguate", "customer id", "api key");
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testConstructor() {
        assertEquals("http://api.dev.springsense.com/disambiguate", api.getUrl());
        assertEquals("customer id", api.getCustomerId());
        assertEquals("api key", api.getApiKey());
    }

    @Test
    public void testRecognize() throws Exception {
        String textToRecognize = "black box";

        final String expectedResponseJson = "[{\"terms\": [{\"lemma\": \"black_box\", \"word\": \"black_box\", \"POS\": \"NN\", \"meanings\": [{\"definition\": \"equipment that records information about the performance of an aircraft during flight\", \"meaning\": \"black_box_n_01\"}]}], \"scores\": [1.0]}]";
        DisambiguationResult expectedResult = DisambiguationResult.fromJson(expectedResponseJson);
        final String expectedResultNormalized = new Gson().toJson(expectedResult);

        DisambiguationResult result = api.recognize(textToRecognize);
        final String resultNormalized = new Gson().toJson(result);

        assertEquals(expectedResultNormalized, resultNormalized);
    }

}