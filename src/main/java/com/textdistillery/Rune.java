package com.textdistillery;


import com.textdistillery.db.jpa.DB;
import edu.mit.jwi.item.POS;

import java.util.List;
import java.util.regex.Pattern;

public class Rune {
    public static final String PUNCT_MARKS = ".?!;:—,–[]{}()-'\"&";

    public enum Type {
        NONE(""),
        WORD("word"),
        NUMBER("number"),
        PUNCTUATION("punctuation");
        final String label;
        Type(String label) {this.label = label;}
        public String toString() {return label;}
    }
    public enum Case {
        NONE(""),
        LOWERCASE("lowercase"),
        CAPITALIZED("capitalized"),
        ALL_CAPS("all caps"),
        CAMEL_CASE("camel case");
        final String label;
        Case(String label) {this.label = label;}
        public String toString() {return label;}
    }
    public enum Pos {
        NONE(""),
        NOUN("noun"),
        VERB("verb"),
        ADJECTIVE("adjective"),
        ADVERB("adverb"),
        PRONOUN("pronoun"),
        DETERMINER("determiner"),
        PREPOSITION("preposition"),
        CONJUNCTION("conjunction"),
        INTERJECTION("interjection");
        final String label;
        Pos(String label) {this.label = label;}
        public String toString() {return label;}
    }
    public enum Inf {
        NONE(""),
        //nouns
        SINGULAR("singular"),
        PLURAL("plural"),
        //verbs
        INFINITIVE("infinitive"),
        PRESENT_PARTICIPLE("present participle"),
        PAST("simple past"),
        PAST_PARTICIPLE("past participle"),
        THIRD_PS_SING("3rd person singular"),
        //adjectives and adverbs
        POSITIVE("positive"),
        COMPARATIVE("comparative"),
        SUPERLATIVE("superlative");
        final String label;
        Inf(String label) {this.label = label;}
        public String toString() {return label;}
    }

    private String inputToken;
    private String normToken; private int normTokenFreq;
    private String lemma; private int lemmaFreq;
    private String tag;
    private String synonym; private int synonymFreq;
    private String infSynonym;
    private String outputSynonym;

    private Type typeEnum;
    private Case caseEnum;
    private Pos posEnum;
    private Inf infEnum;

    public Rune(String token, DB db, Wordnet wordnet) throws Exception {
        inputToken = token;
        normToken = "";
        lemma = "";
        tag = "";
        synonym = "";
        infSynonym = "";
        outputSynonym = inputToken;
        typeEnum = Type.NONE;
        caseEnum = Case.NONE;
        posEnum = Pos.NONE;
        infEnum = Inf.NONE;

        typeEnum = findType(inputToken);
        if (typeEnum == Type.WORD) {
            caseEnum = findCase(inputToken);
            normToken = normalizeWord(inputToken);
            if (findFrequency(normToken) >= Config.MIN_FREQ && caseEnum != Case.ALL_CAPS && caseEnum != Case.CAMEL_CASE) {
                lemma = db.findLemma(normToken);
                tag = db.findTag(normToken);
                setPOSInflection(tag);
                if (posEnum == Pos.NOUN || posEnum == Pos.VERB || posEnum == Pos.ADJECTIVE || posEnum == Pos.ADVERB) {
                    if (!(tag.equals("NNP") || tag.equals("NNPS"))) {
                        POS p = POS.getPartOfSpeech(posEnum.ordinal());
                        List<String> synonymList = wordnet.findBestSynonym(lemma, p);
                        /*if (!synonymList.isEmpty()) {
                            synonym = synonymList.get(0);
                            infSynonym = db.searchByTag(synonym, tag);
                            if (!infSynonym.equals(""))
                                outputSynonym = retriveCase(infSynonym, caseEnum);
                        }*/
                        int shortest = lemma.length();
                        for (String word:synonymList) {
                            if (word.length() < shortest) {
                                synonym = word;
                                shortest = word.length();
                            }
                        }
                        if (synonym.contains("_")) {

                        }
                        infSynonym = db.searchByTag(synonym, tag);
                        if (!infSynonym.equals(""))
                            outputSynonym = retriveCase(infSynonym, caseEnum);
                    }
                }
            }
        }
        System.out.println(inputToken + " -> " + outputSynonym);
    }

    private String normalizeWord(String token) {
        return token.toLowerCase();
    }

