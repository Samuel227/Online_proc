package com.funso;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Main {

    public static String RESOURCES_DIR = "/Users/funso/IdeaProjects/NLPOnlineProcessing/resources/";
    public static String DICTIONARY_OUTPUT_TXT = RESOURCES_DIR + "dictionary.txt";
    public static String DOCS_ID_TXT = RESOURCES_DIR + "docsid.txt";
    public static String POSTING_OUTPUT_TXT = RESOURCES_DIR + "posting.txt";

    // maps <word> to <docFreq>
    public static Map<String, String> dictionaryMap = new HashMap<>();
    // maps <docno> to <termFreq>
    public static Map<String, String> postingMap = new HashMap<>();
    // maps <docid> to <startPosition>
    public static Map<String, String> docMap = new HashMap<>();
    // maps <word> to Array of <docno>
    public static Map<String, Map<String, String>> wordDocNo = new HashMap<>();

    public static String[] wordArr;
    public static String[] docFreqArr;
    public static String[] docNoArr;
    public static String[] termFreqArr;

    public static int dictionaryId = 0;
    public static int postingId = 0;
    public static int docId = 0;


    public static void main(String[] args) {
        doOnlineProcessing();
    }

    public static void doOnlineProcessing(){
        loadIndices();
        readQueries();
    }

    public static void loadIndices(){
        loadDictionary();
        loadPostings();
        loadDocIds();
        int i = 0;
        int max = 0;
        int size = wordArr.length;
        Map<String, String> localMap;
        for (int j = 0; j < size; j++){
            localMap = new HashMap<>();
            max = i + Integer.valueOf(docFreqArr[j]);
            while(i < max){
                localMap.put(docNoArr[i], termFreqArr[i]);
                i++;
            }
            wordDocNo.put(wordArr[j], localMap);
        }
    }

    public static void loadFileContent(String filePath, Map<String, String> map){
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(filePath));
            String line;
            String [] strArr;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                if (isFirstLine){
                    isFirstLine = false;
                    initializeArr(filePath, line);
                }else{
                    strArr = line.split(" ");
                    map.put(strArr[0], strArr[1]);
                    fillArr(filePath, strArr);
                }
            }
        }catch (IOException ex){
            ex.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public static void initializeArr(String filePath, String total){
        int size = Integer.valueOf(total);
        if (filePath.equalsIgnoreCase(DICTIONARY_OUTPUT_TXT)){
            wordArr = new String[size];
            docFreqArr = new String[size];
        }else if (filePath.equalsIgnoreCase(POSTING_OUTPUT_TXT)){
            docNoArr = new String[size];
            termFreqArr = new String[size];
        }else if (filePath.equalsIgnoreCase(DOCS_ID_TXT)){

        }
    }

    public static void fillArr(String filePath, String[] contentArr){
        if (filePath.equalsIgnoreCase(DICTIONARY_OUTPUT_TXT)){
            wordArr[dictionaryId] = contentArr[0];
            docFreqArr[dictionaryId] = contentArr[1];
            dictionaryId++;
        }else if (filePath.equalsIgnoreCase(POSTING_OUTPUT_TXT)){
            docNoArr[postingId] = contentArr[0];
            termFreqArr[postingId] = contentArr[1];
            postingId++;
        }else if (filePath.equalsIgnoreCase(DOCS_ID_TXT)){

        }
    }

    public static void loadDictionary() {
        loadFileContent(DICTIONARY_OUTPUT_TXT, dictionaryMap);
    }

    public static void loadPostings(){
        loadFileContent(POSTING_OUTPUT_TXT, postingMap);
    }

    public static void loadDocIds(){
        loadFileContent(DOCS_ID_TXT, docMap);
    }

    public static void readQueries(){
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new InputStreamReader(System.in));
            while(true){
                System.out.println("Please Enter a Query\n OR \nEnter 'q' to Quit:");
                String query = reader.readLine();
                processQuery(query);
                if ("q".equalsIgnoreCase(query)){
                    System.out.println("Program Exited!");
                    System.exit(0);
                }
            }
        } catch (IOException ex){
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }finally {
            if (reader != null){
                try{
                    reader.close();
                }catch (IOException ex){
                    ex.printStackTrace();
                }
            }
        }
    }

    public static void processQuery(String query){
        String [] words = query.split(" ");
        Map<String, String> map;
        for (String each: words){
            map = wordDocNo.get(each);
            if (map == null || map.isEmpty()){
                System.out.println("Word: " + each + " is not Found");
            }else{
                System.out.println("Word: " + each + " ====> " + map.keySet());
            }

        }
    }

    public static double getJacquardCoefficient(){
        return 0;
    }

}
