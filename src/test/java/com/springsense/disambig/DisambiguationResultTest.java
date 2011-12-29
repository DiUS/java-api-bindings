/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.springsense.disambig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.springsense.disambig.DisambiguationResult.Meaning;
import com.springsense.disambig.DisambiguationResult.ResolvedTerm;
import com.springsense.disambig.DisambiguationResult.Sentence;
import com.springsense.disambig.DisambiguationResult.Term;
import com.springsense.disambig.DisambiguationResult.Variant;
import com.springsense.disambig.DisambiguationResult.VariantSentence;

public class DisambiguationResultTest {
	private String jsonResponse; 
	private DisambiguationResult result;
	private Sentence multipleMeaningsSentence;
	private Sentence singleMeaningsSentence;
	private Term term;
	private Meaning meaning;
	private VariantSentence firstVariant;
	private VariantSentence middleVariant;
	private VariantSentence lastVariant;
	private List<VariantSentence> singleMeaningVariants;
	private VariantSentence singleMeaningVariant;

	@Before
	public void setUpResponse() {
	    jsonResponse = "[{\"terms\":[{\"lemma\":\"dish\",\"word\":\"dish\",\"term\":\"dish\",\"POS\":\"NN\",\"meanings\":[{\"definition\":\"a piece of dishware normally used as a container for holding or serving food\",\"meaning\":\"dish_n_01\"},{\"definition\":\"a particular item of prepared food\",\"meaning\":\"dish_n_02\"},{\"definition\":\"the quantity that a dish will hold\",\"meaning\":\"dish_n_03\"}]},{\"lemma\":\",\",\"word\":\",\",\"term\":\",\",\"POS\":\",\",\"meanings\":[]},{\"lemma\":\"very\",\"word\":\"very\",\"term\":\"very\",\"POS\":\"RB\",\"meanings\":[]},{\"lemma\":\"hot\",\"word\":\"hot\",\"term\":\"hot\",\"POS\":\"JJ\",\"meanings\":[]},{\"lemma\":\"fat\",\"word\":\"fat\",\"term\":\"fat\",\"POS\":\"JJ\",\"meanings\":[]},{\"lemma\":\".\",\"word\":\".\",\"term\":\".\",\"POS\":\".\",\"meanings\":[]}],\"scores\":[0.414161702265756,0.322192070776733,0.263646226957511]},{\"terms\":[{\"lemma\":\"Send\",\"word\":\"Send\",\"term\":\"Send\",\"POS\":\"VB\",\"meanings\":[]},{\"lemma\":\"them\",\"word\":\"them\",\"term\":\"them\",\"POS\":\"PRP\",\"meanings\":[]},{\"lemma\":\"into\",\"word\":\"into\",\"term\":\"into\",\"POS\":\"IN\",\"meanings\":[]},{\"lemma\":\"another\",\"word\":\"another\",\"term\":\"another\",\"POS\":\"DT\",\"meanings\":[]},{\"lemma\":\"one\",\"word\":\"one\",\"term\":\"one\",\"POS\":\"CD\",\"meanings\":[]},{\"lemma\":\"can\",\"word\":\"can\",\"term\":\"can\",\"POS\":\"MD\",\"meanings\":[]},{\"lemma\":\"make\",\"word\":\"make\",\"term\":\"make\",\"POS\":\"VB\",\"meanings\":[]},{\"lemma\":\"little\",\"word\":\"little\",\"term\":\"little\",\"POS\":\"JJ\",\"meanings\":[]},{\"lemma\":\"feculina\",\"word\":\"feculina\",\"term\":\"feculina\",\"POS\":\"NN\",\"meanings\":[]},{\"lemma\":\"flour\",\"word\":\"flour\",\"term\":\"flour\",\"POS\":\"NN\",\"meanings\":[{\"definition\":\"fine powdery foodstuff obtained by grinding and sifting the meal of a cereal grain\",\"meaning\":\"flour_n_01\"}]},{\"lemma\":\".\",\"word\":\".\",\"term\":\".\",\"POS\":\".\",\"meanings\":[]}],\"scores\":[1.0]},{\"terms\":[{\"lemma\":\"Beat\",\"word\":\"Beat\",\"term\":\"Beat\",\"POS\":\"NNP\",\"meanings\":[{\"definition\":\"the rhythmic contraction and expansion of the arteries with each beat of the heart\",\"meaning\":\"pulse_n_02\"},{\"definition\":\"the rhythmic contraction and expansion of the arteries with each beat of the heart\",\"meaning\":\"pulse_n_02\"},{\"definition\":\"the rhythmic contraction and expansion of the arteries with each beat of the heart\",\"meaning\":\"pulse_n_02\"}]},{\"lemma\":\"them\",\"word\":\"them\",\"term\":\"them\",\"POS\":\"PRP\",\"meanings\":[]},{\"lemma\":\"through\",\"word\":\"through\",\"term\":\"through\",\"POS\":\"IN\",\"meanings\":[]},{\"lemma\":\"the\",\"word\":\"the\",\"term\":\"the\",\"POS\":\"DT\",\"meanings\":[]},{\"lemma\":\"sieve\",\"word\":\"sieve\",\"term\":\"sieve\",\"POS\":\"NN\",\"meanings\":[{\"definition\":\"a strainer for separating lumps from powdered material or grading particles\",\"meaning\":\"sieve_n_01\"},{\"definition\":\"a strainer for separating lumps from powdered material or grading particles\",\"meaning\":\"sieve_n_01\"},{\"definition\":\"a strainer for separating lumps from powdered material or grading particles\",\"meaning\":\"sieve_n_01\"}]},{\"lemma\":\",\",\"word\":\",\",\"term\":\",\",\"POS\":\",\",\"meanings\":[]},{\"lemma\":\"return\",\"word\":\"return\",\"term\":\"return\",\"POS\":\"NN\",\"meanings\":[{\"definition\":\"a tennis stroke that sends the ball back to the other player\",\"meaning\":\"return_n_11\"},{\"definition\":\"a tennis stroke that sends the ball back to the other player\",\"meaning\":\"return_n_11\"},{\"definition\":\"a tennis stroke that sends the ball back to the other player\",\"meaning\":\"return_n_11\"}]},{\"lemma\":\"them\",\"word\":\"them\",\"term\":\"them\",\"POS\":\"PRP\",\"meanings\":[]},{\"lemma\":\"down\",\"word\":\"down\",\"term\":\"down\",\"POS\":\"RB\",\"meanings\":[]},{\"lemma\":\"stamp\",\"word\":\"stamp\",\"term\":\"stamp\",\"POS\":\"VB\",\"meanings\":[{\"definition\":\"something that can be used as an official medium of payment\",\"meaning\":\"tender_n_01\"},{\"definition\":\"something that can be used as an official medium of payment\",\"meaning\":\"tender_n_01\"},{\"definition\":\"a device incised to make an impression; used to secure a closing or to authenticate documents\",\"meaning\":\"seal_n_02\"}]},{\"lemma\":\"out\",\"word\":\"out\",\"term\":\"out\",\"POS\":\"IN\",\"meanings\":[]},{\"lemma\":\"in\",\"word\":\"in\",\"term\":\"in\",\"POS\":\"IN\",\"meanings\":[]},{\"lemma\":\"water\",\"word\":\"water\",\"term\":\"water\",\"POS\":\"NN\",\"meanings\":[{\"definition\":\"binary compound that occurs at room temperature as a clear colorless odorless tasteless liquid; freezes into ice below 0 degrees centigrade and boils above 100 degrees centigrade; widely used as a solvent\",\"meaning\":\"water_n_01\"},{\"definition\":\"binary compound that occurs at room temperature as a clear colorless odorless tasteless liquid; freezes into ice below 0 degrees centigrade and boils above 100 degrees centigrade; widely used as a solvent\",\"meaning\":\"water_n_01\"},{\"definition\":\"binary compound that occurs at room temperature as a clear colorless odorless tasteless liquid; freezes into ice below 0 degrees centigrade and boils above 100 degrees centigrade; widely used as a solvent\",\"meaning\":\"water_n_01\"}]},{\"lemma\":\"for\",\"word\":\"for\",\"term\":\"for\",\"POS\":\"IN\",\"meanings\":[]},{\"lemma\":\"a\",\"word\":\"a\",\"term\":\"a\",\"POS\":\"DT\",\"meanings\":[]},{\"lemma\":\"stew-pan\",\"word\":\"stew-pan\",\"term\":\"stew-pan\",\"POS\":\"JJ\",\"meanings\":[]},{\"lemma\":\"in\",\"word\":\"in\",\"term\":\"in\",\"POS\":\"IN\",\"meanings\":[]},{\"lemma\":\"this\",\"word\":\"this\",\"term\":\"this\",\"POS\":\"DT\",\"meanings\":[]},{\"lemma\":\"be\",\"word\":\"is\",\"term\":\"is\",\"POS\":\"VBZ\",\"meanings\":[]},{\"lemma\":\"well\",\"word\":\"well\",\"term\":\"well\",\"POS\":\"RB\",\"meanings\":[]},{\"lemma\":\"some\",\"word\":\"some\",\"term\":\"some\",\"POS\":\"DT\",\"meanings\":[]},{\"lemma\":\"cold_water\",\"word\":\"cold_water\",\"term\":\"cold_water\",\"POS\":\"NN\",\"meanings\":[{\"definition\":\"disparagement of a plan or hope or expectation\",\"meaning\":\"cold_water_n_01\"},{\"definition\":\"disparagement of a plan or hope or expectation\",\"meaning\":\"cold_water_n_01\"},{\"definition\":\"disparagement of a plan or hope or expectation\",\"meaning\":\"cold_water_n_01\"}]},{\"lemma\":\",\",\"word\":\",\",\"term\":\",\",\"POS\":\",\",\"meanings\":[]},{\"lemma\":\"and\",\"word\":\"and\",\"term\":\"and\",\"POS\":\"CC\",\"meanings\":[]},{\"lemma\":\"a\",\"word\":\"a\",\"term\":\"a\",\"POS\":\"DT\",\"meanings\":[]},{\"lemma\":\"puree\",\"word\":\"puree\",\"term\":\"puree\",\"POS\":\"NN\",\"meanings\":[{\"definition\":\"food prepared by cooking and straining or processed in a blender\",\"meaning\":\"puree_n_01\"},{\"definition\":\"food prepared by cooking and straining or processed in a blender\",\"meaning\":\"puree_n_01\"},{\"definition\":\"food prepared by cooking and straining or processed in a blender\",\"meaning\":\"puree_n_01\"}]},{\"lemma\":\"with\",\"word\":\"with\",\"term\":\"with\",\"POS\":\"IN\",\"meanings\":[]},{\"lemma\":\"all\",\"word\":\"all\",\"term\":\"all\",\"POS\":\"DT\",\"meanings\":[]},{\"lemma\":\",\",\"word\":\",\",\"term\":\",\",\"POS\":\",\",\"meanings\":[]},{\"lemma\":\"and\",\"word\":\"and\",\"term\":\"and\",\"POS\":\"CC\",\"meanings\":[]},{\"lemma\":\"in\",\"word\":\"in\",\"term\":\"in\",\"POS\":\"IN\",\"meanings\":[]},{\"lemma\":\"a\",\"word\":\"a\",\"term\":\"a\",\"POS\":\"DT\",\"meanings\":[]},{\"lemma\":\"shallow\",\"word\":\"shallow\",\"term\":\"shallow\",\"POS\":\"JJ\",\"meanings\":[]},{\"lemma\":\"case\",\"word\":\"cases\",\"term\":\"cases\",\"POS\":\"NNS\",\"meanings\":[{\"definition\":\"a comprehensive term for any proceeding in a court of law whereby an individual seeks a legal remedy\",\"meaning\":\"lawsuit_n_01\"},{\"definition\":\"an occurrence of something\",\"meaning\":\"case_n_01\"},{\"definition\":\"a comprehensive term for any proceeding in a court of law whereby an individual seeks a legal remedy\",\"meaning\":\"lawsuit_n_01\"}]},{\"lemma\":\"must\",\"word\":\"must\",\"term\":\"must\",\"POS\":\"MD\",\"meanings\":[]},{\"lemma\":\"already\",\"word\":\"already\",\"term\":\"already\",\"POS\":\"RB\",\"meanings\":[]},{\"lemma\":\"salt\",\"word\":\"salted\",\"term\":\"salted\",\"POS\":\"VBN\",\"meanings\":[]},{\"lemma\":\"water\",\"word\":\"water\",\"term\":\"water\",\"POS\":\"NN\",\"meanings\":[{\"definition\":\"the part of the earth's surface covered with water (such as a river or lake or ocean)\",\"meaning\":\"body_of_water_n_01\"},{\"definition\":\"the part of the earth's surface covered with water (such as a river or lake or ocean)\",\"meaning\":\"body_of_water_n_01\"},{\"definition\":\"the part of the earth's surface covered with water (such as a river or lake or ocean)\",\"meaning\":\"body_of_water_n_01\"}]},{\"lemma\":\"or\",\"word\":\"or\",\"term\":\"or\",\"POS\":\"CC\",\"meanings\":[]},{\"lemma\":\"Liebig\",\"word\":\"Liebig\",\"term\":\"Liebig\",\"POS\":\"NNP\",\"meanings\":[{\"definition\":\"a point or extent in space\",\"meaning\":\"location_n_01\"},{\"definition\":\"a point or extent in space\",\"meaning\":\"location_n_01\"},{\"definition\":\"a point or extent in space\",\"meaning\":\"location_n_01\"}]},{\"lemma\":\".\",\"word\":\".\",\"term\":\".\",\"POS\":\".\",\"meanings\":[]}],\"scores\":[0.333359468056914,0.333335310494971,0.333305221448115]},{\"terms\":[{\"lemma\":\"You\",\"word\":\"You\",\"term\":\"You\",\"POS\":\"PRP\",\"meanings\":[]},{\"lemma\":\"will\",\"word\":\"will\",\"term\":\"will\",\"POS\":\"MD\",\"meanings\":[]},{\"lemma\":\"hold\",\"word\":\"hold\",\"term\":\"hold\",\"POS\":\"VB\",\"meanings\":[]},{\"lemma\":\"your\",\"word\":\"your\",\"term\":\"your\",\"POS\":\"PRP$\",\"meanings\":[]},{\"lemma\":\"omelette\",\"word\":\"omelette\",\"term\":\"omelette\",\"POS\":\"NN\",\"meanings\":[{\"definition\":\"beaten eggs or an egg mixture cooked until just set; may be folded around e.g. ham or cheese or jelly\",\"meaning\":\"omelet_n_01\"},{\"definition\":\"beaten eggs or an egg mixture cooked until just set; may be folded around e.g. ham or cheese or jelly\",\"meaning\":\"omelet_n_01\"},{\"definition\":\"beaten eggs or an egg mixture cooked until just set; may be folded around e.g. ham or cheese or jelly\",\"meaning\":\"omelet_n_01\"}]},{\"lemma\":\",\",\"word\":\",\",\"term\":\",\",\"POS\":\",\",\"meanings\":[]},{\"lemma\":\"so\",\"word\":\"so\",\"term\":\"so\",\"POS\":\"RB\",\"meanings\":[]},{\"lemma\":\"as\",\"word\":\"as\",\"term\":\"as\",\"POS\":\"IN\",\"meanings\":[]},{\"lemma\":\"you\",\"word\":\"you\",\"term\":\"you\",\"POS\":\"PRP\",\"meanings\":[]},{\"lemma\":\"wish\",\"word\":\"wish\",\"term\":\"wish\",\"POS\":\"VBP\",\"meanings\":[]},{\"lemma\":\"to\",\"word\":\"to\",\"term\":\"to\",\"POS\":\"TO\",\"meanings\":[]},{\"lemma\":\"it\",\"word\":\"it\",\"term\":\"it\",\"POS\":\"PRP\",\"meanings\":[]},{\"lemma\":\",\",\"word\":\",\",\"term\":\",\",\"POS\":\",\",\"meanings\":[]},{\"lemma\":\"keep\",\"word\":\"keeping\",\"term\":\"keeping\",\"POS\":\"VBG\",\"meanings\":[{\"definition\":\"the act of retaining something\",\"meaning\":\"retention_n_01\"},{\"definition\":\"the act of retaining something\",\"meaning\":\"retention_n_01\"},{\"definition\":\"the responsibility of a guardian or keeper\",\"meaning\":\"guardianship_n_02\"}]},{\"lemma\":\"hot\",\"word\":\"hot\",\"term\":\"hot\",\"POS\":\"JJ\",\"meanings\":[]},{\"lemma\":\"dish\",\"word\":\"dish\",\"term\":\"dish\",\"POS\":\"NN\",\"meanings\":[{\"definition\":\"an activity that you like or at which you are superior\",\"meaning\":\"cup_of_tea_n_01\"},{\"definition\":\"a piece of dishware normally used as a container for holding or serving food\",\"meaning\":\"dish_n_01\"},{\"definition\":\"a piece of dishware normally used as a container for holding or serving food\",\"meaning\":\"dish_n_01\"}]},{\"lemma\":\".\",\"word\":\".\",\"term\":\".\",\"POS\":\".\",\"meanings\":[]}],\"scores\":[0.417539617808463,0.292002822125236,0.2904575600663]},{\"terms\":[{\"lemma\":\"A\",\"word\":\"A\",\"term\":\"A\",\"POS\":\"DT\",\"meanings\":[]},{\"lemma\":\"quart\",\"word\":\"quart\",\"term\":\"quart\",\"POS\":\"NN\",\"meanings\":[{\"definition\":\"a United States liquid unit equal to 32 fluid ounces; four quarts equal one gallon\",\"meaning\":\"quart_n_01\"},{\"definition\":\"a United States liquid unit equal to 32 fluid ounces; four quarts equal one gallon\",\"meaning\":\"quart_n_01\"},{\"definition\":\"a United States liquid unit equal to 32 fluid ounces; four quarts equal one gallon\",\"meaning\":\"quart_n_01\"}]},{\"lemma\":\"of\",\"word\":\"of\",\"term\":\"of\",\"POS\":\"IN\",\"meanings\":[]},{\"lemma\":\"a\",\"word\":\"a\",\"term\":\"a\",\"POS\":\"DT\",\"meanings\":[]},{\"lemma\":\"half\",\"word\":\"half\",\"term\":\"half\",\"POS\":\"PDT\",\"meanings\":[]},{\"lemma\":\"a\",\"word\":\"a\",\"term\":\"a\",\"POS\":\"DT\",\"meanings\":[]},{\"lemma\":\"lemon\",\"word\":\"lemon\",\"term\":\"lemon\",\"POS\":\"NN\",\"meanings\":[{\"definition\":\"yellow oval fruit with juicy acidic flesh\",\"meaning\":\"lemon_n_01\"},{\"definition\":\"a small evergreen tree that originated in Asia but is widely cultivated for its fruit\",\"meaning\":\"lemon_n_03\"},{\"definition\":\"a distinctive tart flavor characteristic of lemons\",\"meaning\":\"lemon_n_04\"}]},{\"lemma\":\".\",\"word\":\".\",\"term\":\".\",\"POS\":\".\",\"meanings\":[]}],\"scores\":[0.409724161023587,0.302964856834658,0.287310982141755]},{\"terms\":[{\"lemma\":\"This\",\"word\":\"This\",\"term\":\"This\",\"POS\":\"DT\",\"meanings\":[]},{\"lemma\":\"dish\",\"word\":\"dish\",\"term\":\"dish\",\"POS\":\"NN\",\"meanings\":[{\"definition\":\"a piece of dishware normally used as a container for holding or serving food\",\"meaning\":\"dish_n_01\"},{\"definition\":\"a piece of dishware normally used as a container for holding or serving food\",\"meaning\":\"dish_n_01\"},{\"definition\":\"a particular item of prepared food\",\"meaning\":\"dish_n_02\"}]},{\"lemma\":\"be\",\"word\":\"is\",\"term\":\"is\",\"POS\":\"VBZ\",\"meanings\":[]},{\"lemma\":\"good\",\"word\":\"good\",\"term\":\"good\",\"POS\":\"JJ\",\"meanings\":[]},{\"lemma\":\"white\",\"word\":\"white\",\"term\":\"white\",\"POS\":\"JJ\",\"meanings\":[]},{\"lemma\":\"crumb\",\"word\":\"crumb\",\"term\":\"crumb\",\"POS\":\"NN\",\"meanings\":[{\"definition\":\"small piece of e.g. bread or cake\",\"meaning\":\"crumb_n_03\"},{\"definition\":\"small piece of e.g. bread or cake\",\"meaning\":\"crumb_n_03\"},{\"definition\":\"small piece of e.g. bread or cake\",\"meaning\":\"crumb_n_03\"}]},{\"lemma\":\"of\",\"word\":\"of\",\"term\":\"of\",\"POS\":\"IN\",\"meanings\":[]},{\"lemma\":\"crumb\",\"word\":\"crumb\",\"term\":\"crumb\",\"POS\":\"NN\",\"meanings\":[{\"definition\":\"small piece of e.g. bread or cake\",\"meaning\":\"crumb_n_03\"},{\"definition\":\"small piece of e.g. bread or cake\",\"meaning\":\"crumb_n_03\"},{\"definition\":\"small piece of e.g. bread or cake\",\"meaning\":\"crumb_n_03\"}]},{\"lemma\":\"of\",\"word\":\"of\",\"term\":\"of\",\"POS\":\"IN\",\"meanings\":[]},{\"lemma\":\"carrot\",\"word\":\"carrots\",\"term\":\"carrots\",\"POS\":\"NNS\",\"meanings\":[{\"definition\":\"deep orange edible root of the cultivated carrot plant\",\"meaning\":\"carrot_n_01\"},{\"definition\":\"perennial plant widely cultivated as an annual in many varieties for its long conical orange edible roots; temperate and tropical regions\",\"meaning\":\"carrot_n_02\"},{\"definition\":\"deep orange edible root of the cultivated carrot plant\",\"meaning\":\"carrot_n_01\"}]},{\"lemma\":\";\",\"word\":\";\",\"term\":\";\",\"POS\":\":\",\"meanings\":[]},{\"lemma\":\"then\",\"word\":\"then\",\"term\":\"then\",\"POS\":\"RB\",\"meanings\":[]},{\"lemma\":\"peel\",\"word\":\"peel\",\"term\":\"peel\",\"POS\":\"VB\",\"meanings\":[{\"definition\":\"the rind of a fruit or vegetable\",\"meaning\":\"peel_n_02\"},{\"definition\":\"the rind of a fruit or vegetable\",\"meaning\":\"peel_n_02\"},{\"definition\":\"the rind of a fruit or vegetable\",\"meaning\":\"peel_n_02\"}]},{\"lemma\":\"them\",\"word\":\"them\",\"term\":\"them\",\"POS\":\"PRP\",\"meanings\":[]},{\"lemma\":\"for\",\"word\":\"for\",\"term\":\"for\",\"POS\":\"IN\",\"meanings\":[]},{\"lemma\":\"five\",\"word\":\"five\",\"term\":\"five\",\"POS\":\"CD\",\"meanings\":[]},{\"lemma\":\"leave\",\"word\":\"leaves\",\"term\":\"leaves\",\"POS\":\"VBZ\",\"meanings\":[]},{\"lemma\":\"and\",\"word\":\"and\",\"term\":\"and\",\"POS\":\"CC\",\"meanings\":[]},{\"lemma\":\"five\",\"word\":\"five\",\"term\":\"five\",\"POS\":\"CD\",\"meanings\":[]},{\"lemma\":\"flat\",\"word\":\"flat\",\"term\":\"flat\",\"POS\":\"JJ\",\"meanings\":[]},{\"lemma\":\"in\",\"word\":\"in\",\"term\":\"in\",\"POS\":\"IN\",\"meanings\":[]},{\"lemma\":\"the\",\"word\":\"the\",\"term\":\"the\",\"POS\":\"DT\",\"meanings\":[]},{\"lemma\":\"meat\",\"word\":\"meat\",\"term\":\"meat\",\"POS\":\"NN\",\"meanings\":[{\"definition\":\"the flesh of animals (including fishes and birds and snails) used as food\",\"meaning\":\"meat_n_01\"},{\"definition\":\"the flesh of animals (including fishes and birds and snails) used as food\",\"meaning\":\"meat_n_01\"},{\"definition\":\"the flesh of animals (including fishes and birds and snails) used as food\",\"meaning\":\"meat_n_01\"}]},{\"lemma\":\"must\",\"word\":\"must\",\"term\":\"must\",\"POS\":\"MD\",\"meanings\":[]},{\"lemma\":\"exhibit\",\"word\":\"exhibit\",\"term\":\"exhibit\",\"POS\":\"VB\",\"meanings\":[{\"definition\":\"an object or statement produced before a court of law and referred to while giving evidence\",\"meaning\":\"exhibit_n_01\"},{\"definition\":\"an object or statement produced before a court of law and referred to while giving evidence\",\"meaning\":\"exhibit_n_01\"},{\"definition\":\"an object or statement produced before a court of law and referred to while giving evidence\",\"meaning\":\"exhibit_n_01\"}]},{\"lemma\":\"the\",\"word\":\"the\",\"term\":\"the\",\"POS\":\"DT\",\"meanings\":[]},{\"lemma\":\"same\",\"word\":\"same\",\"term\":\"same\",\"POS\":\"JJ\",\"meanings\":[]},{\"lemma\":\"quantity\",\"word\":\"quantity\",\"term\":\"quantity\",\"POS\":\"NN\",\"meanings\":[{\"definition\":\"how much there is or how many there are of something that you can quantify\",\"meaning\":\"measure_n_02\"},{\"definition\":\"how much there is or how many there are of something that you can quantify\",\"meaning\":\"measure_n_02\"},{\"definition\":\"how much there is or how many there are of something that you can quantify\",\"meaning\":\"measure_n_02\"}]}],\"scores\":[0.334084389429456,0.332973359180187,0.332942251390357]}]";
		result = DisambiguationResult.fromJson(jsonResponse);
		singleMeaningsSentence = result.getSentences().get(1);
		singleMeaningVariants = singleMeaningsSentence.getVariants();
		singleMeaningVariant = singleMeaningVariants.get(0);

		multipleMeaningsSentence = result.getSentences().get(2);
		term = multipleMeaningsSentence.getTerms().get(9);
		meaning = term.getMeanings().get(2);

		firstVariant = multipleMeaningsSentence.getVariants().get(0);
		middleVariant = multipleMeaningsSentence.getVariants().get(1);
		lastVariant = multipleMeaningsSentence.getVariants().get(2);

		// System.out.println(new Gson().toJson(sentence));
	}

