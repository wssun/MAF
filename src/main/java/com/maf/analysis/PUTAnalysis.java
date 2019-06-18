package com.maf.analysis;


import com.maf.util.JacksonUtil;
import com.maf.model.MUTModel;
import com.maf.util.FileUtil;
import com.maf.util.ParenthesisUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Author sunzesong
 * @Date 2018/4/3 22:01
 */

public class PUTAnalysis {

    /**
      * The path of the source code of the program is required to conform to
      * the format '～/ProjectName/src/main/java/' when you ready to extract MUT.
      * @param javaFilesDirectoryPath
      * @return
      * @throws
      * @date 2019/6/12 11:52 PM
      * @author sunweisong
      */
    public static void extractMUTFromPUTDirectory (String javaFilesDirectoryPath) {
        File javaFilesDirectory = new File(javaFilesDirectoryPath);
        if (!javaFilesDirectory.exists()) {
            System.err.println("The directory doesn't exist!");
            return;
        }
        File[] javaFiles = javaFilesDirectory.listFiles();
        List<MUTModel> allMUTModelList = new ArrayList<>();
        for (File javaFile : javaFiles) {
            String javaFileName = javaFile.getName();
            if (".DS_Store".equals(javaFileName) || javaFile.isDirectory() || !javaFileName.endsWith(".java")) {
                continue;
            }
            List<MUTModel> mutModelList = extractMUTFromJavaFile(javaFile.getAbsolutePath());
            if (mutModelList != null) {
                allMUTModelList.addAll(mutModelList);
            }
        }
        List<String> mutModel2JsonList = new ArrayList<>(allMUTModelList.size());
        for (MUTModel mutModel: allMUTModelList) {
            String modelJson = JacksonUtil.bean2Json(mutModel);
            mutModel2JsonList.add(modelJson);
        }
        FileUtil.writeMUTListToJsonFile(mutModel2JsonList, javaFilesDirectoryPath);
    }

    /**
      *
      * @param
      * @return
      * @throws
      * @date 2019/6/12 11:36 PM
      * @author sunweisong
      */
    public static List<MUTModel> extractMUTFromJavaFile (String javaFilePath) {
        File javaFile = new File(javaFilePath);
        if (!javaFile.exists()) {
            System.err.println("The file doesn't exist!");
            return null;
        }
        String fileContent = FileUtil.readFileContentToString(javaFile);
        if (!ParenthesisUtil.judgeParenthesisMatchAmongString(fileContent)) {
            System.err.println("The parenthesis in the test file \"" + javaFile.getName() + "\" is mismatched!");
            return null;
        }
        List<MUTModel> mutModelList = analyzeJavaFileContentString(fileContent);
        return  mutModelList;
    }


