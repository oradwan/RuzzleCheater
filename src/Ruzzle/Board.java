package Ruzzle;

import java.util.*;

/**
 * User: oliverradwan
 * Date: 3/3/13
 */
public class Board {
    //TODO:
    //  1: make a little more robust around error handling
    //  2: factor the scoring better
    //  3: pull out the nonsense
    //  4: unit test so I can futz faster

    //Do not edit
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
    private static final int[] VALUES = {1, 4, 4, 2, 1, 4, 3, 4, 1, 1, 1, 1, 3, 1, 1, 4, 1, 1, 1, 1, 1, 1, 4, 1, 4, 10};
    //                                   a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z
    // missing: j, q, k, u, v, x
    private static final int DOUBLE_LETTER = 2;
    private static final int TRIPLE_LETTER = 3;
    private static final int DOUBLE_WORD = 5;
    private static final int TRIPLE_WORD = 6;
    private static final int NUM_COLS = 4, NUM_ROWS = 4;




    //GAME CONFIG
    private static final String BOARD = "iaesnrncetishpoa";

    //2 = double letter
    //3 = triple letter
    //5 = double word
    //6 = triple word

    private static final int[][] BONUS = {
            {3, 0, 2, 0},
            {5, 0, 0, 6},
            {0, 0, 0, 0},
            {3, 0, 0, 5}
    };





    private final String[][] BOARD_ARRAY = new String[NUM_COLS][NUM_ROWS];

    private final Dictionary DICTIONARY;

    private Set<Word> allWords = new HashSet<Word>();

    private final boolean[][] blankMask = new boolean[NUM_COLS][NUM_ROWS];

    public static void main(String[] args) {
        new Board();
    }

    public Board() {
        long start = System.currentTimeMillis();
        DICTIONARY = new Dictionary();
        if (BOARD.length() != 16) {
            throw new IllegalStateException("Board is illegal size");
        }

        for (int i = 0; i < NUM_COLS; i++) {
            for (int j = 0; j < NUM_ROWS; j++) {
                BOARD_ARRAY[i][j] = "" + BOARD.charAt((i) + (NUM_COLS * j));
                blankMask[i][j] = true;
            }
        }
        getAllWords();
        Word[] words = allWords.toArray(new Word[allWords.size()]);
        Arrays.sort(words, lengthComparator);
        for (Word word : words) {
            System.out.println(word);
        }
        long stop = System.currentTimeMillis();
        System.out.println("\n\nFound Words in " + (stop - start) + " ms");
    }

    Comparator<? super Word> lengthComparator = new Comparator<Word>() {
        @Override
        public int compare(Word s, Word s2) {
            return ((Integer) (s.getScore())).compareTo(s2.getScore());
        }
    };

    public void getAllWords() {
        for (int i = 0; i < NUM_COLS; i++) {
            for (int j = 0; j < NUM_ROWS; j++) {
                searchWords(new Word(), blankMask, i, j);
            }
        }
    }

    private static boolean[][] copyArray(boolean[][] in) {
        boolean[][] result = new boolean[NUM_ROWS][NUM_COLS];
        for (int i = 0; i < NUM_COLS; i++) {
            for (int j = 0; j < NUM_ROWS; j++) {
                if (in[i][j]) {
                    result[i][j] = true;
                } else {
                    result[i][j] = false;
                }
            }
        }
        return result;
    }

    public void searchWords(Word wordToNow, boolean[][] mask, int i, int j) {
        //Validate location
        if (i < 0 || i >= NUM_COLS || j < 0 || j >= NUM_ROWS) {
            return;
        }
        if (!mask[i][j]) {
            return;
        }

        Cell newLetter = new Cell(j, i, BOARD_ARRAY[i][j]);
        wordToNow = new Word(wordToNow, newLetter);

        if (DICTIONARY.isWord(wordToNow.getWord())) {
            allWords.add(wordToNow);
        }

        if (wordToNow.getCells().size() == 16) {
            return;
        }

        boolean wordsAreStillPossible = DICTIONARY.wordsPossible(wordToNow.getWord());

        if (wordsAreStillPossible) {
            boolean[][] clonedMask = copyArray(mask);
            //Other words are still possible.
            clonedMask[i][j] = false;

            searchWords(wordToNow, clonedMask, i - 1, j - 1);
            searchWords(wordToNow, clonedMask, i - 1, j);
            searchWords(wordToNow, clonedMask, i - 1, j + 1);
            searchWords(wordToNow, clonedMask, i, j - 1);
            searchWords(wordToNow, clonedMask, i, j + 1);
            searchWords(wordToNow, clonedMask, i + 1, j - 1);
            searchWords(wordToNow, clonedMask, i + 1, j);
            searchWords(wordToNow, clonedMask, i + 1, j + 1);
        }
    }

    private static final class Cell {
        public final int i, j;
        private final String s;

        public Cell(int i, int j, String s) {
            this.i = i;
            this.j = j;
            this.s = s.toLowerCase();
        }

        @Override
        public String toString() {
            return s + " : (" + i + ", " + j + ")" + getSuffix();
        }

        public String getS() {
            return s;
        }

        public int getBaseValue() {
            return VALUES[ALPHABET.indexOf(getS())];
        }

        public String getSuffix() {
            if(BONUS[i][j] == DOUBLE_LETTER){
                return "2L";
            } else if(BONUS[i][j] == DOUBLE_WORD){
                return "2*";
            } else if(BONUS[i][j] == TRIPLE_LETTER){
                return "3L";
            } else if(BONUS[i][j]==TRIPLE_WORD){
                return "3*";
            }
            return "";
        }
    }

    private static final class Word {
        private List<Cell> cells;

        private final String word;

        public Word() {
            cells = new ArrayList<Cell>();
            word = "";
        }

        public Word(Word w, Cell c) {
            cells = new ArrayList<Cell>();
            cells.addAll(w.getCells());
            cells.add(c);
            word = w.word + c.getS();
        }

        public List<Cell> getCells() {
            return cells;
        }

        public String getWord() {
            return word;
        }

        @Override
        public String toString() {
            return getWord() + " : " + getScore();
        }

        public int getScore() {
            int wordMultiplier = 1;
            int score = 0;
            for (Cell c : getCells()) {
                int cellValue = c.getBaseValue();
                if (BONUS[c.i][c.j] == DOUBLE_LETTER) {
                    cellValue *= 2;
                }
                if (BONUS[c.i][c.j] == TRIPLE_LETTER) {
                    cellValue *= 3;
                }
                if (BONUS[c.i][c.j] == DOUBLE_WORD) {
                    wordMultiplier *= 2;
                }
                if (BONUS[c.i][c.j] == TRIPLE_WORD) {
                    wordMultiplier *= 3;
                }
                score += cellValue;
            }

            int lengthScore = 5 * Math.max(0, getCells().size() - 4);

            return (wordMultiplier * score) + lengthScore;
        }
    }
}
