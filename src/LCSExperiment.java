import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.io.*;
import java.lang.Object;
import java.lang.String;
import java.util.Arrays;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;



public class LCSExperiment {

    static ThreadMXBean bean = ManagementFactory.getThreadMXBean( );

    /* define constants */
    static long MAXVALUE =  2000000000;
    static long MINVALUE = -2000000000;
    static int numberOfTrials = 100;
    static int MAXINPUTSIZE  = (int) Math.pow(2,18);
    static int MININPUTSIZE  =  1;
    // static int SIZEINCREMENT =  10000000; // not using this since we are doubling the size each time

    static String ResultsFolderPath = "/home/karson/Results/"; // pathname to results folder
    static FileWriter resultsFile;
    static PrintWriter resultsWriter;


    public static void main(String[] args) {

        // run the whole experiment at least twice, and expect to throw away the data from the earlier runs, before java has fully optimized
        runFullExperiment("TextLCS-Exp1-ThrowAway.txt");
        runFullExperiment("TextLCS-Exp2.txt");
        runFullExperiment("TextLCS-Exp3.txt");
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

        //read in huckleberry book
        String huckleText = "";
        String filePath = "/home/karson/Documents/HuckleBerry.txt";

        huckleText = textToString(filePath).replaceAll(" ", ""); // remove spaces

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
            System.out.print("    Generating test data...");

            String random1 = "", random2 = "";

            //random1 = "xxxxxxxx";
            //random2 = "xxxxxxxx";

            double dub = (Math.random()*25)+1; //generating two random integers
            int num = (int)dub;
            //random1 = randomString(num);
            random1 = huckleString(num, huckleText);
            dub = (Math.random()*25)+1;
            num = (int)dub;
            //random2 = randomString(num);
            random2 = huckleString(num, huckleText);


            System.out.println("...done.");
            System.out.print("    Running trial batch...");

            /* force garbage collection before each batch of trials run so it is not included in the time */
            System.gc();

            int[] findLCS = new int[3]; // variable to hold LCS, and indexes of where LCS starts in both strings



            // instead of timing each individual trial, we will time the entire set of trials (for a given input size)
            // and divide by the number of trials -- this reduces the impact of the amount of time it takes to call the
            // stopwatch methods themselves
            BatchStopwatch.start(); // comment this line if timing trials individually

            // run the tirals
            for (long trial = 0; trial < numberOfTrials; trial++) {

                //TrialStopwatch.start(); // *** uncomment this line if timing trials individually
                /* run the function we're testing on the trial input */


                findLCS = bruteLCS(random1,random2);
                //findLCS = newLCS(random1,random2);


                // batchElapsedTime = batchElapsedTime + TrialStopwatch.elapsedTime(); // *** uncomment this line if timing trials individually
            }
            batchElapsedTime = BatchStopwatch.elapsedTime(); // *** comment this line if timing trials individually
            double averageTimePerTrialInBatch = (double) batchElapsedTime / (double)numberOfTrials; // calculate the average time per trial in this batch

            String LCS = "";

            //if statement is needed for my version since the strings could get swapped depending on which one is smallest
            //if(random1.length() <= random2.length()) {
                for (int i = 0; i < findLCS[0]; i++) {
                    LCS += random1.charAt(findLCS[1] + i);
                }
            /*}
            else {
                for (int i = 0; i < findLCS[0]; i++) {
                    LCS += random2.charAt(findLCS[1] + i);
                }
            }
            */

            if(findLCS[0] == 0)
                LCS = "nothing";

            /* print data for this size of input */
            resultsWriter.printf("%12d  %15.2f \n",inputSize, averageTimePerTrialInBatch); // might as well make the columns look nice
            resultsWriter.flush();

            System.out.println("\n LCS of "+ random1 + " and " + random2 + " is " + LCS);

            System.out.println(" ....done.");
        }
    }

    public static String randomString(int n){
        // chose a Character random from this String
        String AlphaNumericString = /*"ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                +*/ "abcdefghijklmnopqrstuvxyz";

        // create StringBuffer size of AlphaNumericString
        StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; i++) {

            // generate a random number between
            // 0 to AlphaNumericString variable length
            int index
                    = (int)(AlphaNumericString.length()
                    * Math.random());

            // add Character one by one in end of sb
            sb.append(AlphaNumericString
                    .charAt(index));
        }

        return sb.toString();
    }

    public static String huckleString(int n, String huckle){
        String subString = "";
        double dub = (Math.random()*huckle.length())+1;
        int num = (int)dub;

        for( int i = 0; i < n; i++){
            subString+= huckle.charAt(i+num);
        }

        return subString;
    }

    public static int[] bruteLCS(String string1, String string2){
        int length1 = string1.length(), length2 = string2.length();
        int LCSLength = 0, string1Start = 0, string2Start = 0;
        int i, j, k;
        int[] LCSVal = new int[3];

        for( i = 0; i < length1; i++){
            for( j = 0; j < length2; j++){
                for( k = 0; k < Math.min(length1-i,length2-j); k++){
                    if( string1.charAt(i+k) != string2.charAt(j+k)) break;
                }
                if( k > LCSLength){
                    LCSLength = k;
                    string1Start = i;
                    string2Start = j;
                }
            }
        }

        LCSVal[0] = LCSLength;
        LCSVal[1] = string1Start;
        LCSVal[2] = string2Start;

        return LCSVal;
    }

    public static int[] newLCS(String s1,String s2){
        int length1, length2;
        int LCSLength = 0, string1Start = 0, string2Start = 0;
        int i, j, k;
        int[] LCSVal = new int[3];

        String string1 = new String();
        String string2 = new String();

        if( s1.length() <= s2.length()) { //have string1 be the smallest string, this will be helpful because
            string1 = s1;         //now i'll know that 'i' will always correspond with the smallest string
            string2 = s2;
            length1 = string1.length();
            length2 = string2.length();
        }
        else{
            string2 = s1;
            string1 = s2;
            length2 = string1.length();
            length1 = string2.length();
        }

        int small = Math.min(length1,length2);

        for( i = 0; i < length1; i++){
            if( LCSLength >= small - i) break; //the loop will stop if it the LCS cannot possibly be larger
            for( j = 0; j < length2; j++){
                for( k = 0; k < Math.min(length1-i,length2-j); k++){
                    if( string1.charAt(i+k) != string2.charAt(j+k)) break;
                }
                if( k > LCSLength){
                    LCSLength = k;
                    string1Start = i;
                    string2Start = j;
                }
            }
        }

        LCSVal[0] = LCSLength;
        LCSVal[1] = string1Start;
        LCSVal[2] = string2Start;

        return LCSVal;
    }

    private static String textToString(String filePath)
    {
        String content = "";
        try
        {
            content = new String ( Files.readAllBytes( Paths.get(filePath) ) );
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return content;
    }

}