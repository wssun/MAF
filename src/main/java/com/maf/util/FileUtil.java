package com.maf.util;

import com.csvreader.CsvWriter;
import com.maf.model.*;

import javax.management.loading.MLet;
import java.io.*;
import java.util.*;

public class FileUtil {

    /**
     * Read file content to string.
     *
     * @param file
     * @return String
     * @throws
     * @date 2018/4/9 下午8:33
     * @author sunweisong
     */
    public static String readFileContentToString(File file) {
        StringBuffer stringBuffer = new StringBuffer();
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(" " + line + "\n");
            }
        } catch (IOException e) {
            e.getStackTrace();
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                    bufferedReader = null;
                }
                if (fileReader != null) {
                    fileReader.close();
                    fileReader = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String content = stringBuffer.toString();
        stringBuffer = null;
        // Remove notes with regex.
        content = content.replaceAll("\\/\\/[^\\n]*|\\/\\*([^\\*^\\/]*|[\\*^\\/*]*|[^\\**\\/]*)*\\*+\\/", "");
        // Remove redundant spaces.
        content = content.replaceAll("\\s{2,}", " ");
        return content.trim();
    }


    /**
      *
      * @param mutModel2JsonList MUT list with json format.
      * @param targetDirectoryPath the path of target directory.
      * @return
      * @throws
      * @date 2019/6/12 11:51 AM
      * @author sunweisong
      */
    public static void writeMUTListToJsonFile(List<String> mutModel2JsonList, String targetDirectoryPath) {
        String jsonFilePath = targetDirectoryPath + File.separator + "mut.json";
        File jsonFile = new File(jsonFilePath);
        try {
            if (!jsonFile.exists()) {
                jsonFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;
        try {
            fileWriter = new FileWriter(jsonFile, false);
            bufferedWriter = new BufferedWriter(fileWriter);
            for (String mutString : mutModel2JsonList) {
                bufferedWriter.write(mutString + System.lineSeparator());
            }
            bufferedWriter.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bufferedWriter.close();
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
      *
      * @param targetFilePath
      * @return
      * @throws
      * @date 2019/6/12 12:01 PM
      * @author sunweisong
      */
    public static List<String> readFileContentToListByLine(String targetFilePath) {
        File targetFile = new File(targetFilePath);
        if (!targetFile.exists()) {
            System.err.println("The file doesn't exist!");
            return null;
        }
        List<String> stringList = new ArrayList<>();
        StringBuffer stringBuffer = new StringBuffer();
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            fileReader = new FileReader(targetFile);
            bufferedReader = new BufferedReader(fileReader);
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                stringList.add(line);
            }
        } catch (IOException e) {
            e.getStackTrace();
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                    bufferedReader = null;
                }
                if (fileReader != null) {
                    fileReader.close();
                    fileReader = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (stringList.size() == 0) {
            stringList = null;
            return null;
        }
        return stringList;
    }

    /**
      * 
      * @param 
      * @return
      * @throws
      * @date 2019/6/14 4:14 PM
      * @author sunweisong
      */
    public static void writeTestFragmentsToTargetFile(List<String> tfList, String targetFilePath) {
        File targetFile = new File(targetFilePath);
        if (targetFile.exists()) {
            targetFile.delete();
        }
        try {
            targetFile.createNewFile();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;
        try {
            fileWriter = new FileWriter(targetFile);
            bufferedWriter = new BufferedWriter(fileWriter);
            for (String tf : tfList) {
                bufferedWriter.write(tf + System.lineSeparator());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bufferedWriter.close();
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