    private Type findType(String token) {
        String punct = Pattern.quote(PUNCT_MARKS);
        if (Pattern.matches("[" + punct + "]", inputToken)) {
            return Type.PUNCTUATION;
        } else if (Pattern.matches("\\d+", inputToken)) {
            return Type.NUMBER;
        } else {
            return Type.WORD;
        }
    }

    private Case findCase(String token) {
        if (Pattern.matches("[A-Z]+", token)) {
            return Case.ALL_CAPS;
        } else if (Pattern.matches("\\b[A-Z].*?\\b", token)) {
            return Case.CAPITALIZED;
        } else if (Pattern.matches("[a-z]+", token)) {
            return Case.LOWERCASE;
        } else {
            return Case.NONE;
        }
    }

    private String retriveCase(String word, Case c) {
        if (c == Case.CAPITALIZED) {
            return word.substring(0, 1).toUpperCase() + word.substring(1);
        }
        return word;
    }

    private void setPOSInflection(String tag) {
        switch (tag) {
            case "NN"://Noun, singular or mass: bicycle, earthquake, zipper
            case "NNP"://Proper noun, singular: Denver, DORAN, Alexandra
            case "NN:UN"://Nouns that might be used in the plural form and with an indefinite article, depending on their meaning: establishment, wax, afternoon
                posEnum = Pos.NOUN;
                infEnum = Inf.SINGULAR;
                break;
            case "NNS"://Noun, plural: bicycles, earthquakes, zippers
            case "NNPS"://Proper noun, plural: Buddhists, Englishmen
            case "NN:U"://Nouns that are always uncountable: admiration, Afrikaans
                posEnum = Pos.NOUN;
                infEnum = Inf.PLURAL;
                break;
            case "VB"://Verb, base form: eat, jump, believe, be, have
            case "VBP"://non-3rd ps. sing. present: eat, jump, believe, am (as in 'I am'), are
                posEnum = Pos.VERB;
                infEnum = Inf.INFINITIVE;
                break;
            case "VBG"://Verb, gerund/present participle: eating, jumping, believing
                posEnum = Pos.VERB;
                infEnum = Inf.PRESENT_PARTICIPLE;
                break;
            case "VBD"://Verb, past tense: ate, jumped, believed
                posEnum = Pos.VERB;
                infEnum = Inf.PAST;
                break;
            case "VBN"://past participle: eaten, jumped, believed
                posEnum = Pos.VERB;
                infEnum = Inf.PAST_PARTICIPLE;
                break;
            case "VBZ"://Verb, 3rd ps. sing. present: eats, jumps, believes, is, has
                posEnum = Pos.VERB;
                infEnum = Inf.THIRD_PS_SING;
                break;
            case "JJ"://Adjective: beautiful, large, inspectable
                posEnum = Pos.ADJECTIVE;
                infEnum = Inf.POSITIVE;
                break;
            case "JJR"://Adjective, comparative: larger, quicker
                posEnum = Pos.ADJECTIVE;
                infEnum = Inf.COMPARATIVE;
                break;
            case "JJS"://Adjective, superlative: largest, quickest
                posEnum = Pos.ADJECTIVE;
                infEnum = Inf.SUPERLATIVE;
                break;
            case "RB"://Adverb and negation: easily, sunnily, suddenly, specifically, not
                posEnum = Pos.ADVERB;
                infEnum = Inf.POSITIVE;
                break;
            case "RBR"://Adverb, comparative: better, faster, quicker
                posEnum = Pos.ADVERB;
                infEnum = Inf.COMPARATIVE;
                break;
            case "RBS"://Adverb, superlative: best, fastest, quickest
                posEnum = Pos.ADVERB;
                infEnum = Inf.SUPERLATIVE;
                break;
            default:
                posEnum = Pos.NONE;
                infEnum = Inf.NONE;
        }
        //case ""://
    }

    private int findFrequency(String token) {
        return 500;
    }

    public String getInputToken() {return inputToken;}
    public String getNormToken() {return normToken;}
    public String getLemma() {return lemma;}
    public String getSynonym() {return synonym;}
    public String getInfSynonym() {return infSynonym;}
    public String getOutputSynonym() {return outputSynonym;}

    public Type getTypeEnum() {return typeEnum;}
    public Case getCaseEnum() {return caseEnum;}
    public Pos getPosEnum() {return posEnum;}
    public Inf getInfEnum() {return infEnum;}
}