    /**
     * 分析subject文件内容字符串, 切割类(包括内部类)
     *
     * @param javaFileContentString subject文件内容字符串
     * @author sunzesong
     */
    private static List<MUTModel> analyzeJavaFileContentString(String javaFileContentString) {
        List<MUTModel> mutModelList = new ArrayList<>();

        int leftBracketsNum = 0;
        int rightBracketsNum = 0;
        int innerLeftBracketsNum = 0;
        int innerRightBracketsNum = 0;

        boolean hasInnerClass = false;
        List<String> words = Arrays.asList(javaFileContentString.split(" "));

        String className = "";
        String innerClassName = "";
        StringBuilder classContentString = new StringBuilder();
        StringBuilder innerClassContentString = new StringBuilder();

        for (int i = 0; i < words.size(); i++) {
            String word = words.get(i);

            if (!hasInnerClass) {
                if (word.contains("{")) {
                    for (int j = 0; j < word.length(); j++) {
                        if (word.charAt(j) == '{') {
                            leftBracketsNum++;
                        }
                    }
                }

                if (word.contains("}")) {
                    for (int j = 0; j < word.length(); j++) {
                        if (word.charAt(j) == '}') {
                            rightBracketsNum++;
                        }
                    }
                }

                if (word.equals("class")) {
                    // 判断内部类
                    if (leftBracketsNum != rightBracketsNum) {
                        hasInnerClass = true;
                    }

                    String newClassName;
                    // 类名与"{"未分开, 如: class Test{ ... }
                    if (words.get(i + 1).contains("{")) {
                        newClassName = words.get(i + 1).split("\\{")[0];
                        words.set(i + 1, "{");
                    } else {
                        newClassName = words.get(++i);
                    }

                    if (hasInnerClass) {
                        innerClassName = newClassName;
                    } else {
                        className = newClassName;
                    }
                    continue;
                }

                // 类结束
                if (leftBracketsNum == rightBracketsNum) {
                    leftBracketsNum = 0;
                    rightBracketsNum = 0;
                    if (!className.equals("") && !classContentString.toString().equals("")) {
                        List<MUTModel> tempMUTModelList = analyzeClassContentString(className, classContentString.toString());
                        if (tempMUTModelList != null) {
                            mutModelList.addAll(tempMUTModelList);
                        }
                    }
                    classContentString = new StringBuilder();
                } else {
                    classContentString.append(word).append(" ");
                }
            } else {
                if (word.contains("{")) {
                    for (int j = 0; j < word.length(); j++) {
                        if (word.charAt(j) == '{') {
                            innerLeftBracketsNum++;
                        }
                    }
                }

                if (word.contains("}")) {
                    for (int j = 0; j < word.length(); j++) {
                        if (word.charAt(j) == '}') {
                            innerRightBracketsNum++;
                        }
                    }
                }

                // 类结束
                if (innerLeftBracketsNum == innerRightBracketsNum) {
                    innerLeftBracketsNum = 0;
                    innerRightBracketsNum = 0;
                    hasInnerClass = false;
                    if (!className.equals("") && !innerClassContentString.toString().equals("")) {
                        List<MUTModel> tempMUTModelList = analyzeClassContentString(innerClassName, innerClassContentString.toString());
                        if (tempMUTModelList != null) {
                            mutModelList.addAll(tempMUTModelList);
                        }
                    }
                    innerClassContentString = new StringBuilder();
                } else {
                    innerClassContentString.append(word).append(" ");
                }
            }
        }

        if (mutModelList.size() > 0) {
            return mutModelList;
        }
        mutModelList = null;
        return null;
    }

    /**
     * 分析切割出的每一个类的内容字符串, 切割方法
     *
     * @param className          类名
     * @param classContentString 类的内容字符串
     * @author sunzesong
     */
    private static List<MUTModel> analyzeClassContentString(String className, String classContentString) {
        List<String> words = Arrays.asList(classContentString.split(" "));
        int bracketsNum = 0; // 未匹配的"{"个数
        StringBuilder resString = new StringBuilder(); // 处理后的字符串

        List<MUTModel> mutModelList = new ArrayList<MUTModel>();

        for (int i = 1; i < words.size(); i++) { // 忽略第一个"{"
            String word = words.get(i);
            if (word.contains("{")) {
                for (int j = 0; j < word.length(); j++) {
                    if (word.charAt(j) == '{') {
                        bracketsNum++;
                    }
                }
            }

            if (word.contains("}")) {
                for (int j = 0; j < word.length(); j++) {
                    if (word.charAt(j) == '}') {
                        bracketsNum--;
                    }
                }
            }

            if (bracketsNum == 0 && !word.equals("}")) {
                resString.append(word).append(" ");
            }

            if (word.contains(";")) {
                resString = new StringBuilder();
            } else if (word.contains(")") && !resString.toString().equals("")) {
                if (!resString.toString().contains("@")) {
                    MUTModel tempMUTModel = analyzeMethodContentString(className, resString.toString());
                    mutModelList.add(tempMUTModel);
                }
                resString = new StringBuilder();
            }
        }
        if (mutModelList.size() > 0) {
            return mutModelList;
        }
        mutModelList = null;
        return null;
    }

