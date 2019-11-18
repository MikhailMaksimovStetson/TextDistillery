package com.textdistillery.db.jpa;

import javax.jdo.annotations.Index;
import javax.persistence.Entity;

@Entity(name="Ngram")
public class NgramEntity {

    @Index private String token;
    private String pos;
    private short year;
    private int frequency;

    public NgramEntity(String token, String pos, short year, int frequency) {
        this.token = token;
        this.pos = pos;
        this.year = year;
        this.frequency = frequency;
    }

    public String toString() {
        return String.format("(%s, %s, %s, %s)", token, pos, year, frequency);
    }


}
