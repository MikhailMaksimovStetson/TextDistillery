package com.textdistillery.db.jdbc;

import com.textdistillery.Config;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class DB {

    private Connection connect;
    private PreparedStatement statement;
    private ResultSet resultSet;

    public static void main(String[] args) throws Exception {
        DB database = new DB();
        //database.feedInflectionDict();
        /*for (char i='a'; i<'z'; i++) {
            database.feed1gramZip(i);
        }*/
        //database.feed1gramZip('a');
        //database.selectQuery("SELECT TABLENAME FROM SYS.SYSTABLES WHERE TABLETYPE='T'");
        //database.selectQuery("select * from ngram where id = '1'");
        //database.updateQuery("CREATE INDEX lemma_i on inflections (lemma)");
        //database.updateQuery("RENAME COLUMN ngram.pov TO pos");
        //database.selectQuery("SELECT * from google10k where word = 'Mikhail'");
        System.out.println(database.findLemma("fought") + " " + database.findInfTag("fought"));

        /*database.updateQuery("CREATE TABLE infdict (" +
                "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
                "inflection VARCHAR(50) NOT NULL," +
                "lemma VARCHAR(50) NOT NULL," +
                "tag VARCHAR(5) NOT NULL," +
                "CONSTRAINT infdict_key PRIMARY KEY (id))");
        database.updateQuery("CREATE INDEX inflection_i on infdict (inflection)");
        database.updateQuery("CREATE INDEX lemma_i on infdict (lemma)");
        database.updateQuery("CREATE INDEX tag_i on infdict (tag)");*/
        //database.connect.commit();
        database.close();
    }

    public DB() throws Exception {
        try {
            Class.forName(Config.DRIVER).newInstance();
            connect = DriverManager.getConnection(Config.PROTOCOL + Config.DB_PATH);
            connect.setAutoCommit(false);
            System.out.println("Database connection opened");
        } catch (Exception e) {
            close();
            throw e;
        }
    }

    public void selectQuery(String command) throws SQLException {
        try {
            statement = connect.prepareStatement(command, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            resultSet = statement.executeQuery();
            printResultSet(resultSet);
        } catch (SQLException e) {
            System.out.println("Problem with query: " + command);
            close();
            throw e;
        }
    }

    public void updateQuery(String command) throws SQLException {
        try {
            statement = connect.prepareStatement(command);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Problem with query: " + command);
            close();
            throw e;
        }
    }

    public static void printResultSet(ResultSet rs) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnsNumber = rsmd.getColumnCount();
        while (rs.next()) {
            for (int i = 1; i <= columnsNumber; i++) {
                if (i > 1) System.out.print(" | ");
                System.out.print(rs.getString(i));
            }
            System.out.println("");
        }
    }

    public String findLemma(String word) {
        return searchInflections(word, "lemma");
    }

    public String findInfTag(String word) {
        return searchInflections(word, "tag");
    }

    private String searchInflections(String word, String col) {
        try {
            //selectQuery("select * from inflections where inflection = '" + word + "'");
            statement = connect.prepareStatement(
                    "select * from inflections where inflection = ?",
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            statement.setString(1, word);
            resultSet = statement.executeQuery();
            if (resultSet.first()){
                String lemma = resultSet.getString(col);
                return lemma;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void feed1gramZip(Character letter) throws Exception {
        String path = Config.RESOURCE + "ngram/googlebooks-eng-all-1gram-20120701-" + letter + ".gz";
        String[] tags = {"ADJ", "ADP", "ADV", "CONJ", "DET", "NOUN", "NUM", "PRON", "PRT", "VERB"};
        List<String> tagList = Arrays.asList(tags);
        ArrayList<String> tagTracker = new ArrayList<>();
        statement = connect.prepareStatement("insert into ngram(text,pos,years,frequency,books) values(?,?,?,?,?)");
        int record = 0;
        int maxchar = 200;
        String input;
        try {
            System.out.println("Scanning through " + letter);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new GZIPInputStream(new FileInputStream(path)), StandardCharsets.UTF_8));
            while ((input = reader.readLine()) != null) {
                String[] row = input.split("\t");
                String[] word_tag = row[0].split("_");
                int tagLen = word_tag[word_tag.length - 1].length();

                if (tagList.contains(word_tag[word_tag.length - 1]) && word_tag.length > 1) {
                    String word = row[0].substring(0, row[0].length() - tagLen - 1);
                    String tag = row[0].substring(row[0].length() - tagLen);
                    //String[] out = {word, tag, row[1], row[2], row[3]};
                    statement.setString(1, word);
                    statement.setString(2, tag);
                    statement.setInt(3, Integer.parseInt(row[1]));
                    statement.setInt(4, Integer.parseInt(row[2]));
                    statement.setInt(5, Integer.parseInt(row[3]));
                    statement.executeUpdate();
                } else {
                    //String[] out = {row[0], null, row[1], row[2], row[3]};
                    statement.setString(1, row[0]);
                    statement.setString(2, null);
                    statement.setInt(3, Integer.parseInt(row[1]));
                    statement.setInt(4, Integer.parseInt(row[2]));
                    statement.setInt(5, Integer.parseInt(row[3]));
                    statement.executeUpdate();
                }
                record++;
            }
            System.out.println("\n" + record + " records processed");
            System.out.println(tagTracker.toString());
            reader.close();
            connect.commit();
        } catch (Exception e) {
            close();
            throw e;
        }
    }

    public void feedInflectionDict() throws Exception{
        String path = Config.RESOURCE + "dataset/inflections_UTF-8.txt";
        ArrayList<String> tagTracker = new ArrayList<>();
        statement = connect.prepareStatement("insert into infdict(inflection,lemma,tag) values(?,?,?)");
        int record = 0;
        String input;
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(path),StandardCharsets.UTF_8)); //original is Cp1252 or ISO_8859_1
            while ((input = reader.readLine()) != null) {
                record++;
                String[] row = input.split("\t");
                statement.setString(1, row[0]);
                statement.setString(2, row[1]);
                statement.setString(3, row[2]);
                statement.executeUpdate();
                /*if (!Pattern.matches("^[\\d\\p{L}-/$.']+$", row[0]))
                    System.out.println(Arrays.toString(row));*/
                if (!tagTracker.contains(row[2]))
                    tagTracker.add(row[2]);
            }
            System.out.println("\n" + record + " records processed");
            System.out.println(tagTracker.toString());
            reader.close();
            connect.commit();
        } catch (Exception e) {
            close();
            throw e;
        }
    }

    public void close() {
        try {
            connect.rollback();
            if (resultSet != null)
                resultSet.close();
            if (statement != null)
                statement.close();
            if (connect != null)
                connect.close();
            System.out.println("Database connection closed");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Database could not close properly!");
        }
    }
}
