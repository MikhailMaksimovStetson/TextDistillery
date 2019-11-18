package com.textdistillery;

import com.textdistillery.db.jpa.DB;
import edu.princeton.cs.algs4.Stopwatch;
import edu.princeton.cs.algs4.StopwatchCPU;

import java.util.ArrayList;
import java.util.List;

public class App {

    public static void main( String[] args ) throws Exception {
        StopwatchCPU cpuTime = new StopwatchCPU();
        Stopwatch wallTime = new Stopwatch();
        new App();
        System.out.println("CPU time: " + cpuTime.elapsedTime() + " seconds");
        System.out.println("wall time: " + wallTime.elapsedTime() + " seconds");
    }

    public App() throws Exception {
        try(DB db = new DB(); Wordnet wordnet = new Wordnet()) {
            String input = Parser.readFile("input.txt");
            String[] inputTokens = Parser.tokenize(input);
            ArrayList<Rune> runeList = new ArrayList<>();
            for (String token : inputTokens) {
                runeList.add(new Rune(token, db, wordnet));
            }
            List<String> outputTokens = new ArrayList<>();
            StringBuilder builder = new StringBuilder();
            builder.append("Input ");
            //builder.append("Normalized ");
            builder.append("Lemma ");
            builder.append("Synonym ");
            //builder.append("Inflected ");
            builder.append("Output ");
            builder.append("Type ");
            builder.append("Case ");
            builder.append("POS ");
            builder.append("Inflection ").append("\n");
            for (Rune rune : runeList) {
                builder.append(rune.getInputToken()).append(" ");
                //builder.append(rune.getNormToken()).append(" ");
                builder.append(rune.getLemma()).append(" ");
                builder.append(rune.getSynonym()).append(" ");
                //builder.append(rune.getInfSynonym()).append(" ");
                builder.append(rune.getOutputSynonym()).append(" ");
                builder.append("'").append(rune.getTypeEnum()).append("' ");
                builder.append("'").append(rune.getCaseEnum()).append("' ");
                builder.append("'").append(rune.getPosEnum()).append("' ");
                builder.append("'").append(rune.getInfEnum()).append("'");
                builder.append("\n");
                outputTokens.add(rune.getOutputSynonym());
            }
            String[] t = outputTokens.toArray(new String[0]);
            String output = Parser.detokenize(t);
            Parser.writeFile(builder.toString(), "runes.dump");
            Parser.writeFile(output, "output.txt");
            System.out.println();
        }
    }
}
