package com.maf.main;

import com.maf.analysis.PUTAnalysis;
import com.maf.analysis.TPAnalysis;

/**
  * @Author sunweisong
  * @Date 2019/6/2 11:50 AM
  */

public class Main {

    public static void main(String[] args) {
        /**
         * Extract methods under test (MUT) from the program under test.
         */
        String javaFilesDirectoryPath = "/Users/sunweisong/Desktop/Test Recommendation Experimental Data/Programs Under Test/BPT/src/main/java/net/mooctest";
        PUTAnalysis.extractMUTFromPUTDirectory(javaFilesDirectoryPath);

        /**
         * Extract test fragments from students' test codes.
         */
        String pathOfPUT = "/Users/sunweisong/Desktop/Test Recommendation Experimental Data/Programs Under Test/ALU";
        String pathOfTP = "/Users/sunweisong/Desktop/Test Recommendation Experimental Data/Test Programs/ALU";
        TPAnalysis.extractTestFragmentsFromTestProgram(pathOfPUT, pathOfTP);
    }

}
