package com.textdistillery;

public class Config {
    //general
    public static final String RESOURCE = "src/main/resources/";
    public static final int MIN_FREQ = 400;
    //wordnet
    public static final String WORDNET_VER = "3.0";
    public static final String WORDNET_PATH = "C:/Users/Mikhail/Desktop/Senior_Project/data/wordnet/" + WORDNET_VER;
    //objectdb
    public static final String OBJECTDB_PATH = "C:/Users/Mikhail/Desktop/Senior_Project/data/objectdb/words.odb";
    //derby
    public static final String PROTOCOL = "jdbc:derby:";
    public static final String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
    public static final String DB_PATH = "C:/Users/Mikhail/Desktop/Senior_Project/data/derby/wordlist2";

}