	@Test
	public void testSentences() {
		assertTrue(result.getSentences().size() == 6);
		
	}

	@Test
	public void testSentenceEqualsIsTolerant() {
		assertTrue(result.getSentences().size() == 6);
		
		Sentence alternateCopyMultipleMeaningsSentence = DisambiguationResult.fromJson(jsonResponse).getSentences().get(2);
		
		assertTrue(multipleMeaningsSentence.equals(alternateCopyMultipleMeaningsSentence));
		alternateCopyMultipleMeaningsSentence.getScores()[0] -= 0.0005;
		assertTrue(multipleMeaningsSentence.equals(alternateCopyMultipleMeaningsSentence));
		alternateCopyMultipleMeaningsSentence.getScores()[1] -= 0.0015;
		assertTrue(!multipleMeaningsSentence.equals(alternateCopyMultipleMeaningsSentence));
	}

	@Test
	public void testScores() {
		assertEquals(3, multipleMeaningsSentence.getScores().length);
	}

	@Test
	public void testTerms() {
		assertEquals(41, multipleMeaningsSentence.getTerms().size());
	}

	@Test
	public void testTerm() {
		assertEquals("stamp", term.getLemma());
		assertEquals("stamp", term.getWord());
		assertEquals("VB", term.getPOS());
		assertEquals(3, term.getMeanings().size());
	}