    /**
     * 分析切割出的每一个方法的内容字符串, 保存到数据库
     *
     * @param className           类名
     * @param methodContentString 方法的内容字符串
     * @author sunzesong
     */
    private static MUTModel analyzeMethodContentString(String className, String methodContentString) {
        MUTModel mutModel = new MUTModel();
        mutModel.setClassName(className);
        mutModel.setAccess("");

        List<String> words = Arrays.asList(methodContentString.split(" "));
        List<String> arguments = new ArrayList<>();
        int judgeIndex = 0; // 判断类名前是否为返回值类型, 以判断是否为构造函数

        for (int i = 0; i < words.size(); i++) {
            String word = words.get(i);
            if (word.equals("public") || word.equals("private") || word.equals("protected")) {
                mutModel.setAccess(word);
            } else if (word.equals("(")) {
                mutModel.setMethodName(words.get(i - 1));
                arguments.add(words.get(i + 1));
                judgeIndex = i - 2;
            } else if (word.endsWith("(")) {
                mutModel.setMethodName(word.split("\\(")[0]);
                arguments.add(words.get(i + 1));
                judgeIndex = i - 1;
            } else if (word.startsWith("(")) {
                mutModel.setMethodName(words.get(i - 1));
                judgeIndex = i - 2;
            } else if (word.contains("(") && !word.endsWith("(")) {
                mutModel.setMethodName(word.split("\\(")[0]);
                judgeIndex = i - 1;

                if (!word.split("\\(")[1].equals(")")) {
                    arguments.add(word.split("\\(")[1]);
                }
            } else if (word.equals(",") || word.endsWith(",")) {
                arguments.add(words.get(i + 1));
            } else if (word.contains(",") && !word.endsWith(",")) {
                arguments.add(word.split(",")[1]);
            }
        }

        StringBuilder argumentsWithComa = new StringBuilder();
        StringBuilder argumentsWithoutComa = new StringBuilder();

        for (String argument : arguments) {
            argumentsWithComa.append(argument).append(",");
            argumentsWithoutComa.append(argument);
        }

        if (argumentsWithComa.toString().length() == 0) {
            mutModel.setArguments("");
        } else {
            mutModel.setArguments(argumentsWithComa.toString().substring(0, argumentsWithComa.toString().length() - 1));
        }

        mutModel.setIsConstructor("");
        if (judgeIndex < 0 || words.get(judgeIndex).equals(mutModel.getAccess())
                || words.get(judgeIndex).equals("static")) {
            mutModel.setIsConstructor("constructor");
        }

        String midString = className + mutModel.getMethodName() + argumentsWithoutComa;
        mutModel.setMethodId(midString.hashCode());
        return mutModel;
    }

    /**
     * @param PUTRootDirectory PUT根目录路径
     * @return FUT文件名列表
     * @date 2018/4/9 下午7:53
     * @author sunweisong
     */
    static List<String> getAllFUTNameFromPUT(File PUTRootDirectory) {
        File[] directories = PUTRootDirectory.listFiles();
        if (directories != null) {
            for (File directory : directories) {
                if (directory.isFile()) {
                    continue;
                }
                String directoryName = directory.getName();
                if ("src".equals(directoryName)) {
                    File[] files = directory.listFiles();
                    if (files != null && files.length > 0) {
                        List<String> fileNameList = new ArrayList<>();
                        for (File file : files) {
                            String fileName = file.getName();
                            if (".DS_Store".equals(fileName)) {
                                continue;
                            }
                            int lastPointIndex = fileName.lastIndexOf(".");
                            String fileExtension = fileName.substring(lastPointIndex + 1, fileName.length());
                            if ("java".equals(fileExtension)) {
                                fileNameList.add(fileName);
                            }
                        }
                        return fileNameList;
                    } else {
                        System.err.println("The directory \"" + directory.getAbsolutePath() + "\" is empty.");
                        return null;
                    }
                }
            }
        }
        return null;
    }


}
