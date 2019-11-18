package com.textdistillery;

import edu.mit.jwi.IRAMDictionary;
import edu.mit.jwi.RAMDictionary;
import edu.mit.jwi.data.ILoadPolicy;
import edu.mit.jwi.item.*;
import edu.mit.jwi.morph.WordnetStemmer;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Wordnet implements Closeable {

    IRAMDictionary dict;

    public static void main( String[] args ) throws IOException {
        try (Wordnet wordnet = new Wordnet()) {
            String word = "victim";
            String lemma = word;
            //lemma = wordnet.findLemma(word);
            System.out.println(word + " -> " + lemma + "\n");
            wordnet.search(lemma, POS.NOUN);
        }
    }

    public Wordnet() throws IOException {
        URL url = new URL("file", null, Config.WORDNET_PATH);
        dict = new RAMDictionary(url, ILoadPolicy.NO_LOAD);
        dict.open();
    }

    public String findLemma(String word) {
        WordnetStemmer stemmer = new WordnetStemmer(dict);
        List<String> lemma = stemmer.findStems(word,null);
        if (!lemma.isEmpty()) {
            return lemma.get(0);
        } else {
            return null;
        }
    }

    public void search(String search, POS type) {
        try {
            IIndexWord index = dict.getIndexWord(search,type);
            List<IWordID> idlist = index.getWordIDs();
            for (int i=0; i<idlist.size(); i++) {
                IWord sense = dict.getWord(idlist.get(i));
                ISynset synset = sense.getSynset();
                System.out.println("Lemma: " + sense.getLemma());
                System.out.println("Definition: " + synset.getGloss());
                System.out.print("Synonyms: ");
                for (IWord w : synset.getWords()){
                    System.out.print(w.getLemma() + " ");
                }
                System.out.println("\n");
            }
        } catch (NullPointerException e) {
            System.out.println("Nothing found for " + search);
        }
    }

    public List<String> findBestSynonym(String search, POS type) {
        List<String> synonyms = new ArrayList<>();
        try {
            IIndexWord index = dict.getIndexWord(search,type);
            List<IWordID> idlist = index.getWordIDs();
            for (int i=0; i<idlist.size(); i++) {
                IWord sense = dict.getWord(idlist.get(i));
                ISynset synset = sense.getSynset();
                for (IWord w : synset.getWords()){
                    if (!w.getLemma().equals(search))
                        synonyms.add(w.getLemma());
                }
            }
            return synonyms;
        } catch (Exception e) {
            return synonyms;
        }
    }

    public void close() {
        dict.close();
    }
}