	@Test
	public void testMeaning() {
		assertEquals("seal_n_02", meaning.getMeaning());
		assertEquals(
				"a device incised to make an impression; used to secure a closing or to authenticate documents",
				meaning.getDefinition());
	}

	@Test
	public void testSentenceVariants() {
		assertEquals(3, multipleMeaningsSentence.getVariants().size());

		assertEquals(
				"pulse_n_02 them through the sieve_n_01 , return_n_11 them down seal_n_02 out in water_n_01 for a stew-pan in this is well some cold_water_n_01 , and a puree_n_01 with all , and in a shallow lawsuit_n_01 must already salted body_of_water_n_01 or Liebig .",
				lastVariant.toString());

		final ResolvedTerm pulseFromLastVariant = lastVariant.getTerms().get(0);
		final ResolvedTerm pulseFromFirstVariant = firstVariant.getTerms().get(
				0);

		assertSame(pulseFromFirstVariant, pulseFromLastVariant);
		assertEquals(1.0, pulseFromLastVariant.getScore(), 0.01);

		final ResolvedTerm stampFromFirstVariant = firstVariant.getTerms().get(
				9);
		final ResolvedTerm stampFromMiddleVariant = middleVariant.getTerms()
				.get(9);
		final ResolvedTerm stampFromLastVariant = lastVariant.getTerms().get(9);

		assertSame(stampFromFirstVariant, stampFromMiddleVariant);
		assertEquals(0.66, stampFromMiddleVariant.getScore(), 0.01);

		assertEquals("seal_n_02", stampFromLastVariant.getMeaning()
				.getMeaning());
		assertEquals(0.33, stampFromLastVariant.getScore(), 0.01);
	}

