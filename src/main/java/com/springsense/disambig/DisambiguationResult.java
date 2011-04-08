package com.springsense.disambig;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * A dismabiguation result. Returned from a meaningRecognitionAPI.recognize(...) call.
 */
public class DisambiguationResult implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private List<Sentence> sentences;

    static DisambiguationResult fromJson(String json) {
        List<Sentence> sentences = new Gson().fromJson(json, new TypeToken<List<Sentence>>() {
        }.getType());

        DisambiguationResult result = new DisambiguationResult();
        result.setSentences(sentences);

        return result;
    }

    private DisambiguationResult() {
    }

    /**
     * Returns the sentences that comprise the disambiguation result
     * @return A list of sentences that comprise the disambiguation result
     */
    public List<Sentence> getSentences() {
        return sentences;
    }

    public void setSentences(List<Sentence> sentences) {
        this.sentences = sentences;
    }

    /**
     * A disambiguated sentence
     */
    public static class Sentence implements Serializable {

        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private double[] scores;
        private List<Term> terms;
        private List<VariantSentence> variants;

        protected Sentence() {
        }

        /**
         * Returns the scores for the various variants in the sentence
         * @return An array of doubles containing the normalised (to 1.0) score breakdown of the potential disambiguation variants for the sentence
         */
        public double[] getScores() {
            return scores;
        }

        public void setScores(double[] scores) {
            this.scores = scores;
        }

        /**
         * Returns the list of terms comprising the disambiguated sentence
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
         * @return A list of the individual disambiguated variants for the sentence
         */
        public List<VariantSentence> getVariants() {
            if (variants == null) {
                variants = calculateVariants();
            }
            return variants;
        }

        private List<VariantSentence> calculateVariants() {
            final int cardinality = getScores().length;
            List<VariantSentence> variants = new ArrayList<VariantSentence>(cardinality);
            for (int i = 0; i < cardinality; i++) {
                variants.add(new VariantSentence(getScores()[i], new ArrayList<ResolvedTerm>(getTerms().size())));
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
            StringBuilder sb = new StringBuilder();

            for (Term t : getTerms()) {
                sb.append(t.toString());
                sb.append(' ');
            }

            return sb.toString().trim();
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
         * @return A double which is the probable score for the variant sentence
         */
        public double getScore() {
            return score;
        }

        /**
         * Returns the disambiguated terms comprising this variant
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
     * Encapsulates resolved term, a specific meaning which is part of a specific variant of a disambiguated sentence
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
        private String POS;
        private List<Meaning> meanings;

        protected Term() {
        }

        /**
         * Returns the part-of-speech as determined by the API
         * @return
         */
        public String getPOS() {
            return POS;
        }

        protected void setPOS(String POS) {
            this.POS = POS;
        }

        /**
         * Returns the lemmatized form of the term
         * @return A string, the lemmatized form of the term
         */
        public String getLemma() {
            return lemma;
        }

        protected void setLemma(String lemma) {
            this.lemma = lemma;
        }

        /**
         * Returns the multiple potential meanings of the term if the API determined them
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
         * @return The original form of the term
         */
        public String getWord() {
            return word;
        }

        protected void setWord(String word) {
            this.word = word;
        }

        @Override
        public String toString() {
            return getWord();
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

        /**
         * Returns the definition of the meaning, according to WordNet
         * @return The definition of the meaning, according to WordNet
         */
        public String getDefinition() {
            return definition;
        }

        protected void setDefinition(String definition) {
            this.definition = definition;
        }

        /**
         * Returns the meaning token for the meaning, according to the WordNet conventions
         * @return The meaning token for the meaning, according to the WordNet conventions
         */
        public String getMeaning() {
            return meaning;
        }

        protected void setMeaning(String meaning) {
            this.meaning = meaning;
        }
    }
}
