import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;


/**
 * Created by Abdul Wasae on 19-Jul-16.
 */
public class Main {

    static ArrayList<WordData> wordsList = new ArrayList<WordData>();
    static String outputTextFilePath = "assets/wordData.txt";
    static String outputSqliteDbPath = "C:/dbs/odnvt.db";

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        long sTime = System.currentTimeMillis();


        populateDbAndFile();

        System.out.println("ALL DONE");
        System.out.println("Execution took: "+((sTime-System.currentTimeMillis())/1000)+"sec");
    }




    private static void populateDbAndFile() throws IOException, ClassNotFoundException {


        Scanner wordInflectionsScanner = new Scanner(new File("assets/validWordInflections.txt"));
        wordsList.clear();

        while (wordInflectionsScanner.hasNext())
        {
            wordsList.add(new WordData(wordInflectionsScanner.next()));
        }
        System.out.println("Got Inflections. Size of Array list: " + wordsList.size());


        for(WordData word: wordsList) {

            // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution
            Properties props = new Properties();
            props.setProperty("annotators", "tokenize, ssplit, pos, lemma");
            StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

            // create an empty Annotation just with the given text
            Annotation document = new Annotation(word.getWordInflection());

            // run all Annotators on this text
            pipeline.annotate(document);

            ////////

            // these are all the sentences in this document
            // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
            List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
            CoreLabel coreLabel = sentences.get(0).get(CoreAnnotations.TokensAnnotation.class).get(0);

            String pos = coreLabel.get(CoreAnnotations.PartOfSpeechAnnotation.class);
            String lemma = coreLabel.get(CoreAnnotations.LemmaAnnotation.class);

            word.setPos(pos);
            word.setWordLemma(lemma);
        }
        System.out.println("Got pos and lemma. Size of Array list: " + wordsList.size());

        /////////////////////////NOW WRITE////////////
        Class.forName("org.sqlite.JDBC");
        Connection connection = null;

        String inflection;
        String lemma;
        String pos;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + outputSqliteDbPath);
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.
            statement.executeUpdate("delete from worddataeng");
            connection.commit();
            System.out.println("DB cleared");

            for(WordData wordDatum: wordsList) {

                inflection = wordDatum.getWordInflection(); //escape the " ' "
                inflection = inflection.replace("'", "''");
                lemma = wordDatum.getWordLemma();
                lemma = lemma.replace("'", "''");
                pos = wordDatum.getPos();

                //write to db table
                System.out.println("insert into worddataeng values('" + inflection + "', '" + lemma + "', '" + pos + "')");
                statement.executeUpdate("insert into worddataeng values('"
                        + inflection + "', '"
                        + lemma + "', '"
                        + pos + "')");

            }

            connection.commit();
            System.out.println("Data Entered into the DB");
        }
        catch(SQLException e)
        {
            // if the error message is "out of memory",
            // it probably means no database file is found
            System.err.println(e.getMessage());
        }
        finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e);
            }
        }


    }



    private static void populateWordData() throws IOException {
        Scanner wordInflectionsScanner = new Scanner(new File("assets/validWordInflections.txt"));

        while (wordInflectionsScanner.hasNext())
        {
            wordsList.add(new WordData(wordInflectionsScanner.next()));
        }
        System.out.println("Got Inflections. Size of Array list: " + wordsList.size());


        for(WordData word: wordsList) {

            // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution
            Properties props = new Properties();
            props.setProperty("annotators", "tokenize, ssplit, pos, lemma");
            StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

            // create an empty Annotation just with the given text
            Annotation document = new Annotation(word.getWordInflection());

            // run all Annotators on this text
            pipeline.annotate(document);

            ////////

            // these are all the sentences in this document
            // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
            List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
            CoreLabel coreLabel = sentences.get(0).get(CoreAnnotations.TokensAnnotation.class).get(0);

            String pos = coreLabel.get(CoreAnnotations.PartOfSpeechAnnotation.class);
            String lemma = coreLabel.get(CoreAnnotations.LemmaAnnotation.class);

            word.setPos(pos);
            word.setWordLemma(lemma);
        }
        System.out.println("Got pos and lemma. Size of Array list: " + wordsList.size());

        //write to new file
        flushFile(outputTextFilePath);
        FileWriter fileWriter = new FileWriter(outputTextFilePath, true);
        PrintWriter print_line = new PrintWriter(fileWriter);
        for(WordData word: wordsList) {
            print_line.println(word.getWordInflection()+", "
                    +word.getWordLemma()+", "+word.getPos());
        }
        print_line.close();

    }
    private static void flushFile(String fileName) throws IOException {
        FileWriter fileWriter = new FileWriter(fileName, false);
        PrintWriter print_line = new PrintWriter(fileWriter);
        print_line.print("");
        print_line.close();
    }

}
