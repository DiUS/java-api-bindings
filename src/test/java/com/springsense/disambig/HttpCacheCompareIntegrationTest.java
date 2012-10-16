package com.springsense.disambig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

public class HttpCacheCompareIntegrationTest {
	private MeaningRecognitionAPI directApi;
	private MeaningRecognitionAPI cachedApi;

	private int totalQueriesMade = 0;
	private int totalSentenceCount = 0;
	private long totalDirectTimeMs = 0;
	private long totalPrimingTimeMs = 0;
	private long totalCachedTimeMs = 0;

	@Before
	public void setUp() {
		directApi = new MeaningRecognitionAPI("http://192.168.0.96:8989/disambiguate", null, null, null);
		cachedApi = new MeaningRecognitionAPI("http://192.168.0.96:88/disambiguate", null, null, null);
	}

	//@Test
	public void testRecognize() throws Exception {
		verifyCachedIsSame("black box");
		verifyCachedIsSame("cat vet");

		List<String> pages = testPages();

		for (String page : pages) {
			verifyCachedIsSame(page);
		}

		System.out.printf("Total Queries: %d\n", totalQueriesMade);
		System.out.printf("Total Sentences: %d\n", totalSentenceCount);
		System.out.printf("Total Direct Time: %dms.\tDirect QpS: %g q/s.\tDirect Sentences p/sec: %g\n", totalDirectTimeMs, (double) totalQueriesMade
				/ ((double) totalDirectTimeMs / 1000.0), (double) totalSentenceCount / ((double) totalDirectTimeMs / 1000.0));
		System.out.printf("Total Priming Time: %dms.\tPriming QpS: %g q/s.\tPriming Sentences p/sec: %g\n", totalPrimingTimeMs, (double) totalQueriesMade
				/ ((double) totalPrimingTimeMs / 1000.0), (double) totalSentenceCount / ((double) totalPrimingTimeMs / 1000.0));
		System.out.printf("Total Cached Time: %dms.\tCached QpS: %g q/s.\tCached Sentences p/sec: %g\n", totalCachedTimeMs, (double) totalQueriesMade
				/ ((double) totalCachedTimeMs / 1000.0), (double) totalSentenceCount / ((double) totalCachedTimeMs / 1000.0));
	}

	private List<String> testPages() throws IOException {
		List<String> lines = IOUtils.readLines(HttpCacheCompareIntegrationTest.class.getClassLoader().getResourceAsStream(
				"com/springsense/disambig/random_sentences.txt"));
		Collections.shuffle(lines);

		List<String> pages = new LinkedList<String>();
		
		int i = 0;
		int linesInPage = 200;

		StringBuilder pageBuilder = new StringBuilder();
		for (String line : lines) {
			String[] sentences = line.split("[\\?\\.\n\\!]");

			for (String s : sentences) {
				if (s.trim().length() > 0) {
					String newS = shuffleSentence(s).trim();
					pageBuilder.append(newS);
					pageBuilder.append('\n');
					i++;

					int lineInPage = (i % linesInPage);
					System.out.printf("%d\t(%d/%d):\t%s\n", i, lineInPage, linesInPage, newS);

					if (lineInPage == 0) {
						String page = pageBuilder.toString().trim();
						if (page.length() > 0) {
							pages.add(page);
							pageBuilder.setLength(0);
						}
					}
				}
			}
		}

		totalSentenceCount += i;

		String lastPage = pageBuilder.toString().trim();
		if (lastPage.length() > 0) {
			pages.add(lastPage);
		}

		return pages;
	}

	private String shuffleSentence(String s) {
		ArrayList<String> words = new ArrayList<String>(Arrays.asList(s.split("\\s")));
		Collections.shuffle(words);
		String newS = StringUtils.join(words, ' ');
		return newS;
	}

	protected void verifyCachedIsSame(String textToRecognize) throws Exception {
		System.out.printf("Comparing %d...\n", (totalQueriesMade + 1));
		// System.out.printf("Will verify:\n-------\n%s\n----------\n",
		// textToRecognize);

		long beforeDirectMs = System.currentTimeMillis();
		DisambiguationResult directResult1 = directApi.recognize(textToRecognize);
		long afterDirectMs = System.currentTimeMillis();

		long beforePrimingMs = System.currentTimeMillis();
		DisambiguationResult primeCacheResult1 = cachedApi.recognize(textToRecognize);
		long afterPrimingMs = System.currentTimeMillis();

		long beforeCachedMs = System.currentTimeMillis();
		DisambiguationResult cachedResult2 = cachedApi.recognize(textToRecognize);
		long afterCachedMs = System.currentTimeMillis();

		totalDirectTimeMs += (afterDirectMs - beforeDirectMs);
		totalPrimingTimeMs += (afterPrimingMs - beforePrimingMs);
		totalCachedTimeMs += (afterCachedMs - beforeCachedMs);
		totalQueriesMade++;

		boolean okay = verifyMostlyEquals("dr1", directResult1, "cr1", primeCacheResult1) && verifyMostlyEquals("dr1", directResult1, "cr2", cachedResult2);

		assertTrue("Invocation results must be 95% equal.", okay);
	}

	@Test
	public void testPercentDifference() throws IOException {
		assertEquals(0.0, percentLinesDifference("1\n2\n3\n4\n5\n", "1\n2\n3\n4\n5\n"), 0.01);
		assertEquals(1.0, percentLinesDifference("a\nb\nc\nd\ne\n", "1\n2\n3\n4\n5\n"), 0.01);
		assertEquals(0.5, percentLinesDifference("a\nb\nc\n4\n5\n6", "1\n2\n3\n4\n5\n6"), 0.01);
		assertEquals(0.2, percentLinesDifference("a\n2\n3\n4\n5\n", "1\n2\n3\n4\n5\n"), 0.01);
		assertEquals(0.2, percentLinesDifference("1\n2\n3\n4\n5\n", "1\n2\n3\n4\n"), 0.01);
		assertEquals(0.4, percentLinesDifference("1\n2\n3\nd\n", "1\n2\n3\n4\n5\n"), 0.01);
	}
	
	@Test
	public void testVerifyShuffle() {
		String sentence = "it's the end of the world as we know it. and I feel fine.";
		assertFalse(sentence.equals(shuffleSentence(sentence)));
	}

	private double percentLinesDifference(String left, String right) throws IOException {
		if (left.equals(right)) {
			return 0.0;
		}

		List<String> leftLines = IOUtils.readLines(new StringReader(left));
		List<String> rightLines = IOUtils.readLines(new StringReader(right));

		Iterator<String> leftLineIterator = leftLines.iterator();
		Iterator<String> rightLineIterator = rightLines.iterator();

		int differentLinesCount = 0;
		while (leftLineIterator.hasNext()) {
			String leftLine = leftLineIterator.next();
			if (!rightLineIterator.hasNext()) {
				break;
			}

			String rightLine = rightLineIterator.next();
			if (!leftLine.equals(rightLine)) {
				differentLinesCount++;
			}
		}

		differentLinesCount += Math.abs(leftLines.size() - rightLines.size());

		return (double) differentLinesCount / (double) Math.max(leftLines.size(), rightLines.size());
	}

	private boolean verifyMostlyEquals(String descLeft, Object left, String descRight, Object right) throws Exception {
		double percentLinesDifference = percentLinesDifference(left.toString(), right.toString());
		// System.out.printf("%s != %s by %g difference.\n", descLeft,
		// descRight, percentLinesDifference);
		if (percentLinesDifference >= 0.95) {
			return false;
		}

		return true;
	}
}
