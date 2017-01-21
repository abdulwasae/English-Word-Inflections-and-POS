import java.util.ArrayList;

/**
 * Created by Abdul Wasae on 19-Jul-16. cool
 */
public class WordData {
    private String wordInflection;
    private String wordLemma;
    private String pos;


    public WordData(String wordInflection) {
        this.wordInflection = wordInflection;
    }

    public String getWordInflection() {
        return wordInflection;
    }

    public void setWordInflection(String wordInflection) {
        this.wordInflection = wordInflection;
    }

    public String getWordLemma() {
        return wordLemma;
    }

    public void setWordLemma(String wordLemma) {
        this.wordLemma = wordLemma;
    }

    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }
}