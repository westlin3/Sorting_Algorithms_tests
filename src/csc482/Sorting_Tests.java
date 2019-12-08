package csc482;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class Sorting_Tests {

    static ThreadMXBean bean = ManagementFactory.getThreadMXBean( );

    /* define constants */

    static long MAXVALUE =  200000000;

    static long MINVALUE = -200000000;

    static int numberOfTrials = 10;
    static int MAXINPUTSIZE  = (int) Math.pow(2,14);
    static int MININPUTSIZE  =  4;

    static String ResultsFolderPath = "/home/curtis/Bean/LAB4/"; // pathname to results folder
    static FileWriter resultsFile;
    static PrintWriter resultsWriter;


    //Quick Sort with pivot at end of list
    public static int partitionNaive(long arr[], int low, int high){
        long pivot = arr[high];
        int i = (low - 1);
        for (int j=low; j<high; j++){
            if (arr[j] < pivot) {
                i++;

                //swap arr[i] and arr[j]
                long temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }
        long temp = arr[i+1];
        arr[i+1] = arr[high];
        arr[high] = temp;

        return i+1;
    }

    //https://www.geeksforgeeks.org/quick-sort/
    public static void quickSortNaive(long arr[], int low, int high){
        if (low<high) {
            int pi = partitionNaive(arr, low, high);

            quickSortNaive(arr, low, pi -1);
            quickSortNaive(arr, pi+1, high);
        }
    }

    public static int partitionRandom2(long arr[], int low, int high){

        long pivot = arr[high];
        int i = (low - 1);
        for (int j=low; j<high; j++){
            if (arr[j] < pivot) {
                i++;

                //swap arr[i] and arr[j]
                long temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }
        long temp = arr[i+1];
        arr[i+1] = arr[high];
        arr[high] = temp;

        return i+1;
    }

    public static int partitionRandom(long arr[], int low, int high){
        int random = (int) (high - Math.random() * (high-low));

        long temp = arr[high];
        arr[high] = arr[random];
        arr[random] = temp;
        return partitionRandom2(arr, low, high);
    }

    //https://www.geeksforgeeks.org/quicksort-using-random-pivoting/
    public static void quickSortRandom(long arr[], int low, int high){
        if (low<high) {
            int pi = partitionRandom(arr, low, high);
            quickSortRandom(arr, low, pi-1);
            quickSortRandom(arr, pi+1, high);
        }
    }

    public static void bubbleSort(long arr[]){
        int length = arr.length;
        for (int i = 0; i < length-1; i++)
            for (int j = 0; j<length-i-1; j++)
                if (arr[j] > arr[j+1]) {
                    // swap arr[j+1] and arr[i]
                    long temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
        return;
    }

    //https://www.geeksforgeeks.org/insertion-sort/
    public static void insertionSort(long arr[]){
        int length = arr.length;
        for (int i = 1; i < length; ++i){
            long key = arr[i];
            int j = i - 1;

            while (j >= 0 && arr[j] > key){
                arr[j+1] = arr[j];
                j = j-1;
            }
            arr[j+1] = key;
        }
    }

    public static void merge(long arr[], int l, int m, int r){
        int n1 = m - l + 1;
        int n2 = r - m;

        long L[] = new long [n1];
        long R[] = new long [n2];

        for (int i=0; i<n1; ++i)
            L[i] = arr[l + i];
        for (int j=0; j<n2; ++j)
            R[j] = arr[m + 1 + j];

        int i = 0, j= 0;
        int k = l;
        while (i < n1 && j < n2) {
            if (L[i] <= R[j]) {
                arr[k] = L[i];
                i++;
            }
            else {
                arr[k] = R[j];
                j++;
            }
            k++;
        }

        while (i < n1) {
            arr[k] = L[i];
            i++;
            k++;
        }

        while (j < n2) {
            arr[k] = R[j];
            j++;
            k++;
        }
    }

    //https://www.geeksforgeeks.org/merge-sort/
    public static void mergeSort(long arr[], int l, int r){
        if (l < r) {
            int m = (l+r)/2;

            mergeSort(arr, l, m);
            mergeSort(arr, m+1, r);

            merge(arr, l, m, r);
        }
    }

    static void runFullExperiment(String resultsFileName){

        try {
            resultsFile = new FileWriter(ResultsFolderPath + resultsFileName);
            resultsWriter = new PrintWriter(resultsFile);
        } catch(Exception e) {
            System.out.println("*****!!!!!  Had a problem opening the results file "+ResultsFolderPath+resultsFileName);
            return; // not very foolproof... but we do expect to be able to create/open the file...
        }

        ThreadCpuStopWatch BatchStopwatch = new ThreadCpuStopWatch(); // for timing an entire set of trials
        ThreadCpuStopWatch TrialStopwatch = new ThreadCpuStopWatch(); // for timing an individual trial

        resultsWriter.println("#InputSize    AverageTime"); // # marks a comment in gnuplot data
        resultsWriter.flush();

        /* for each size of input we want to test: in this case starting small and doubling the size each time */
        for(int inputSize=MININPUTSIZE;inputSize<=MAXINPUTSIZE; inputSize*=2) {

            // progress message...
            System.out.println("Running test for input size "+inputSize+" ... ");

            /* repeat for desired number of trials (for a specific size of input)... */
            long batchElapsedTime = 0;

            // generate a list of randomly spaced integers in ascending sorted order to use as test input
            // In this case we're generating one list to use for the entire set of trials (of a given input size)
            // but we will randomly generate the search key for each trial

            /* force garbage collection before each batch of trials run so it is not included in the time */
            System.gc();


            // instead of timing each individual trial, we will time the entire set of trials (for a given input size)
            // and divide by the number of trials -- this reduces the impact of the amount of time it takes to call the
            // stopwatch methods themselves

            BatchStopwatch.start(); // comment this line if timing trials individually

            // run the trials
            for (long trial = 0; trial < numberOfTrials; trial++) {

                // generate a random key to search in the range of a the min/max numbers in the list
                //long testSearchKey = (long) (0 + Math.random() * (testList[testList.length-1]));
                /* force garbage collection before each trial run so it is not included in the time */
                //System.gc();
                System.out.print("    Generating test data...");
                long[] testList = createRandomIntegerList(inputSize);
                System.out.println("...done.");
                System.out.print("    Running trial batch...");

                //TrialStopwatch.start(); // *** uncomment this line if timing trials individually
                /* run the function we're testing on the trial input */
                //long foundIndex = slow3(testList);
                quickSortNaive(testList, 0, testList.length-1);
//              bubbleSort(testList);
//              insertionSort(testList);
                //quickSortRandom(testList, 0, testList.length-1);
//                mergeSort(testList,0, testList.length-1);
                //long foundIndex = binarySearch(testSearchKey, testList);
                // batchElapsedTime = batchElapsedTime + TrialStopwatch.elapsedTime(); // *** uncomment this line if timing trials individually

            }

            batchElapsedTime = BatchStopwatch.elapsedTime(); // *** comment this line if timing trials individually
            double averageTimePerTrialInBatch = (double) batchElapsedTime / (double)numberOfTrials; // calculate the average time per trial in this batch

            /* print data for this size of input */
            resultsWriter.printf("%12d  %15.2f \n",inputSize, averageTimePerTrialInBatch); // might as well make the columns look nice
            resultsWriter.flush();
            System.out.println(" ....done.");

        }

    }

    public static void main(String[] args)  {
        int sizeOfList = 10000;
        int trials = 10;
        double totalTime = 0;
        double nanoToSeconds = 1000000000;
        //long[] testList = {11,60,78,2,3,7,150,205,-2,9, 18, -35, 100,-88,-89,0,22};
        //long[] testList = createRandomIntegerList(sizeOfList);
        //long[] testList = createSortedList(sizeOfList);

        //quickSortNaive(testList, 0, testList.length-1);
//        bubbleSort(testList);
//        insertionSort(testList);
//        quickSortRandom(testList, 0, testList.length-1);
//        mergeSort(testList, 0, testList.length-1);
//        if (verifySorted(testList)) {
//            System.out.println("SUCCESSFUL SORT");
//        } else {
//            printArray(testList);
//            System.out.println("not sorted :(");
//        }
        runFullExperiment("NaiveUnSorted-Exp1-ThrowAway.txt");
        runFullExperiment("NaiveUnSorted-Exp2.txt");
        runFullExperiment("NaiveUnSorted-Exp3.txt");

    }

    public static boolean verifySorted(long arr[]) {
        for (int i = 0; i < arr.length-1; i++)
        {
            if (arr[i] > arr[i+1])
                return false;
        }
        return true;
    }

    public static void printArray(long arr[]){
        int n = arr.length;
        for (int i=0; i<n; ++i)
            System.out.print(arr[i] + " ");
        System.out.println();
    }

    public static long[] createRandomIntegerList(int size){
        int MAXVALUE = 410000000;
        int MINVALUE = -41000000;
        long[] newList = new long[size];
        for(int j=0;j<size;j++){
            newList[j] = (long) (MAXVALUE - Math.random() * (MAXVALUE - MINVALUE));
        }
        return newList;
    }

    public static long[] createSortedList(int size){
        long[] newList = new long[size];
        newList[0] = (long) (10 * Math.random());
        for(int j = 1; j<size; j++)
            newList[j] = newList[j - 1] + (long) (10 * Math.random());
        return newList;
    }
}
