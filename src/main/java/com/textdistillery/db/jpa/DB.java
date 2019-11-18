package com.textdistillery.db.jpa;

import com.textdistillery.Config;

import javax.persistence.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class DB implements AutoCloseable {

    private EntityManagerFactory emf;
    private EntityManager em ;

    public static void main(String[] args) throws Exception{
        try (DB db = new DB()) {
            //db.feedInflectionDict();
            //db.feed1gramZip('a');
            System.out.println(db.getCount("Inflist") + " records");
            String word = "male";
            String result = String.format("%s %s %s", db.findLemma(word), word, db.findTag(word));
            System.out.println(result);
        }
    }

    public DB() {
        emf = Persistence.createEntityManagerFactory(Config.OBJECTDB_PATH);
        em = emf.createEntityManager();
    }

    public long getCount(String name) {
        try {
            Query query = em.createQuery("SELECT COUNT(i) FROM " + name + " i");
            //query.setParameter(1, obj.getSimpleName());
            return Long.parseLong(query.getSingleResult().toString());
        } catch (Exception e) {
            return 0;
        }
    }

    public String findLemma(String word) {
        return searchInflections(word, "lemma");
    }

    public String findTag(String word) {
        return searchInflections(word, "tag");
    }

    private String searchInflections(String word, String col) {
        try {
            TypedQuery<InflistEntity> query = em.createQuery("SELECT i FROM Inflist i WHERE i.inflection = ?1", InflistEntity.class);
            query.setParameter(1, word);
            List<InflistEntity> results = query.getResultList();
            if (!results.isEmpty()) {
                InflistEntity entry = results.get(0);
                if (col.equals("lemma"))
                    return entry.getLemma();
                if (col.equals("tag"))
                    return entry.getTag();
            }
            return "";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public String searchByTag(String lemma, String tag) {
        try {
            TypedQuery<InflistEntity> query = em.createQuery("SELECT i FROM Inflist i WHERE i.lemma = ?1 AND i.tag = ?2", InflistEntity.class);
            query.setParameter(1, lemma);
            query.setParameter(2, tag);
            List<InflistEntity> results = query.getResultList();
            if (!results.isEmpty()) {
                InflistEntity entry = results.get(0);
                return entry.getInflection();
            }
            return "";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public void feedInflectionDict() throws Exception{
        em.getTransaction().begin();
        //original character set is Cp1252 or ISO_8859_1
        String path = "C:/Users/Mikhail/Desktop/Senior_Project/data/inflist/inflections_UTF-8.txt";
        int record = 0;
        String input;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8))) {
            while ((input = reader.readLine()) != null) {
                record++;
                String[] row = input.split("\t");
                InflistEntity entry = new InflistEntity(row[0],row[1],row[2]);
                em.persist(entry);
            }
            System.out.println(record + " records processed");
            em.getTransaction().commit();
        }
    }

    public void feed1gramZip(Character letter) throws Exception {
        String path = "C:/Users/Mikhail/Desktop/Senior_Project/data/ngram/googlebooks-eng-all-1gram-20120701-" + letter + ".gz";
        int record = 0;
        String input;
        String[] tags = {"ADJ", "ADP", "ADV", "CONJ", "DET", "NOUN", "NUM", "PRON", "PRT", "VERB"};
        List<String> tagList = Arrays.asList(tags);

        try(BufferedReader reader = new BufferedReader(
                new InputStreamReader(new GZIPInputStream(new FileInputStream(path)), StandardCharsets.UTF_8))) {

            System.out.println("Scanning through " + letter);
            em.getTransaction().begin();
            while ((input = reader.readLine()) != null) {
                String[] row = input.split("\t");
                String[] word_tag = row[0].split("_");
                int tagLen = word_tag[word_tag.length - 1].length();

                if (tagList.contains(word_tag[word_tag.length - 1]) && word_tag.length > 1) {
                    String word = row[0].substring(0, row[0].length() - tagLen - 1);
                    String tag = row[0].substring(row[0].length() - tagLen);
                    NgramEntity entry = new NgramEntity(word, tag, Short.parseShort(row[1]), Integer.parseInt(row[2]));
                    em.persist(entry);
                } else {
                    NgramEntity entry = new NgramEntity(row[0],null, Short.parseShort(row[1]), Integer.parseInt(row[2]));
                    em.persist(entry);
                }
                record++;
                if(record%20000==0) {
                    System.out.println(record + " complete");
                    em.getTransaction().commit();
                    em.getTransaction().begin();
                }
            }
            em.getTransaction().commit();
            System.out.println("\n" + record + " records processed");
        }
    }

    public void close() {
        try {
            em.close();
            emf.close();
        } catch (Exception e) {
            System.out.println("Database could not close properly");
            e.printStackTrace();
        }
    }
}
