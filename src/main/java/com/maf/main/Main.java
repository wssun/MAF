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
         * javaFilesDirectoryPath: the path of java files.
         */
        String javaFilesDirectoryPath = "******";
        PUTAnalysis.extractMUTFromPUTDirectory(javaFilesDirectoryPath);

        /**
         * Extract test fragments from test codes.
         * pathOfPUT: the path of the program under test
         * pathOfTP: the path of the test program
         */
        String pathOfPUT = "******";
        String pathOfTP = "******";
        TPAnalysis.extractTestFragmentsFromTestProgram(pathOfPUT, pathOfTP);
    }

}
