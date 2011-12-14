package com.springsense.disambig;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * A dismabiguation result. Returned from a meaningRecognitionAPI.recognize(...)
 * call.
 */
public class DisambiguationResult implements Serializable {

	private static final long serialVersionUID = 1L;
	private List<Sentence> sentences = null;
	private List<Variant> variants = null;

	static DisambiguationResult fromJson(String json) {
		List<Sentence> sentences = new Gson().fromJson(json, new TypeToken<List<Sentence>>() {
		}.getType());

		DisambiguationResult result = new DisambiguationResult();
		result.setSentences(sentences);

		return result;
	}

	protected DisambiguationResult() {
	}

	public DisambiguationResult(List<Sentence> sentences) {
		super();
		this.sentences = sentences;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for (Sentence s : getSentences()) {
			sb.append(s.toString());
			sb.append('\n');
		}

		return sb.toString().trim();
	}
	/**
	 * Returns the sentences that comprise the disambiguation result
	 * 
	 * @return A list of sentences that comprise the disambiguation result
	 */
	public List<Sentence> getSentences() {
		return sentences;
	}

	public void setSentences(List<Sentence> sentences) {
		this.sentences = sentences;
	}

	/**
	 * Returns the result as a list of variants, each having multiple variant
	 * sentences
	 * 
	 * @return The result as a list of variants, each having multiple variant
	 *         sentences
	 */
	public List<Variant> getVariants() {
		if (variants == null) {
			int maxNumberOfVariants = getHighestNumberOfVariants();

			VariantSentence[][] variantMatrix = new VariantSentence[maxNumberOfVariants][sentences.size()];

			for (int s = 0; s < sentences.size(); s++) {
				Sentence sentence = sentences.get(s);
				for (int v = 0; v < maxNumberOfVariants; v++) {
					List<VariantSentence> sentenceVariants = sentence.getVariants();
					int sizeOfSentenceVariants = sentenceVariants.size();

					VariantSentence variantSentence = sentenceVariants.get(v < sizeOfSentenceVariants ? v : 0);

					variantMatrix[v][s] = variantSentence;
				}
			}

			variants = new ArrayList<Variant>(variantMatrix.length);
			for (int v = 0; v < maxNumberOfVariants; v++) {
				variants.add(new Variant(Arrays.asList(variantMatrix[v])));
			}

		}
		return Collections.unmodifiableList(variants);
	}