	@Test
	public void testSentenceVariantsForOneMeaningSentence() {
		final ResolvedTerm flour_n_01 = singleMeaningVariant.getTerms().get(
				singleMeaningVariant.getTerms().size() - 2);

		assertEquals(
				"'sentence':['terms':['term':{word:'Send', term:'Send', lemma:'Send', POS:'VB', meanings:[], offset:0}, 'term':{word:'them', term:'them', lemma:'them', POS:'PRP', meanings:[], offset:0}, 'term':{word:'into', term:'into', lemma:'into', POS:'IN', meanings:[], offset:0}, 'term':{word:'another', term:'another', lemma:'another', POS:'DT', meanings:[], offset:0}, 'term':{word:'one', term:'one', lemma:'one', POS:'CD', meanings:[], offset:0}, 'term':{word:'can', term:'can', lemma:'can', POS:'MD', meanings:[], offset:0}, 'term':{word:'make', term:'make', lemma:'make', POS:'VB', meanings:[], offset:0}, 'term':{word:'little', term:'little', lemma:'little', POS:'JJ', meanings:[], offset:0}, 'term':{word:'feculina', term:'feculina', lemma:'feculina', POS:'NN', meanings:[], offset:0}, 'term':{word:'flour', term:'flour', lemma:'flour', POS:'NN', meanings:['meaning':{'meaning':'flour_n_01','definition':'fine powdery foodstuff obtained by grinding and sifting the meal of a cereal grain'}], offset:0}, 'term':{word:'.', term:'.', lemma:'.', POS:'.', meanings:[], offset:0}],'scores':[1.0]]",
				singleMeaningsSentence.toString());
		assertEquals(1, singleMeaningVariants.size());

		assertEquals(
				"Send them into another one can make little feculina flour_n_01 .",
				singleMeaningVariant.toString());

		assertNotNull(flour_n_01);
		assertNotNull(flour_n_01.getMeaning());
		assertEquals("flour_n_01", flour_n_01.getMeaning().getMeaning());
		assertEquals(1.0, flour_n_01.getScore(), 0.01);
	}

