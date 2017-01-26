package com.funso;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

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
    // maps <word> to Map of <docno> to <termFreq>
    public static Map<String, Map<String, String>> wordDocNo = new HashMap<>();

    public static String[] wordArr;
    public static String[] docFreqArr;
    public static String[] docNoArr;
    public static String[] termFreqArr;

    public static int dictionaryId = 0;
    public static int postingId = 0;
    public static int docId = 0;

    public static int TOTAL_NUMBER_OF_DOCUMENTS;
    public static int TOTAL_NUMBER_OF_WORDS;

    public static String[] queryWordArr;
    public static String[] queryDocArr;

    public static double[][] weightMatrix;
    public static double[] docVecMagnitude;
    public static double[] queryVec;
    public static double [] docQueryCosine;
    public static Map<Double, ArrayList<String>> cosineDocNoMap;

    public static String[] resultantDocNo;

    public static void main(String[] args) {
        doOnlineProcessing();
    }

    public static void doOnlineProcessing(){
        loadIndices();
//        generateWeightMatrix();
//        normalizeWeightMatrix();
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
            TOTAL_NUMBER_OF_WORDS = size;
            wordArr = new String[size];
            docFreqArr = new String[size];
        }else if (filePath.equalsIgnoreCase(POSTING_OUTPUT_TXT)){
            docNoArr = new String[size];
            termFreqArr = new String[size];
        }else if (filePath.equalsIgnoreCase(DOCS_ID_TXT)){
            TOTAL_NUMBER_OF_DOCUMENTS = size;
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
                if ("q".equalsIgnoreCase(query)){
                    System.out.println("Program Exited!");
                    System.exit(0);
                }
                System.out.println("Processing Query ...");
                processQuery(query);
                System.out.println("Done!");
                String move;
                int start = 0;
                int end;
                do{
                    end = (start + 10) % resultantDocNo.length;
                    System.out.println(
                            String.format(
                                    "<======== Search Result %d to %d out of %d results =====> \n %s \n",
                                    start + 1,
                                    end,
                                    resultantDocNo.length,
                                    Arrays.toString(Arrays.copyOfRange(resultantDocNo, start, end))
                            )
                    );
                    System.out.println("Enter 'n' to see next set of results OR 'p' to see the prev set: ");
                    move = reader.readLine();
                    if ("n".equalsIgnoreCase(move)){
                        start = (start + 10) % resultantDocNo.length;
                    }else if ("p".equalsIgnoreCase(move)){
                        if (start > 9 && end > 18){
                            start -= 10;
                        }else{
                            System.out.println(" No Previous Result; Proceed to next set of results.");
                        }
                    }else{
                        System.out.println("End of Search. Perform another search.");
                    }
                }while(!"r".equalsIgnoreCase(move));
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
        Map<String, Integer> map = new HashMap<>();
        HashSet<String> docSet = new HashSet<>();
        Integer previousValue;
        Map<String, String> docFreqMap;
        for (String each: words){
            previousValue = map.get(each);
            if (dictionaryMap.containsKey(each)){
                if (previousValue == null){
                    previousValue = 0;
                    docFreqMap = wordDocNo.get(each);
                    if (docFreqMap != null && !docFreqMap.isEmpty()){
                        docSet.addAll(docFreqMap.keySet());
                    }
                }
                map.put(each, previousValue + 1);
            }
        }

        int rows = map.size();
        int cols = docSet.size();

        queryVec = new double[rows];
        queryWordArr = new String[rows];
        map.keySet().toArray(queryWordArr);

        queryDocArr = new String[cols];
        docSet.toArray(queryDocArr);

        Integer termFreq;
        double docFreq;
        double queryVecMagnitude = 0;
        for (int i = 0; i < rows; i++){
            termFreq = map.get(queryWordArr[i]);
            docFreq = Double.valueOf(dictionaryMap.get(queryWordArr[i]));
            if (termFreq != null){
                queryVec[i] = (1 + Math.log(termFreq)) * Math.log(TOTAL_NUMBER_OF_DOCUMENTS / docFreq);
                queryVecMagnitude += Math.pow(queryVec[i], 2);
            }
        }

        // normalize Query Vector
        for (int j = 0; j < rows; j++){
            queryVec[j] /= queryVecMagnitude;
        }

        generateWeightMatrix();
        normalizeWeightMatrix();

        docQueryCosine = new double[cols];
        ArrayList<String> list;
        ArrayList<Double> uniqueCosineValues = new ArrayList<>();
        cosineDocNoMap = new HashMap<>();
        for (int k = 0; k < cols; k++){
            for (int l = 0; l < rows; l++){
                docQueryCosine[k] += weightMatrix[l][k] * queryVec[l];
            }
            list = cosineDocNoMap.get(docQueryCosine[k]);
            if (list == null){
                list = new ArrayList<>();
                uniqueCosineValues.add(docQueryCosine[k]);
                cosineDocNoMap.put(docQueryCosine[k], list);
            }
            list.add(docNoArr[k]);
        }

        Double [] uniqueCosineValArr = new Double[uniqueCosineValues.size()];
        uniqueCosineValues.toArray(uniqueCosineValArr);
        Arrays.sort(uniqueCosineValArr);

        int j = 0;
        ArrayList<String> result;
        for (int i = uniqueCosineValArr.length - 1; i >= 0 ; i--){
            result = cosineDocNoMap.get(uniqueCosineValArr[i]);
            for (int k = 0; k < result.size(); k++){
                resultantDocNo[j++] = result.get(k);
            }
        }
    }

    public static void generateWeightMatrix(){
        int rows = queryWordArr.length;
        int cols = queryDocArr.length;
        weightMatrix = new double[rows][cols];
        docVecMagnitude = new double[cols];
        resultantDocNo = new String[cols];
        String termFreq;
        double docFreq;
        for (int i = 0; i < rows; i++){
            Map<String, String> docNoMap = wordDocNo.get(queryWordArr[i]);
            docFreq = Double.valueOf(dictionaryMap.get(queryWordArr[i]));
            if (docNoMap != null && !docNoMap.isEmpty()) {
                for (int j = 0; j < cols; j++) {
                    termFreq = docNoMap.get(queryDocArr[j]);
                    if (termFreq != null){
                        weightMatrix[i][j] = (1 + Math.log(Double.valueOf(termFreq)))
                                * Math.log(TOTAL_NUMBER_OF_DOCUMENTS / docFreq);
                        docVecMagnitude[j] += Math.pow(weightMatrix[i][j], 2);
                    }
                }
            }
        }
    }

    public static void normalizeWeightMatrix(){
        for (int i = 0; i < weightMatrix.length; i++){
            for (int j = 0; j < weightMatrix[i].length; j++){
                weightMatrix[i][j] /= Math.sqrt(docVecMagnitude[j]);
            }
        }
    }

}
