package com.textdistillery.db.jpa;

import javax.jdo.annotations.Index;
import javax.jdo.annotations.Indices;
import javax.persistence.Entity;

@Entity(name="Inflist")
@Indices({
        @Index(members={"lemma","tag"}),
        @Index(members={"inflection"})
})
public class InflistEntity {

    private String inflection;
    private String lemma;
    private String tag;

    public InflistEntity(String inflection, String lemma, String tag) {
        this.inflection = inflection;
        this.lemma = lemma;
        this.tag = tag;
    }

    public String toString() {
        return String.format("(%s, %s, %s)", inflection, lemma, tag);
    }

    public String getInflection() {return inflection;}
    public String getLemma() {return lemma;}
    public String getTag(){return tag;}
}
