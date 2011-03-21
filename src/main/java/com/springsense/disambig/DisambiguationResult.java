package com.springsense.disambig;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A dismabiguation result
 */
class DisambiguationResult {

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

    public List<Sentence> getSentences() {
        return sentences;
    }

    public void setSentences(List<Sentence> sentences) {
        this.sentences = sentences;
    }

    protected static class Sentence {

        private double[] scores;
        private List<Term> terms;
        private List<VariantSentence> variants;

        public double[] getScores() {
            return scores;
        }

        public void setScores(double[] scores) {
            this.scores = scores;
        }

        public List<Term> getTerms() {
            return terms;
        }

        public void setTerms(List<Term> terms) {
            this.terms = terms;
        }

        List<VariantSentence> getVariants() {
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
                        resolvedTermsByMeaning.put( meaningToken, resolvedTerm);
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

    protected static class VariantSentence {

        private double score;
        private List<ResolvedTerm> terms;

        VariantSentence(double score, List<ResolvedTerm> terms) {
            this.score = score;
            this.terms = terms;
        }

        public double getScore() {
            return score;
        }

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

    protected static class ResolvedTerm {

        private Term originalTerm;
        private Meaning meaning;
        private double score;

        ResolvedTerm(Term originalTerm, Meaning meaning, double score) {
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

    protected static class Term {

        private String lemma;
        private String word;
        private String POS;
        private List<Meaning> meanings;

        public String getPOS() {
            return POS;
        }

        public void setPOS(String POS) {
            this.POS = POS;
        }

        public String getLemma() {
            return lemma;
        }

        public void setLemma(String lemma) {
            this.lemma = lemma;
        }

        public List<Meaning> getMeanings() {
            return meanings;
        }

        public void setMeanings(List<Meaning> meanings) {
            this.meanings = meanings;
        }

        public String getWord() {
            return word;
        }

        public void setWord(String word) {
            this.word = word;
        }

        @Override
        public String toString() {
            return getWord();
        }


    }

    protected static class Meaning {

        private String definition;
        private String meaning;

        public String getDefinition() {
            return definition;
        }

        public void setDefinition(String definition) {
            this.definition = definition;
        }

        public String getMeaning() {
            return meaning;
        }

        public void setMeaning(String meaning) {
            this.meaning = meaning;
        }
    }
}