	@Test
	public void testVariants() {
		List<Variant> variants = result.getVariants();

		assertEquals(singleMeaningVariant, variants.get(0).getSentences()
				.get(1));
		assertEquals(singleMeaningVariant, variants.get(1).getSentences()
				.get(1));
		assertEquals(singleMeaningVariant, variants.get(2).getSentences()
				.get(1));

		assertEquals(firstVariant, variants.get(0).getSentences().get(2));
		assertEquals(middleVariant, variants.get(1).getSentences().get(2));
		assertEquals(lastVariant, variants.get(2).getSentences().get(2));
	}

	@Test
	public void testVariantsToString() {
		assertEquals(
				"dish_n_02 , very hot fat . Send them into another one can make little feculina flour_n_01 . pulse_n_02 them through the sieve_n_01 , return_n_11 them down tender_n_01 out in water_n_01 for a stew-pan in this is well some cold_water_n_01 , and a puree_n_01 with all , and in a shallow case_n_01 must already salted body_of_water_n_01 or Liebig . You will hold your omelet_n_01 , so as you wish to it , retention_n_01 hot dish_n_01 . A quart_n_01 of a half a lemon_n_03 . This dish_n_01 is good white crumb_n_03 of crumb_n_03 of carrot_n_02 ; then peel_n_02 them for five leaves and five flat in the meat_n_01 must exhibit_n_01 the same measure_n_02",
				result.getVariants().get(1).toString());
	}

