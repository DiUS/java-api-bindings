package com.springsense.disambig;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.List;

/**
 * A dismabiguation result
 */
class DisambiguationResult {

    private List<Sentence> sentences;


    static DisambiguationResult fromJson(String json) {
        List<Sentence> sentences = new Gson().fromJson(json, new TypeToken<List<Sentence>>(){}.getType());

        DisambiguationResult result = new DisambiguationResult();
        result.setSentences(sentences);

        return result;
    }

    private DisambiguationResult() {}

    public List<Sentence> getSentences() {
        return sentences;
    }

    public void setSentences(List<Sentence> sentences) {
        this.sentences = sentences;
    }

    protected static class Sentence {

        private double[] scores;
        private List<Term> terms;

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
