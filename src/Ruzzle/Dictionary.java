package Ruzzle;

import java.io.*;
import java.util.*;

/**
 * User: oliverradwan
 * Date: 3/3/13
 * Time: 3:45 PM
 */
public class Dictionary {
    private static final boolean FORCE_REINDEX = false;

    private static final boolean DEBUG = false;

    private static final File SERIALIZED_SEARCH_FILE = new File("resources/serializedWords.ruzzle");
    private static final File WORDLIST_DIRECTORY = new File("resources/ispell-enwl-3.1.20/");

    private List<String> wordsBinarySearch = new ArrayList<String>();
    private Set<String> wordsFastIndex = new TreeSet<String>();
    private int listSize;

    public Dictionary() {
        long start = System.currentTimeMillis();
        if (FORCE_REINDEX) {
            initializeStructuresAndSerialize();
        } else {
            try {
                initializeStructuresUsingFile();
            } catch (Exception e) {
                initializeStructuresAndSerialize();
            }
        }
        System.out.println("Dictionary size: " + wordsFastIndex.size());
        long stop = System.currentTimeMillis();
        System.out.println("Made dictionary in " + (stop - start) + " ms");
    }

    private void initializeStructuresUsingFile() {
        wordsBinarySearch = (List<String>) readObjectFromFile(SERIALIZED_SEARCH_FILE);
        wordsFastIndex.addAll(wordsBinarySearch);
        listSize = wordsFastIndex.size();
    }

    private Object readObjectFromFile(File f) {
        try {
            FileInputStream fileInputStream = new FileInputStream(f);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            return objectInputStream.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void initializeStructuresAndSerialize() {
        try {
            for (File file : WORDLIST_DIRECTORY.listFiles()) {
                Scanner scanner = new Scanner(file);
                while (scanner.hasNext()) {
                    String word = scanner.nextLine();
                    word = word.trim();
                    if (isValidWord(word)) {
                        wordsFastIndex.add(word.toLowerCase());
                    }
                }
            }
        } catch (FileNotFoundException e) {
            throw new IllegalStateException(e);
        }

        listSize = wordsFastIndex.size();
        wordsBinarySearch.addAll(wordsFastIndex);

        writeObjectToFile(wordsBinarySearch, SERIALIZED_SEARCH_FILE);
    }

    private void writeObjectToFile(Object out, File file) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(out);
            objectOutputStream.close();
            fileOutputStream.close();
        } catch (Exception ignored) {
        }
    }

    private boolean isValidWord(String possibleWord) {
        return !possibleWord.contains("'");
    }


    public boolean wordsPossible(String prefix) {
        boolean b = binaryWordSearch(prefix.toLowerCase(), 0, listSize);
        if(DEBUG){
            System.out.println("Word possible '" + prefix + "': " + b);
        }
        return b;
    }

    private boolean binaryWordSearch(String prefix, int min, int max) {
        int midPoint = (max + min) / 2;
        String wordAtMidPoint = wordsBinarySearch.get(midPoint);
        if (wordAtMidPoint.startsWith(prefix)) {
            return true;
        }
        if (max - min <= 1) {
            return false;
        }
        int bs = prefix.compareTo(wordAtMidPoint);
        if (bs == 0) {
            return wordAtMidPoint.startsWith(prefix);
        } else if (bs < 0) {
            return binaryWordSearch(prefix, min, midPoint);
        } else {
            return binaryWordSearch(prefix, midPoint, max);
        }
    }

    public boolean isWord(String word) {
        boolean contains = wordsFastIndex.contains(word.toLowerCase());
        if(DEBUG){
            System.out.println("Is word '" + word + "': " + contains);
        }
        return contains;
    }
}