	@Test
	public void testVariantsWithEmptyDisambiguation() {
		String jsonResponse = "[{\"terms\": [{\"lemma\": \"Postgraduate\", \"word\": \"Postgraduate\", \"POS\":    			\"NNP\", \"meanings\": [{\"definition\": \"a student who continues studies    			after graduation\", \"meaning\": \"graduate_student_n_01\"}, {\"definition\":    			\"a student who continues studies after graduation\", \"meaning\":    			\"graduate_student_n_01\"}, {\"definition\": \"a student who continues    			studies after graduation\", \"meaning\": \"graduate_student_n_01\"}]},    			{\"lemma\": \"course\", \"word\": \"course\", \"POS\": \"NN\", \"meanings\":    			[{\"definition\": \"education imparted in a series of lessons or    			meetings\", \"meaning\": \"course_n_01\"}, {\"definition\": \"education    			imparted in a series of lessons or meetings\", \"meaning\":    			\"course_n_01\"}, {\"definition\": \"education imparted in a series of    			lessons or meetings\", \"meaning\": \"course_n_01\"}]}, {\"lemma\": \"-\",    			\"word\": \"-\", \"POS\": \":\", \"meanings\": []}, {\"lemma\": \"Develop\", \"word\":    			\"Develop\", \"POS\": \"VB\", \"meanings\": []}, {\"lemma\": \"specialise\",    			\"word\": \"specialised\", \"POS\": \"VBN\", \"meanings\": []}, {\"lemma\":    			\"information\", \"word\": \"information\", \"POS\": \"NN\", \"meanings\":    			[{\"definition\": \"a message received and understood\", \"meaning\":    			\"information_n_01\"}, {\"definition\": \"a collection of facts from which    			conclusions may be drawn\", \"meaning\": \"data_n_01\"}, {\"definition\":    			\"knowledge acquired through study or experience or instruction\",    			\"meaning\": \"information_n_02\"}]}, {\"lemma\": \"management\", \"word\":    			\"management\", \"POS\": \"NN\", \"meanings\": [{\"definition\": \"the act of    			managing something\", \"meaning\": \"management_n_01\"}, {\"definition\":    			\"the act of managing something\", \"meaning\": \"management_n_01\"},    			{\"definition\": \"the act of managing something\", \"meaning\":    			\"management_n_01\"}]}, {\"lemma\": \"skill\", \"word\": \"skills\", \"POS\":    			\"NNS\", \"meanings\": [{\"definition\": \"an ability that has been acquired    			by training\", \"meaning\": \"skill_n_01\"}, {\"definition\": \"an ability    			that has been acquired by training\", \"meaning\": \"skill_n_01\"},    			{\"definition\": \"an ability that has been acquired by training\",    			\"meaning\": \"skill_n_01\"}]}, {\"lemma\": \"in\", \"word\": \"in\", \"POS\": \"IN\",    			\"meanings\": []}, {\"lemma\": \"this\", \"word\": \"this\", \"POS\": \"DT\",    			\"meanings\": []}, {\"lemma\": \"accredit\", \"word\": \"accredited\", \"POS\":    			\"VBN\", \"meanings\": []}, {\"lemma\": \"course\", \"word\": \"course\", \"POS\":    			\"NN\", \"meanings\": [{\"definition\": \"education imparted in a series of    			lessons or meetings\", \"meaning\": \"course_n_01\"}, {\"definition\":    			\"education imparted in a series of lessons or meetings\", \"meaning\":    			\"course_n_01\"}, {\"definition\": \"education imparted in a series of    			lessons or meetings\", \"meaning\": \"course_n_01\"}]}, {\"lemma\": \".\",    			\"word\": \".\", \"POS\": \".\", \"meanings\": []}], \"scores\":    			[0.3665618879810195, 0.3268606553117148, 0.3065774567072656]},    			{\"terms\": [{\"lemma\": \"Online\", \"word\": \"Online\", \"POS\": \"NNP\",    			\"meanings\": [{\"definition\": \"a human being\", \"meaning\":    			\"person_n_01\"}, {\"definition\": \"a human being\", \"meaning\":    			\"person_n_01\"}, {\"definition\": \"a point or extent in space\",    			\"meaning\": \"location_n_01\"}]}, {\"lemma\": \"course\", \"word\": \"course\",    			\"POS\": \"NN\", \"meanings\": [{\"definition\": \"education imparted in a    			series of lessons or meetings\", \"meaning\": \"course_n_01\"},    			{\"definition\": \"education imparted in a series of lessons or    			meetings\", \"meaning\": \"course_n_01\"}, {\"definition\": \"education    			imparted in a series of lessons or meetings\", \"meaning\":    			\"course_n_01\"}]}, {\"lemma\": \"deliver\", \"word\": \"delivered\", \"POS\":    			\"VBN\", \"meanings\": []}, {\"lemma\": \"by\", \"word\": \"by\", \"POS\": \"IN\",    			\"meanings\": []}, {\"lemma\": \"Curtin_University\", \"word\":    			\"Curtin_University\", \"POS\": \"NNP\", \"meanings\": [{\"definition\": \"a    			large and diverse institution of higher learning created to educate    			for life and for a profession and to grant degrees\", \"meaning\":    			\"university_n_03\"}, {\"definition\": \"the body of faculty and students    			at a university\", \"meaning\": \"university_n_01\"}, {\"definition\": \"a    			large and diverse institution of higher learning created to educate    			for life and for a profession and to grant degrees\", \"meaning\":    			\"university_n_03\"}]}, {\"lemma\": \".\", \"word\": \".\", \"POS\": \".\",    			\"meanings\": []}], \"scores\": [0.4230435177574157, 0.3077650993314534,    			0.2691913829111309]}, {\"terms\": [{\"lemma\": \"Learn\", \"word\": \"Learn\",    			\"POS\": \"VB\", \"meanings\": []}, {\"lemma\": \"more\", \"word\": \"more\", \"POS\":    			\"JJR\", \"meanings\": []}, {\"lemma\": \".\", \"word\": \".\", \"POS\": \".\",    			\"meanings\": []}], \"scores\": []}]";
		result = DisambiguationResult.fromJson(jsonResponse);
		
		assertEquals(1, result.getSentences().get(2).getVariants().size());
		assertEquals(3, result.getVariants().size());
	}


	@Test
	public void testVariantsWithEntityTypeDisambiguation() {
		String jsonResponse = "[{\"terms\": [{\"lemma\": \"Steve_Jobs\", \"word\": \"Steve_Jobs\", \"term\": \"Steve Jobs\", \"POS\": \"NNP\", \"meanings\": [{\"definition\": \"a human being\", \"meaning\": \"person_n_01\"}, {\"definition\": \"a workplace; as in the expression 'on the job'; \", \"meaning\": \"job_n_03\"}, {\"definition\": \"A person, institution or place name called 'Steve Jobs'\", \"meaning\": \"Steve_Jobs_n_01\"}]}], \"scores\": [0.33333340921091204, 0.33333334712849727, 0.33333324366059075]}]";
		
		result = DisambiguationResult.fromJson(jsonResponse);
		
		assertEquals(3, result.getSentences().get(0).getVariants().size());
		assertEquals("Steve Jobs", result.getVariants().get(0).toString());
		assertEquals("job_n_03", result.getVariants().get(1).toString());
		assertEquals("Steve_Jobs_n_01", result.getVariants().get(2).toString());
	}
}