	/**
	 * Returns the highest number of variants in the result sentences.
	 * 
	 * @return The highest number of variants in the result sentences.
	 */
	protected int getHighestNumberOfVariants() {
		int maxNumberOfVariants = 0;
		for (Sentence sentence : sentences) {
			List<VariantSentence> sentenceVariants = sentence.getVariants();

			maxNumberOfVariants = Math.max(maxNumberOfVariants, sentenceVariants.size());
		}
		return maxNumberOfVariants;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sentences == null) ? 0 : sentences.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		DisambiguationResult other = (DisambiguationResult) obj;
		if (sentences == null) {
			if (other.sentences != null) {
				return false;
			}
		} else if (!sentences.equals(other.sentences)) {
			return false;
		}
		return true;
	}

	/**
	 * A disambiguated sentence
	 */
	public static class Sentence implements Serializable {

		private static final long serialVersionUID = 1L;

		private double[] scores;
		private List<Term> terms;
		private List<VariantSentence> variants;

		public Sentence() {
		}

		/**
		 * Returns the scores for the various variants in the sentence
		 * 
		 * @return An array of doubles containing the normalised (to 1.0) score
		 *         breakdown of the potential disambiguation variants for the
		 *         sentence
		 */
		public double[] getScores() {
			return scores;
		}

		public void setScores(double[] scores) {
			this.scores = scores;
		}

		/**
		 * Returns the list of terms comprising the disambiguated sentence
		 * 
		 * @return The list of terms comprising the disambiguated sentence
		 */
		public List<Term> getTerms() {
			return terms;
		}

		public void setTerms(List<Term> terms) {
			this.terms = terms;
		}

		/**
		 * Returns the individual disambiguated variants for the sentence
		 * 
		 * @return A list of the individual disambiguated variants for the
		 *         sentence
		 */
		public List<VariantSentence> getVariants() {
			if (variants == null) {
				variants = calculateVariants();
			}
			return variants;
		}

		private List<VariantSentence> calculateVariants() {
			int scoresCount = scores.length;
			final int cardinality = Math.max(scoresCount, 1);
			List<VariantSentence> variants = new ArrayList<VariantSentence>(cardinality);
			for (int i = 0; i < cardinality; i++) {
				variants.add(new VariantSentence(i < scoresCount ? scores[i] : 1.0, new ArrayList<ResolvedTerm>(getTerms().size())));
			}
			for (Term term : getTerms()) {
				List<ResolvedTerm> resolvedTermsForTerm = termToResolvedTerms(term, cardinality);
				for (int i = 0; i < cardinality; i++) {
					variants.get(i).getTerms().add(resolvedTermsForTerm.get(i));
				}
			}
			return variants;
		}

		private List<ResolvedTerm> termToResolvedTerms(Term term, int cardinality) {
			List<ResolvedTerm> resolvedTerms = new ArrayList<ResolvedTerm>(cardinality);
			if (term.getMeanings().size() < 1) {
				ResolvedTerm termWithNoMeanings = new ResolvedTerm(term, null, 1.0);

				for (int i = 0; i < cardinality; i++) {
					resolvedTerms.add(termWithNoMeanings);
				}
			} else {
				Map<String, ResolvedTerm> resolvedTermsByMeaning = new HashMap<String, ResolvedTerm>();

				for (int i = 0; i < cardinality; i++) {
					Meaning meaning = term.getMeanings().get(i % term.getMeanings().size());
					final String meaningToken = meaning.getMeaning();
					ResolvedTerm resolvedTerm = resolvedTermsByMeaning.get(meaningToken);

					if (resolvedTerm == null) {
						// New one
						resolvedTerm = new ResolvedTerm(term, meaning, getScores()[i]);
						resolvedTermsByMeaning.put(meaningToken, resolvedTerm);
					} else {
						resolvedTerm.setScore(resolvedTerm.getScore() + getScores()[i]);
					}

					resolvedTerms.add(resolvedTerm);
				}
			}

			return resolvedTerms;
		}

		
		@Override
		public String toString() {
			return String.format("'sentence':['terms':%s,'scores':%s]", terms, Arrays.toString(scores));
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(scores);
			result = prime * result + ((terms == null) ? 0 : terms.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			Sentence other = (Sentence) obj;
			if (scores.length != other.scores.length) {
				return false;
			}
			
			double tolerance = 0.001;
			for (int i = 0; i < scores.length; i++) {
				if (Math.abs(scores[i] - other.scores[i]) > tolerance) {
					return false;
				}
			}

			if (terms == null) {
				if (other.terms != null) {
					return false;
				}
			} else if (!terms.equals(other.terms)) {
				return false;
			}
			return true;
		}

	}

	/**
	 * A disambiguated variant sentence
	 */
	public static class VariantSentence implements Serializable {

		/**
         *
         */
		private static final long serialVersionUID = 1L;
		private double score;
		private List<ResolvedTerm> terms;

		protected VariantSentence(double score, List<ResolvedTerm> terms) {
			this.score = score;
			this.terms = terms;
		}

		/**
		 * Returns the probable score for the variant sentence
		 * 
		 * @return A double which is the probable score for the variant sentence
		 */
		public double getScore() {
			return score;
		}

		/**
		 * Returns the disambiguated terms comprising this variant
		 * 
		 * @return A list of disambiguated terms comprising this variant
		 */
		public List<ResolvedTerm> getTerms() {
			return terms;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();

			for (ResolvedTerm term : getTerms()) {
				sb.append(term.toString());
				sb.append(' ');
			}

			return sb.toString().trim();
		}
	}

	/**
	 * Encapsulates resolved term, a specific meaning which is part of a
	 * specific variant of a disambiguated sentence
	 */
	public static class ResolvedTerm implements Serializable {

		private static final long serialVersionUID = 1L;
		private Term originalTerm;
		private Meaning meaning;
		private double score;

		protected ResolvedTerm(Term originalTerm, Meaning meaning, double score) {
			this.originalTerm = originalTerm;
			this.meaning = meaning;
			this.score = score;
		}

		public String getWord() {
			return getOriginalTerm().getWord();
		}

		public Meaning getMeaning() {
			return meaning;
		}

		public Term getOriginalTerm() {
			return originalTerm;
		}

		public double getScore() {
			return score;
		}

		public void setScore(double score) {
			this.score = score;
		}

		@Override
		public String toString() {
			if (getMeaning() != null) {
				if (getMeaning().isEntityType()) {
					return getWord().replaceAll("_", " ");
				}
				return getMeaning().getMeaning();
			} else {
				return getWord();
			}
		}
	}

	/**
	 * This class encapsulates a term, potentially with multiple meanings
	 */
	public static class Term implements Serializable {

		private static final long serialVersionUID = 1L;
		private String lemma;
		private String word;
		private String term;
		private String POS;
		private int offset;
		private List<Meaning> meanings;

		protected Term() {
		}

		public Term(String lemma, String word, String POS, String term, int offset, List<Meaning> meanings) {
			super();
			this.lemma = lemma;
			this.word = word;
			this.POS = POS;
			this.term = term;
			this.offset = offset;
			this.meanings = meanings;
		}

		/**
		 * Returns the part-of-speech as determined by the API
		 * 
		 * @return The part-of-speech identifier for the term
		 */
		public String getPOS() {
			return POS;
		}

		protected void setPOS(String POS) {
			this.POS = POS;
		}

		/**
		 * Returns the lemmatized form of the term
		 * 
		 * @return A string, the lemmatized form of the term
		 */
		public String getLemma() {
			return lemma;
		}

		protected void setLemma(String lemma) {
			this.lemma = lemma;
		}

		/**
		 * Returns the multiple potential meanings of the term if the API
		 * determined them
		 * 
		 * @return A list of the multiple potential meanings for the term
		 */
		public List<Meaning> getMeanings() {
			return meanings;
		}

		protected void setMeanings(List<Meaning> meanings) {
			this.meanings = meanings;
		}

		/**
		 * Returns the original form of the term
		 * 
		 * @return The original form of the term
		 */
		public String getTerm() {
			return term;
		}

		protected void setTerm(String term) {
			this.term = term;
		}

		/**
		 * Returns the offset in the original text of the term
		 * 
		 * @return The offset of the term
		 */
		public int getTermOffset() {
			return offset;
		}

		protected void setTermOffset(int offset) {
			this.offset = offset;
		}

		/**
		 * Returns the form of the term used by the meaning recognition engine
		 * 
		 * @return The form of the term used for meaning recognition
		 */
		public String getWord() {
			return word;
		}

		protected void setWord(String word) {
			this.word = word;
		}


		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((POS == null) ? 0 : POS.hashCode());
			result = prime * result + ((lemma == null) ? 0 : lemma.hashCode());
			result = prime * result + ((meanings == null) ? 0 : meanings.hashCode());
			result = prime * result + offset;
			result = prime * result + ((term == null) ? 0 : term.hashCode());
			result = prime * result + ((word == null) ? 0 : word.hashCode());
			return result;
		}

		@Override
		public String toString() {
			return String.format("'term':{word:'%s', term:'%s', lemma:'%s', POS:'%s', meanings:%s, offset:%s}", word, term, lemma, POS, meanings, offset);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			Term other = (Term) obj;
			if (POS == null) {
				if (other.POS != null) {
					return false;
				}
			} else if (!POS.equals(other.POS)) {
				return false;
			}
			if (lemma == null) {
				if (other.lemma != null) {
					return false;
				}
			} else if (!lemma.equals(other.lemma)) {
				return false;
			}
			if (meanings == null) {
				if (other.meanings != null) {
					return false;
				}
			} else {
				List<Meaning> sortedMeanings = new ArrayList<Meaning>(meanings);
				List<Meaning> sortedOtherMeanings = new ArrayList<Meaning>(other.meanings);
				
				Comparator<Meaning> meaningComp = new Comparator<Meaning>() {

					@Override
					public int compare(Meaning left, Meaning right) {
						return left.getMeaning().compareTo(right.getMeaning());
					}
					
				};
				
				Collections.sort(sortedMeanings, meaningComp);
				Collections.sort(sortedOtherMeanings, meaningComp);
				
				if (!sortedMeanings.equals(sortedOtherMeanings)) {
					return false;
				}
			}
			if (offset != other.offset) {
				return false;
			}
			if (term == null) {
				if (other.term != null) {
					return false;
				}
			} else if (!term.equals(other.term)) {
				return false;
			}
			if (word == null) {
				if (other.word != null) {
					return false;
				}
			} else if (!word.equals(other.word)) {
				return false;
			}
			return true;
		}

	}

	/**
	 * One potential meaning of a term, with its definition
	 */
	public static class Meaning implements Serializable {

		private static final long serialVersionUID = 1L;
		private String definition;
		private String meaning;

		protected Meaning() {
		}

		public Meaning(String meaning, String definition) {
			super();
			this.definition = definition;
			this.meaning = meaning;
		}

		/**
		 * Returns the definition of the meaning, according to WordNet
		 * 
		 * @return The definition of the meaning, according to WordNet
		 */
		public String getDefinition() {
			return definition;
		}

		protected void setDefinition(String definition) {
			this.definition = definition;
		}

		/**
		 * Returns the meaning token for the meaning, according to the WordNet
		 * conventions
		 * 
		 * @return The meaning token for the meaning, according to the WordNet
		 *         conventions
		 */
		public String getMeaning() {
			return meaning;
		}

		protected void setMeaning(String meaning) {
			this.meaning = meaning;
		}

		public boolean isEntityType() {
			return (("person_n_01".equals(getMeaning())) || ("association_n_01".equals(getMeaning())) || ("location_n_01".equals(getMeaning())));

		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((definition == null) ? 0 : definition.hashCode());
			result = prime * result + ((meaning == null) ? 0 : meaning.hashCode());
			return result;
		}

		@Override
		public String toString() {
			return String.format("'meaning':{'meaning':'%s','definition':'%s'}", meaning, definition);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			Meaning other = (Meaning) obj;
			if (definition == null) {
				if (other.definition != null) {
					return false;
				}
			} else if (!definition.equals(other.definition)) {
				return false;
			}
			if (meaning == null) {
				if (other.meaning != null) {
					return false;
				}
			} else if (!meaning.equals(other.meaning)) {
				return false;
			}
			return true;
		}

	}

	public static class Variant implements Serializable {
		private static final long serialVersionUID = 1L;

		List<VariantSentence> sentences;

		protected Variant(List<VariantSentence> sentences) {
			super();
			this.sentences = sentences;
		}

		public List<VariantSentence> getSentences() {
			return Collections.unmodifiableList(sentences);
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();

			for (VariantSentence s : getSentences()) {
				sb.append(s.toString());
				sb.append(' ');
			}

			return sb.toString().trim();
		}
	}
}
