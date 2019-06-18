package com.maf.analysis;



import com.maf.model.*;
import com.maf.util.*;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TPAnalysis {

    private static List<MUTModel> mutModelList;

    /**
     * The path of the directory of test fragments.
     */
    private static String pathOfTestFragment = "/Users/sunweisong/Desktop/Test Recommendation Experimental Data/Test Fragments";

    /**
      * This function is responsible for initializing the list "mutModelList".
      * @param pathOfPUT: the path of the directory of the program under test
      * @return
      * @throws
      * @date 2019/6/14 1:42 PM
      * @author sunweisong
      */
    private static void initialize (String pathOfPUT) {
        mutModelList = getMUTFromPUT(pathOfPUT);
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2019/6/14 1:37 PM
     * @author sunweisong
     */
    private static List<MUTModel> getMUTFromPUT(String PUTDirectoryPath) {
        StringBuffer pathBuffer = new StringBuffer(PUTDirectoryPath);
        pathBuffer.append(File.separator + "src/main/java/net/mooctest/mut.json");
        String targetFilePath = pathBuffer.toString();
        List<String> mutStringList = FileUtil.readFileContentToListByLine(targetFilePath);
        List<MUTModel> mutModelList = new ArrayList<>();
        if (mutStringList != null) {
            for (String mutString : mutStringList) {
                MUTModel mutModel = JacksonUtil.json2Bean(mutString, MUTModel.class);
                mutModelList.add(mutModel);
            }
        }
        if (mutModelList.size() == 0) {
            mutModelList = null;
        }
        pathBuffer = null;
        return mutModelList;
    }

    /**
      * 
      * @param pathOfPUT: the path of the directory of the program under test.
      * @param pathOfTP: the path of the directory of the test program.
      * @return
      * @throws
      * @date 2019/6/13 5:28 PM
      * @author sunweisong
      */
    public static void extractTestFragmentsFromTestProgram(String pathOfPUT, String pathOfTP) {

        initialize(pathOfPUT);

        File directoryOfTP = new File(pathOfTP);
        if (!directoryOfTP.exists()) {
            System.err.println("The directory doesn't exist!");
            return;
        }
        StringBuffer bufferOfPathOfPreservationOfTestFragment = new StringBuffer(pathOfTestFragment);
        bufferOfPathOfPreservationOfTestFragment.append(File.separator + directoryOfTP.getName() + "_By_SID");
        for (File directoryOfSID : directoryOfTP.listFiles()) {
            String studentID = directoryOfSID.getName();
            if (".DS_Store".equals(studentID) || directoryOfSID.isFile()) {
                continue;
            }
//            if (!"40670".equals(studentID)) {
//                continue;
//            }
            System.out.println("studentID :" + studentID);
            List<TestFileModel> testFileModelList = traverseDirectoryOfSID(directoryOfSID);
            if (testFileModelList == null) {
                continue;
            }
            bufferOfPathOfPreservationOfTestFragment.append(File.separator + studentID);
            File targetDirectory = new File(bufferOfPathOfPreservationOfTestFragment.toString());
            if (!targetDirectory.exists()) {
                targetDirectory.mkdir();
                targetDirectory = null;
            }
            bufferOfPathOfPreservationOfTestFragment.append(File.separator + "test_fragments.json");
            List<String> tfList = convertToTFInfoModel(testFileModelList);
            FileUtil.writeTestFragmentsToTargetFile(tfList, bufferOfPathOfPreservationOfTestFragment.toString());
            int start = bufferOfPathOfPreservationOfTestFragment.indexOf(File.separator + studentID);
            bufferOfPathOfPreservationOfTestFragment.replace(start
                    , bufferOfPathOfPreservationOfTestFragment.length(), "");
            testFileModelList = null;
        }
        bufferOfPathOfPreservationOfTestFragment = null;
        directoryOfTP = null;
    }
    
    /**
      *
      * @param 
      * @return
      * @throws
      * @date 2019/6/14 8:11 PM
      * @author sunweisong
      */
    private static List<String> convertToTFInfoModel(List<TestFileModel> testFileModelList) {
        List<TFInfoModel> tfInfoModelList = new ArrayList<>();
        for (TestFileModel testFileModel : testFileModelList) {
            String testFileName = testFileModel.getFileName();
            List<InvokeMethodModel> invokeMethodModelList = testFileModel.getTestMethodList();
            StringBuffer tfBuffer = new StringBuffer();
            for (InvokeMethodModel invokeMethodModel : invokeMethodModelList) {
                TFInfoModel tfInfoModel = new TFInfoModel();
                tfInfoModel.setFileName(testFileName);
                List<StatementModel> relatedInstantiateStatementList = invokeMethodModel.getRelatedInstantiateStatementList();
                List<StatementModel> relatedParameterizedStatementList = invokeMethodModel.getRelatedParameterizedStatementList();
                List<StatementModel> relatedInvokedStatementList = invokeMethodModel.getRelatedInvokedStatementList();
                tfInfoModel.setMethodId(invokeMethodModel.getmId());
                tfInfoModel.setMethodName(invokeMethodModel.getMethodName());
                tfInfoModel.setClassName(invokeMethodModel.getCutName());
                if (relatedInstantiateStatementList != null) {
                    for (int i = relatedInstantiateStatementList.size() - 1; i >= 0; i--) {
                        tfBuffer.append(relatedInstantiateStatementList.get(i).getContent() + System.lineSeparator());
                    }
                }
                if (relatedParameterizedStatementList != null) {
                    int relatedParameterizedStatementListSize = relatedParameterizedStatementList.size();
                    for (int i = relatedParameterizedStatementListSize - 1; i >= 0; i--) {
                        tfBuffer.append(relatedParameterizedStatementList.get(i).getContent() + System.lineSeparator());
                    }
                }
                if (relatedInvokedStatementList != null) {
                    for (int i = 0; i < relatedInvokedStatementList.size(); i++) {
                        tfBuffer.append(relatedInvokedStatementList.get(i).getContent() + System.lineSeparator());
                    }
                }
                tfInfoModel.setTestFragment(tfBuffer.toString());
                tfInfoModelList.add(tfInfoModel);
                tfBuffer.setLength(0);
            }
            tfBuffer = null;
        }

        List<String> tfList = new ArrayList<>(tfInfoModelList.size());
        for (TFInfoModel tfInfoModel : tfInfoModelList) {
            tfList.add(JacksonUtil.bean2Json(tfInfoModel));
        }
        return tfList;
    }

    /**
     * traverse the directory of the submission.
     * @param directoryOfSID
     * @date 2018/4/4 上午10:38
     * @author sunweisong
     */
    private static List<TestFileModel> traverseDirectoryOfSID(File directoryOfSID) {
        File[] arrayOfDirectoryOfSubmission = directoryOfSID.listFiles();
        if (arrayOfDirectoryOfSubmission.length == 0) {
            System.out.println("The directory \"" + directoryOfSID.getAbsolutePath() + "\" is empty.");
            return null;
        }
        List<TestFileModel> testFileModelList = null;
        for (File directoryOfSubmission : arrayOfDirectoryOfSubmission) {
            if (directoryOfSubmission.isFile()) {
                continue;
            }
            if (".DS_Store".equals(directoryOfSubmission.getName())) {
                continue;
            }
            testFileModelList = traverseSubmitDirectory(directoryOfSubmission);
            break;
        }
        return testFileModelList;
    }

    /**
     * @param directoryOfSubmission
     * @return void
     * @date 2018/4/9 下午5:12
     * @author sunweisong
     */
    private static List<TestFileModel> traverseSubmitDirectory(File directoryOfSubmission) {
        File[] directories = directoryOfSubmission.listFiles();
        if (directories.length == 0) {
            System.out.println("The directory \"" + directoryOfSubmission.getAbsolutePath() + "\" is empty.");
            return null;
        }
        List<TestFileModel> testFileModelList = null;
        for (File directory : directories) {
            if (directory.isFile()) {
                continue;
            }
            String directoryName = directory.getName();
            if (!directoryName.startsWith("result")) {
                continue;
            }
            testFileModelList = traverseExecuteDirectory(directory);
            break;
        }
        return testFileModelList;
    }

    /**
     * @param directoryOfExecution
     * @return void
     * @date 2018/4/9 下午5:21
     * @author sunweisong
     */
    private static List<TestFileModel> traverseExecuteDirectory(File directoryOfExecution) {
        StringBuffer testFileDirectoryPathBuffer = new StringBuffer(directoryOfExecution.getAbsolutePath());
        testFileDirectoryPathBuffer.append(File.separator + "src/test/java/net/mooctest");
        File testFileDirectory = new File(testFileDirectoryPathBuffer.toString());
        File[] testFiles = testFileDirectory.listFiles();
        if (testFiles.length == 0) {
            return null;
        }
        List<TestFileModel> allTestFileModelList = new ArrayList<>();
        for (File testFile : testFiles) {
            if (testFile.isDirectory()) {
                continue;
            }
            String testFileName = testFile.getName();
            if (".DS_Store".equals(testFileName)) {
                continue;
            }
            TestFileModel testFileModel = new TestFileModel(testFileName);
            List<InvokeMethodModel> testMethodList = analyzeTestFile(testFile);
            if (testMethodList != null) {
                testFileModel.setTestMethodList(testMethodList);
                allTestFileModelList.add(testFileModel);
            }
        }
        testFiles = null;
        testFileDirectory = null;
        testFileDirectoryPathBuffer = null;
        if (allTestFileModelList.size() == 0) {
            allTestFileModelList = null;
        }
        return allTestFileModelList;
    }


    /**
     * @param testFile
     * @return void
     * @date 2018/4/9 下午6:07
     * @author sunweisong
     */
    private static List<InvokeMethodModel> analyzeTestFile(File testFile) {
        String contentStringOfTestFile = FileUtil.readFileContentToString(testFile);
        if ("".equals(contentStringOfTestFile)) {
            System.err.println("The file \"" + testFile.getAbsolutePath() + "\" is empty!");
            return null;
        }
        boolean isParenthesisMatch = ParenthesisUtil.judgeParenthesisMatchAmongString(contentStringOfTestFile);
        if (!isParenthesisMatch) {
            System.err.println("The parenthesis in the test file is mismatched!");
            return null;
        }
        return analyzeTestFileContentString(contentStringOfTestFile);
    }

    /**
      *
      * @param testFileContentString
      * @return
      * @throws
      * @date 2018/4/11 下午7:40
      * @author sunweisong
      */
    private static List<InvokeMethodModel> analyzeTestFileContentString(String testFileContentString) {
        List<String> testCaseStringList = extractMethodWithJUnitAnnotationFromString(testFileContentString);
        if (testCaseStringList.size() == 0) {
            return null;
        }

        List<InvokeMethodModel> allInvokeMethodModelList = null;
        List<InvokeMethodModel> invokeMethodModelList = new ArrayList<>();
        List<InvokeMethodModel> invokeMethodModelAmongTryBlockList = new ArrayList<>();

        for (String testCaseString : testCaseStringList) {
            if (!testCaseString.contains("@Test") && !testCaseString.contains("@org.junit.Test")) {
                continue;
            }
            String testCaseName = extractTestCaseNameFromTestCaseString(testCaseString);


            List<String> tryCatchBlockList = null;
            if (testCaseString.contains("try")) {
                tryCatchBlockList = extractTryCatchBlockFromTestCaseString(testCaseString);
            }
            if (tryCatchBlockList != null) {
                for (String tryCatchBlock : tryCatchBlockList) {

                    List<String> statementAmongTryCatchBlockList = extractStatementFromTryCatchBlock(tryCatchBlock);

                    if (statementAmongTryCatchBlockList == null) {
                        continue;
                    }
                    List<String> statementContainsInvokeMethodList = getSentenceContainsInvokeMethod(statementAmongTryCatchBlockList);
                    if (statementContainsInvokeMethodList == null) {
                        continue;
                    }
                    initInvokeMethod(statementContainsInvokeMethodList, testCaseName
                            , invokeMethodModelAmongTryBlockList);
                    if (invokeMethodModelAmongTryBlockList.size() > 0) {
                        List<InvokeMethodModel> testMethodList = analyzeInvokeMethodFromTryBlockAmongTestFile(invokeMethodModelAmongTryBlockList
                                , testCaseString
                                , tryCatchBlock
                                , statementAmongTryCatchBlockList
                                , tryCatchBlockList
                                , testCaseStringList
                                , testFileContentString);
                        if (testMethodList != null) {
                            if (allInvokeMethodModelList == null) {
                                allInvokeMethodModelList = new ArrayList<>();
                            }
                            allInvokeMethodModelList.addAll(testMethodList);
                        }
                    }
                    testCaseString = testCaseString.replace(tryCatchBlock, "");
                    invokeMethodModelAmongTryBlockList.clear();
                }
            }

            List<String> sentenceAmongTestCaseStringList = getSentenceEndedWithSemicolonFromString(testCaseString);

            if (sentenceAmongTestCaseStringList == null) {
                continue;
            }

            List<String> sentenceContainsInvokeMethodList = getSentenceContainsInvokeMethod(sentenceAmongTestCaseStringList);

            int sentenceContainsInvokeMethodListSize = sentenceContainsInvokeMethodList.size();
            if (sentenceContainsInvokeMethodListSize == 0) {
                continue;
            }
            initInvokeMethod(sentenceContainsInvokeMethodList, testCaseName
                    , invokeMethodModelList);
            if (invokeMethodModelList.size() > 0) {
                List<InvokeMethodModel> testMethodList = analyzeInvokeMethodAmongTestFile(invokeMethodModelList
                        ,testCaseString, testCaseStringList, testFileContentString);
                if (allInvokeMethodModelList == null) {
                    allInvokeMethodModelList = new ArrayList<>();
                }
                allInvokeMethodModelList.addAll(testMethodList);
                invokeMethodModelList.clear();
            }
        }

        return allInvokeMethodModelList;
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2018/4/25 下午7:14
     * @author sunweisong
     */
    private static List<InvokeMethodModel> analyzeInvokeMethodFromTryBlockAmongTestFile(List<InvokeMethodModel> invokeMethodModelList
            , String testCaseString
            , String tryCatchBlock
            , List<String> statementAmongTryCatchBlockList
            , List<String> tryCatchBlockList
            , List<String> testCaseStringList
            , String testFileContentString) {
        List<InvokeMethodModel> mutList = new ArrayList<>();
        boolean isStatic = false;
        for (InvokeMethodModel invokeMethodModel : invokeMethodModelList) {
            String object = invokeMethodModel.getObject();
            String testCaseName = invokeMethodModel.getTestCaseName();
            if (!object.contains("(") && !object.contains(")")) {

                // search cutName among try block
                String cutName = searchCutNameAmongTryBlock(object
                        , statementAmongTryCatchBlockList);

                // search cutName among testCaseString
                if (cutName == null) {
                    cutName = searchCutNameExcludeTryCatchBlockAmongTestCaseString(object
                            , testCaseName, tryCatchBlockList, testCaseStringList);
                }
                // search cutName among Before or BeforeClass
                if (cutName == null) {
                    cutName = searchCutNameAmongBeforeTestCase(object
                            , testCaseStringList);
                }
                if (cutName == null) {
                    // search cutName among testFileContentString
                    cutName = searchCutNameAmongTestFileContentString(object
                            , testCaseStringList, testFileContentString);
                }

                if (cutName != null) {
                    invokeMethodModel.setCutName(cutName);
                } else {
                    String regex = "^[A-Z].*";
                    if (Pattern.matches(regex, object)) {
                        isStatic = true;
                        invokeMethodModel.setCutName(object);
                    }
                }
            } else {
                if (object.startsWith("new ")) {
                    int newStringIndex = object.indexOf("new ");
                    int leftIndex = object.indexOf("(");
                    String cutName = object.substring(newStringIndex + 4, leftIndex).trim();
                    invokeMethodModel.setCutName(cutName);
                }
            }
            // search the type of parameter
            List<ParameterModel> parameterList = invokeMethodModel.getParameterList();
            if (parameterList != null) {
                setParameterTypeInvokeMethodFromTryBlock(parameterList
                        , tryCatchBlockList, statementAmongTryCatchBlockList
                        , testCaseName
                        , testCaseStringList, testFileContentString);
            }
            if (invokeMethodModel.getCutName() == null) {
                continue;
            }
            List<Integer> midList = calculateMIDWithInvokeMethodModel(invokeMethodModel);
            for (MUTModel mutModel : mutModelList) {
                int mutMid = mutModel.getMethodId();
                for (int mid : midList) {
                    if (mid == mutMid) {
                        invokeMethodModel.setmId(mutMid);
                        // search related test sentence (test fragment)
                        extractRelatedSentenceExcludeTryCatchBlockFromTestFile(invokeMethodModel
                                , testCaseString, testCaseStringList
                                , testFileContentString, isStatic
                                , tryCatchBlock, tryCatchBlockList);
                        mutList.add(invokeMethodModel);
                    }
                }
            }
            isStatic = false;
        }
        return mutList;
    }

    /**
      * 
      * @param 
      * @return
      * @throws
      * @date 2018/6/6 下午2:45
      * @author sunweisong
      */
    private static void extractRelatedSentenceExcludeTryCatchBlockFromTestFile(InvokeMethodModel invokeMethodModel
            , String testCaseString, List<String> testCaseStringList
            , String testFileString, boolean isStatic
            , String tryCatchBlock
            , List<String> tryCatchBlockList) {

        String invokeMethodSentence = invokeMethodModel.getInvokeMethod();
        for (String tempTestCaseString : testCaseStringList) {
            testFileString = testFileString.replace(tempTestCaseString, "");
        }

        if (testFileString.contains("){")
                || testFileString.contains(") {")
                || testFileString.contains(") throws Exception {")
                || testFileString.contains(")throws Exception {")
                || testFileString.contains(") throws Exception{")
                || testFileString.contains(")throws Exception{")) {
            testFileString = removeFunctionsCreatedByUserFromString(testFileString);
        }

        List<StatementModel> sentenceAmongTestFileStringList = extractStatementModelFromString(testFileString);

        if (!isStatic) {
            // search instantiate statement
            String instance = invokeMethodModel.getObject();
            List<StatementModel> instantiateStatementModelList = searchInstantiateStatement(instance
                    , invokeMethodSentence
                    , testCaseString, testCaseStringList
                    , sentenceAmongTestFileStringList
                    , true
                    , tryCatchBlock
                    , tryCatchBlockList);
            if (instantiateStatementModelList != null) {
                invokeMethodModel.setRelatedInstantiateStatementList(instantiateStatementModelList);
            }
        }

        // search parameterized statement
        List<ParameterModel> parameterList = invokeMethodModel.getParameterList();
        if (parameterList != null) {
            List<StatementModel> parameterizedStatementModelList = searchParameterizedStatement(parameterList
                    , invokeMethodSentence
                    , testCaseString, testCaseStringList
                    , sentenceAmongTestFileStringList
                    , true
                    , tryCatchBlock
                    , tryCatchBlockList);
            if (parameterizedStatementModelList != null) {
                invokeMethodModel.setRelatedParameterizedStatementList(parameterizedStatementModelList);
            }
        }
        // search invoked statement
        List<StatementModel> invokedStatementModelList = searchInvokedStatement(invokeMethodSentence
                , testCaseString
                , true
                , tryCatchBlock
                , tryCatchBlockList);

        if (invokedStatementModelList != null) {
            invokeMethodModel.setRelatedInvokedStatementList(invokedStatementModelList);
        }
    }


    /**
      * 
      * @param 
      * @return
      * @throws
      * @date 2018/6/6 下午2:45
      * @author sunweisong
      */
    private static void setParameterTypeInvokeMethodFromTryBlock(List<ParameterModel> parameterList
            , List<String> tryCatchBlockList
            , List<String> statementAmongTryCatchBlockList
            , String testCaseName
            , List<String> testCaseStringList
            , String testFileContentString) {
        for (ParameterModel parameterModel : parameterList) {
            String parameterType = parameterModel.getType();
            if (parameterType == null) {
                String parameterName = parameterModel.getName();
                if ("null".equals(parameterName)) {
                    continue;
                }
                boolean flag = false;
                Matcher matcher = Pattern.compile("\\s*\\w+\\.\\w+\\(.*?\\)").matcher(parameterName);
                if (matcher.find()) {
                    int start = matcher.start(0);
                    if (start == 0) {
                        flag = true;
                    }
                }
                if (!flag) {
                    if (parameterName.contains("new ")) {
                        int leftParenthesesIndex = 0;
                        if (parameterName.contains("(")) {
                            leftParenthesesIndex = parameterName.indexOf("(");
                        }
                        if (parameterName.contains("[")) {
                            String regex = "\\s*new\\s\\w+\\[.*?\\]";
                            Matcher matcher1 = Pattern.compile(regex).matcher(parameterName);
                            if (matcher1.find()) {
                                int index1 = parameterName.indexOf("[");
                                if (leftParenthesesIndex > 0) {
                                    if (index1 < leftParenthesesIndex) {
                                        leftParenthesesIndex = parameterName.indexOf("]");;
                                    }
                                } else {
                                    leftParenthesesIndex = parameterName.indexOf("]");
                                }
                            }
                        }
                        int newStringIndex = parameterName.indexOf("new ");
                        parameterType = parameterName.substring(newStringIndex + 4
                                , leftParenthesesIndex).trim();
                    }
                    if (parameterType != null) {
                        parameterModel.setType(parameterType);
                        continue;
                    }
                    // search cutName among try block string
                    parameterType = searchCutNameAmongTryBlock(parameterName
                            , statementAmongTryCatchBlockList);
                    // search cutName among test case string
                    if (parameterType == null) {
                        parameterType = searchCutNameExcludeTryCatchBlockAmongTestCaseString(parameterName
                                , testCaseName, tryCatchBlockList, testCaseStringList);
                    }
                    // search cutName among Before or BeforeClass
                    if (parameterType == null) {
                        parameterType = searchCutNameAmongBeforeTestCase(parameterName
                                , testCaseStringList);
                    }
                    // search cutName among test file
                    if (parameterType == null) {
                        parameterType = searchCutNameAmongTestFileContentString(parameterName
                                , testCaseStringList, testFileContentString);
                    }
                    if (parameterType != null) {
                        parameterModel.setType(parameterType);
                    }
                }
            }
        }
    }
    
    /**
      * 
      * @param 
      * @return
      * @throws
      * @date 2018/6/6 下午2:33
      * @author sunweisong
      */
    private static String searchCutNameExcludeTryCatchBlockAmongTestCaseString(String object
            , String testCaseName
            , List<String> tryCatchBlockList
            , List<String> testCaseStringList) {
        String cutName = null;
        for (String testCaseString : testCaseStringList) {
            if (testCaseString.contains("@Before")
                    || testCaseString.contains("@org.junit.Before")
                    || testCaseString.contains("@After")
                    || testCaseString.contains("@org.junit.After")) {
                continue;
            }
            String tempTestCaseName = extractTestCaseNameFromTestCaseString(testCaseString);
            if (tempTestCaseName.equals(testCaseName)) {
                for (String tryCatchBlock : tryCatchBlockList) {
                    testCaseString = testCaseString.replace(tryCatchBlock, "");
                }
                List<String> sentenceAmongTestCaseStringList = getSentenceEndedWithSemicolonFromString(testCaseString);
                if (sentenceAmongTestCaseStringList == null) {
                    break;
                }
                cutName = searchCutNameFromStatementList(object
                        , sentenceAmongTestCaseStringList);
            }
        }
        return cutName;
    }

    /**
      * 
      * @param 
      * @return
      * @throws
      * @date 2018/6/6 下午2:19
      * @author sunweisong
      */
    private static String searchCutNameAmongTryBlock(String instance
            , List<String> statementAmongTryCatchBlockList) {
        return searchCutNameFromStatementList(instance
                , statementAmongTryCatchBlockList);
    }
    
    /**
      * 
      * @param 
      * @return
      * @throws
      * @date 2018/6/6 下午2:30
      * @author sunweisong
      */
    private static String  searchCutNameFromStatementList(String instance
            , List<String> statementList) {
        for (String statement : statementList) {
            if (statement.contains(instance + ".")) {
                continue;
            }
            if (!isOnlyContainsInstanceAmongInitialStatement(instance, statement)) {
                continue;
            }
            String cutName = extractClassNameFromSentence(instance, statement);
            if (cutName != null && !"".equals(cutName)) {
                return cutName;
            }
        }
        return null;
    }

    /**
      * 
      * @param 
      * @return
      * @throws
      * @date 2018/6/6 下午2:01
      * @author sunweisong
      */
    private static void initInvokeMethod(List<String> sentenceContainsInvokeMethodList
            , String testCaseName
            , List<InvokeMethodModel> invokeMethodModelList) {
        for (String sentenceContainsInvokeMethod : sentenceContainsInvokeMethodList) {
            String temp = sentenceContainsInvokeMethod;

            /**
             * find = "Assert.assertEquals(c.value(value),c2.value(value));"
             * 2018.06.05
             */
            int pointIndex1 = temp.indexOf(".");
            String stringBeforePoint = temp.substring(0, pointIndex1).trim();
            if ("Assert".equals(stringBeforePoint)
                    || "TestCase".equals(stringBeforePoint)) {
                int firstLeftIndex = temp.indexOf("(");
                int lastRightIndex = temp.lastIndexOf(")");
                temp = temp.substring(firstLeftIndex + 1, lastRightIndex);
                sentenceContainsInvokeMethod = temp;
            }

/**
 * assertEquals(false,new Datalog(predicate1,arguments1).compatibleWith(new Fact(predicate2,values1)));
 * new Fact(new Predicate("asd"),v).getPredicate();
 * 2018.05.16
 */
            if (temp.contains("\"")) {
                temp = removeContentsInQuotes(temp);
            }
            List<String> invokeStatementList = new ArrayList<>();

            /**
             * sentenceContainsInvokeMethod = s.extend(a[2].getVariable(),v );
             * 2018.05.11
             */
            if (!StringUtil.isParenthesesMatchInString(temp)) {
                temp = StringUtil.extractPartStringIsParenthesesMatch(temp);
            }

            if (StringUtil.isMoreThanOneLayerParenthesesInString(temp)) {
                temp = temp.replace(",", "|");
            }

            /**
             * sentenceContainsInvokeMethod = assertEquals(d.getArguments()[0], f.getArguments()[0]);
             * 2018.05,10
             */
            if (temp.contains(",")) {
                String regex = "\\s*\\w+\\.\\w+\\(.*?\\)";
                Matcher matcher = Pattern.compile(regex).matcher(sentenceContainsInvokeMethod);
                while (matcher.find()) {
                    String invokeStatement = matcher.group().trim();
                    if (invokeStatementList == null) {
                        invokeStatementList = new ArrayList<>();
                    }
                    invokeStatementList.add(invokeStatement);
                }
                for (String invokeStatement : invokeStatementList) {
                    if (invokeStatement.contains("System.out.println")) {
                        int tempIndex = invokeStatement.indexOf("(");
                        invokeStatement = invokeStatement.substring(tempIndex + 1);
                    }
                    InvokeMethodModel invokeMethodModel = new InvokeMethodModel(invokeStatement);
                    invokeMethodModel.setTestCaseName(testCaseName);
                    if (!invokeStatement.contains(".")) {
                        continue;
                    }
                    if (invokeStatement.contains("new ")) {
                        invokeStatement = getInstanceFromStringContainsNew(temp);
                    }

                    int pointIndex = invokeStatement.indexOf(".");
                    String instance = invokeStatement.substring(0, pointIndex);
                    invokeMethodModel.setObject(instance.trim());
                    extractNameAndParameterFromSentence(invokeMethodModel, invokeStatement);
                    invokeMethodModel.setInvokeTimes(1);
                    invokeMethodModelList.add(invokeMethodModel);
                }
                continue;
            }
            temp = sentenceContainsInvokeMethod;
            if (temp.contains("System.out.println")) {
                int tempIndex = temp.indexOf("(");
                temp = temp.substring(tempIndex + 1);
            }
            if (!temp.contains(".")) {
                continue;
            }

            if (temp.contains("\"")) {
                /**
                 * System.out.println("8.floatRepresentation = "+tempString);
                 * 2019.06.14
                 */
                String temp1 = removeContentsInQuotes(temp);
                if (!temp1.contains(".")) {
                    continue;
                }
            }

            if (temp.contains("new ")) {
                temp = getInstanceFromStringContainsNew(temp);
            }
            InvokeMethodModel invokeMethodModel = new InvokeMethodModel(sentenceContainsInvokeMethod);
            invokeMethodModel.setTestCaseName(testCaseName);
            int pointIndex = temp.indexOf(".");
            String instance = temp.substring(0, pointIndex);
            invokeMethodModel.setObject(instance.trim());
            extractNameAndParameterFromSentence(invokeMethodModel, temp);
            invokeMethodModel.setInvokeTimes(1);
            invokeMethodModelList.add(invokeMethodModel);
        }
    }

    /**
      * 
      * @param 
      * @return
      * @throws
      * @date 2018/6/6 上午11:18
      * @author sunweisong
      */
    private static String extractTryBlockFromTryCatchBlock(String tryCatchBlock) {
        int tryIndex = tryCatchBlock.indexOf("try");
        String temp = tryCatchBlock.substring(tryIndex);
        StringBuffer stringBuffer = new StringBuffer();
        char[] charArray = temp.toCharArray();
        int count = 0;
        for (char alpha : charArray) {
            stringBuffer.append(alpha);
            if (alpha == '{') {
                count++;
            }
            if (alpha == '}') {
                count--;
            }
            if (count == 0 && stringBuffer.indexOf("{") != -1) {
                break;
            }
        }
        return stringBuffer.toString();
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2018/6/6 上午11:18
     * @author sunweisong
     */
    private static List<StatementModel> extractStatementModelFromTryCatchBlock(String tryCatchBlock) {
        String tryBlock = extractTryBlockFromTryCatchBlock(tryCatchBlock);
        int start = tryBlock.indexOf("{");
        int end = tryBlock.lastIndexOf("}");
        String temp = tryBlock.substring(start + 1, end).trim();
        if ("".equals(temp)) {
            return null;
        }
        StringBuffer stringBuffer = new StringBuffer(" " + temp);
        temp = stringBuffer.toString();
        List<StatementModel> statementModelList = null;
        String regex = "((\\s\\w+)|(\\s\\w+\\s\\w+)|(\\s*\\w+\\.\\w+))(\\s*\\=\\s*new\\s\\w+(\\<.*?\\>)*\\(.*?\\)\\s*\\;)|"
                + "(\\s\\w+\\s\\w+)(\\<.*?\\>)*(\\;|\\s*\\=\\s*\\w+\\;)|"
                + "(\\s\\w+\\s*\\=\\s*\\w+\\;)|"
                + "(\\s\\w+\\s*\\=\\s*(.*?)\\;)|"
                + "(\\s*\\w+\\s\\w+\\s*\\=\\s*\\w+\\.\\w+\\(.*?\\)\\;)|"
                + "(\\s*\\w+\\s*\\=\\s*\\w+\\.\\w+\\(.*?\\)\\;)|"
                + "(\\s*\\w+\\[\\]\\s*\\w+\\s*\\=((\\s*new\\s\\w+\\[\\])|(\\s*))\\{.*?\\}\\;)|"
                + "(((\\s\\w+)|(\\s*\\w+\\.\\w+))(\\.\\w+\\(.*?\\)\\;))|"
                + "(\\s\\w+\\[\\]\\s\\w+\\s*\\=\\s*\\w+\\.\\w+\\(.*?\\)\\;)|"
                + "(\\s\\w+\\[\\]\\s\\w+\\;)|"
                + "((\\s\\w+\\[\\]\\s\\w+\\s*\\=\\s*new\\s\\w+\\[(.*?)\\]\\;))|"
                + "(\\s*\\w+\\[\\]\\s\\w+\\s*\\=\\s*new\\s\\w+\\(.*?\\)\\;)|"
                + "(\\s*\\w+\\s\\w+\\[\\]\\s*\\=\\s*(((()|(new\\s\\w+\\[\\]))\\{.*?\\})|(\\w+\\.\\w+\\(.*?\\)))\\;)|"
                + "(\\s*\\w+\\s*\\[\\]\\s*\\w+\\s*\\=\\s*\\{.*?\\}\\;)|"
                + "(\\s*\\w+\\s*\\[\\]\\s*\\w+\\s*\\=\\s*null\\s*\\;)|"
                + "(\\s*\\w+\\<.*?\\>\\s\\w+(.*?)\\;)|"
                + "(\\s*\\w+\\s\\w+\\s*\\;)|"
                + "(\\s*\\w+\\s\\w+\\s*\\=\\s*\\\"\\w+\\\"\\s*\\;)|"
                + "(\\s*\\w+\\s\\w+\\s*\\=\\s*(new)\\s\\w+\\(.*?\\)\\s*\\;)|"
                + "(\\s*\\w+\\s(\\w+\\,\\s*)+\\w+\\;)|"
                + "(\\s\\w+\\s*(\\(.*?\\))\\;)";
        Matcher matcher = Pattern.compile(regex).matcher(temp);
        int count = 0;
        while (matcher.find()) {
            String findString = matcher.group();

            Boolean isFailFunction = Pattern.matches("\\s*fail\\s*\\(.*?\\)\\;", findString);
            if (isFailFunction) {
                continue;
            }
            count++;

            if (findString.contains("for")) {
                while (true) {
                    if (isIdentifierAmongStatement("for", findString)) {
                        int leftBraceIndex = findString.indexOf("{");
                        if (leftBraceIndex != -1) {
                            findString = findString.substring(leftBraceIndex + 1);
                        } else {
                            leftBraceIndex = findString.indexOf(")");
                            findString = findString.substring(leftBraceIndex + 1);
                        }
                    } else {
                        break;
                    }
                }

            }
            if (findString.contains("if")) {
                /**
                 * 2018.06.19
                 */
                String regex2 = "\\s*if\\s*\\(.*?\\)\\s*\\{.*?\\}\\s*else\\s*\\{.*?\\}";
                Matcher matcher1 = Pattern.compile(regex2).matcher(findString);
                while (matcher1.find()) {
                    String findIfString = matcher1.group();
                    findString = findString.replace(findIfString, "");
                }

                while (true) {
                    if (isIdentifierAmongStatement("if", findString)) {
                        int tempIndex = findString.indexOf("{");
                        if (tempIndex == -1) {
                            tempIndex = findString.indexOf(")");
                        }
                        findString = findString.substring(tempIndex + 1);
                    } else {
                        break;
                    }
                    if (isIdentifierAmongStatement("else", findString)) {
                        int tempIndex = findString.indexOf("{");
                        findString = findString.substring(tempIndex + 1);
                    } else {
                        break;
                    }
                }

            }
            if (statementModelList == null) {
                statementModelList = new ArrayList<>();
            }

            StatementModel statementModel = new StatementModel(findString, count, 0);
            statementModelList.add(statementModel);
        }
        stringBuffer = null;
        if (statementModelList != null) {
            return statementModelList;
        }
        return null;
    }


    /**
      * 
      * @param 
      * @return
      * @throws
      * @date 2018/6/6 上午11:18
      * @author sunweisong
      */
    private static List<String> extractStatementFromTryCatchBlock(String tryCatchBlock) {
        String tryBlock = extractTryBlockFromTryCatchBlock(tryCatchBlock);
        int start = tryBlock.indexOf("{");
        int end = tryBlock.lastIndexOf("}");
        String temp = tryBlock.substring(start + 1, end).trim();
        if ("".equals(temp)) {
            return null;
        }
        StringBuffer stringBuffer = new StringBuffer(" " + temp);
        temp = stringBuffer.toString();
        List<String> sentences = new ArrayList<>();
        String regex = "((\\s\\w+)|(\\s\\w+\\s\\w+)|(\\s*\\w+\\.\\w+))(\\s*\\=\\s*new\\s\\w+(\\<.*?\\>)*\\(.*?\\)\\s*\\;)|"
                + "(\\s\\w+\\s\\w+)(\\<.*?\\>)*(\\;|\\s*\\=\\s*\\w+\\;)|"
                + "(\\s\\w+\\s*\\=\\s*\\w+\\;)|"
                + "(\\s\\w+\\s*\\=\\s*(.*?)\\;)|"
                + "(\\s*\\w+\\s\\w+\\s*\\=\\s*\\w+\\.\\w+\\(.*?\\)\\;)|"
                + "(\\s*\\w+\\s*\\=\\s*\\w+\\.\\w+\\(.*?\\)\\;)|"
                + "(\\s*\\w+\\[\\]\\s*\\w+\\s*\\=((\\s*new\\s\\w+\\[\\])|(\\s*))\\{.*?\\}\\;)|"
                + "(((\\s\\w+)|(\\s*\\w+\\.\\w+))(\\.\\w+\\(.*?\\)\\;))|"
                + "(\\s\\w+\\[\\]\\s\\w+\\s*\\=\\s*\\w+\\.\\w+\\(.*?\\)\\;)|"
                + "(\\s\\w+\\[\\]\\s\\w+\\;)|"
                + "((\\s\\w+\\[\\]\\s\\w+\\s*\\=\\s*new\\s\\w+\\[(.*?)\\]\\;))|"
                + "(\\s*\\w+\\[\\]\\s\\w+\\s*\\=\\s*new\\s\\w+\\(.*?\\)\\;)|"
                + "(\\s*\\w+\\s\\w+\\[\\]\\s*\\=\\s*(((()|(new\\s\\w+\\[\\]))\\{.*?\\})|(\\w+\\.\\w+\\(.*?\\)))\\;)|"
                + "(\\s*\\w+\\s*\\[\\]\\s*\\w+\\s*\\=\\s*\\{.*?\\}\\;)|"
                + "(\\s*\\w+\\s*\\[\\]\\s*\\w+\\s*\\=\\s*null\\s*\\;)|"
                + "(\\s*\\w+\\<.*?\\>\\s\\w+(.*?)\\;)|"
                + "(\\s*\\w+\\s\\w+\\s*\\;)|"
                + "(\\s*\\w+\\s\\w+\\s*\\=\\s*\\\"\\w+\\\"\\s*\\;)|"
                + "(\\s*\\w+\\s\\w+\\s*\\=\\s*(new)\\s\\w+\\(.*?\\)\\s*\\;)|"
                + "(\\s*\\w+\\s\\w+\\s*=\\s*\\w+\\s*\\;)|"
                + "(\\s*\\w+\\s(\\w+\\,\\s*)+\\w+\\;)|"
                + "(\\s\\w+\\s*(\\(.*?\\))\\;)";
        Matcher matcher = Pattern.compile(regex).matcher(temp);
        while (matcher.find()) {
            String findString = matcher.group();

            Boolean isFailFunction = Pattern.matches("\\s*fail\\s*\\(.*?\\)\\;", findString);
            if (isFailFunction) {
                continue;
            }

            if (findString.contains("for")) {
                while (true) {
                    if (isIdentifierAmongStatement("for", findString)) {
                        int leftBraceIndex = findString.indexOf("{");
                        if (leftBraceIndex != -1) {
                            findString = findString.substring(leftBraceIndex + 1);
                        } else {
                            leftBraceIndex = findString.indexOf(")");
                            findString = findString.substring(leftBraceIndex + 1);
                        }
                    } else {
                        break;
                    }
                }

            }
            if (findString.contains("if")) {
                /**
                 * 2018.06.19
                 */
                String regex2 = "\\s*if\\s*\\(.*?\\)\\s*\\{.*?\\}\\s*else\\s*\\{.*?\\}";
                Matcher matcher1 = Pattern.compile(regex2).matcher(findString);
                while (matcher1.find()) {
                    String findIfString = matcher1.group();
                    findString = findString.replace(findIfString, "");
                }

                while (true) {
                    if (isIdentifierAmongStatement("if", findString)) {
                        int tempIndex = findString.indexOf("{");
                        if (tempIndex == -1) {
                            tempIndex = findString.indexOf(")");
                        }
                        findString = findString.substring(tempIndex + 1);
                    } else {
                        break;
                    }
                    if (isIdentifierAmongStatement("else", findString)) {
                        int tempIndex = findString.indexOf("{");
                        findString = findString.substring(tempIndex + 1);
                    } else {
                        break;
                    }
                }

            }
            sentences.add(findString);
        }
        stringBuffer = null;
        if (sentences.size() > 0) {
            return sentences;
        }
        sentences = null;
        return null;
    }


    /**
      * 
      * @param 
      * @return
      * @throws
      * @date 2018/6/6 上午10:56
      * @author sunweisong
      */
    private static List<String> extractTryCatchBlockFromTestCaseString(String testCaseString) {
        List<String> tryCatchBlockList = new ArrayList<>();
        String regex = "\\s*(try)\\s*\\{.*?\\}\\s*(catch)\\s*\\(.*?\\)\\s*\\{.*?\\}";
        Matcher matcher = Pattern.compile(regex).matcher(testCaseString);
        while (matcher.find()) {
            String temp = matcher.group().trim();
            tryCatchBlockList.add(temp);
        }
        if (tryCatchBlockList.size() > 0) {
            return tryCatchBlockList;
        }
        tryCatchBlockList = null;
        return null;
    }

    /**
      * 
      * @param 
      * @return
      * @throws
      * @date 2018/5/17 下午12:30
      * @author sunweisong
      */
    private static String getInstanceFromStringContainsNew(String string) {
        /**
         * assertEquals(false,new Datalog(predicate1,arguments1).compatibleWith(new Fact(predicate2,values1)));
         * new Fact(new Predicate("asd"),v).getPredicate();
         * 2018.05.16
         */
        int pointIndex = string.indexOf(".");
        String temp = string.substring(0, pointIndex + 1);
        String stringAfterPoint = string.substring(pointIndex + 1);
        StringBuffer buffer = null;
        if (!StringUtil.isParenthesesMatchInString(temp)) {
            int newStringIndex = temp.indexOf("new ");
            temp = temp.substring(newStringIndex);
            buffer = new StringBuffer(temp);
            int count = 0;
            Matcher matcher = Pattern.compile("new ").matcher(temp);
            while(matcher.find()){
                count++;
            }
            if (count > 1) {
                count = 0;
                buffer.setLength(0);
                for (char alpha : temp.toCharArray()) {
                    buffer.append(alpha);
                    if (alpha == '(') {
                        count++;
                    }
                    if (alpha == ')') {
                        count--;
                    }
                    if (alpha == ',') {
                        if (count == 0 && buffer.length() > 0) {
                            String temp1 = buffer.toString();
                            if (!temp1.contains(".")) {
                                buffer.setLength(0);
                            }
                        }
                    }
                }

            }
        } else {
            buffer = new StringBuffer(temp);
        }
        if (buffer != null) {
            buffer.append(stringAfterPoint);
            temp = buffer.toString();
        }
        return temp;
    }

    /**
      *
      * @param
      * @return
      * @throws
      * @date 2018/4/26 下午5:20
      * @author sunweisong
      */
    private static void extractNameAndParameterFromSentence(InvokeMethodModel invokeMethodModel
            , String sentence) {
        /**
         *  sentence = thrown.expect(NullPointerException.class);
         *  2018.05.10
         */

        /**
         * datalog.getArguments().length);
         * predicate2.getName().equals(string2));
         * s.extend(a[2].getVariable(),v );
         * 2018.05.11
         */

        if (sentence.contains("\"")) {
            String regex = "\\\"(.*?)\\\"";
            Matcher matcher = Pattern.compile(regex).matcher(sentence);
            while (matcher.find()) {
                String find = matcher.group();
                if (find.contains("(")) {
                    find = find.replace("(", "$[$");
                }
                if (find.contains(")")) {
                    find = find.replace(")", "$]$");
                }
                sentence = sentence.replace(matcher.group(), find);
            }
        }

        if (!StringUtil.isParenthesesMatchInString(sentence)) {
            sentence = StringUtil.extractPartStringIsParenthesesMatch(sentence);
        }

        String temp = sentence;
        // extract method name
        int pointIndex = temp.indexOf(".");
        temp = temp.substring(pointIndex + 1);
        int firstLeftParenthesesIndex = temp.indexOf("(");
        temp = temp.substring(0, firstLeftParenthesesIndex);
        invokeMethodModel.setMethodName(temp.trim());
        // extract parameters
        int tempStringIndex = sentence.indexOf(temp);
        List<ParameterModel> parameterModelList = null;
        temp = sentence.substring(tempStringIndex);
        if (StringUtil.isMoreThanOnePointInString(sentence)) {
            temp = StringUtil.extractInvokeSentenceInString(temp);
        }
        firstLeftParenthesesIndex = temp.indexOf("(");
        int lastRightParenthesesIndex = temp.lastIndexOf(")");
        if (firstLeftParenthesesIndex + 1 != lastRightParenthesesIndex) {
            temp = temp.substring(firstLeftParenthesesIndex + 1
                    , lastRightParenthesesIndex).trim();
            /**
             * s.extend(new Variable("var"), new Value("v1"));
             * 2018.05.10
             */
//            String myTempString1 = temp;
//            if (myTempString1.contains("\"")) {
//                myTempString1 = removeContentsInQuotes(myTempString1);
//            }
//            if (!StringUtil.isParenthesesMatchInString(myTempString1)) {
//                if (temp.contains(")")) {
//                    temp = StringUtil.removeParenthesesAtStringEnd(temp);
//                }
//            }

            String parameterContent = temp;
            if (!"".equals(temp)) {
                if (temp.contains("\"")) {
                    temp = removeContentsInQuotes(temp);
                }
                if ("".equals(temp)) {
                    ParameterModel parameterModel = new ParameterModel();
                    parameterModel.setOrder(1);
                    parameterModel.setName(parameterContent);
                    parameterModel.setType("String");
                    if (parameterModelList == null) {
                        parameterModelList = new ArrayList<>();
                    }
                    parameterModelList.add(parameterModel);
                    invokeMethodModel.setParameterList(parameterModelList);
                } else {
                    /**
                     * new Fact(p,v)
                     * new Fact(p,new Value[]{new Value("v2")})
                     * new Class[]{ Value.class,Variable.class,boolean.class }
                     * 2018.05.16
                     */
                    if (StringUtil.isMoreThanOneLayerParenthesesInString(temp)) {
                        if (temp.contains(",")) {
                            temp = temp.replace(",", "|");
                        }
                    } else {
                        if (!isMoreThanOneSetOfParenthesesAmongString(temp)) {
                            if (temp.indexOf("(") != -1) {
                                if (temp.contains(",")) {
                                    temp = temp.replace(",", "|");
                                }
                            }
                        }
                    }
                    if (temp.contains(",")) {
                        String[] parameterArray = parameterContent.split(",");
                        int count = 0;
                        for (String parameter : parameterArray) {
                            count++;
                            ParameterModel parameterModel = new ParameterModel();
                            parameterModel.setOrder(count);
                            parameterModel.setName(parameter.trim());
                            if (parameter.contains("\"")) {
                                String tempParameter = removeContentsInQuotes(parameter);
                                if ("".equals(tempParameter)) {
                                    parameterModel.setType("String");
                                } else {
                                    if (parameter.contains("new ")) {
                                        String type = extractParameterTypeFromString(parameter);
                                        parameterModel.setType(type);
                                    }
                                }
                            }
                            if (parameterModelList == null) {
                                parameterModelList = new ArrayList<>();
                            }
                            parameterModelList.add(parameterModel);
                        }
                    } else {
                        ParameterModel parameterModel = new ParameterModel();
                        parameterModel.setOrder(1);
                        String parameterString = parameterContent.trim();
                        parameterModel.setName(parameterString);
                        /**
                         * sentence = "res.add(test1.extend(new Variable(""), new Value("")));"
                         * parameterContent = "test1.extend(new Variable(""), new Value(""))";
                         * 2018.05.30
                         */
                        boolean flag = false;
                        Matcher matcher = Pattern.compile("\\s*\\w+\\.\\w+\\(.*?\\)").matcher(parameterString);
                        if (matcher.find()) {
                            int start = matcher.start(0);
                            if (start == 0) {
                                flag = true;
                            }
                        }
                        if (!flag) {
                            if (parameterContent.contains("new ")) {
                                String type = extractParameterTypeFromString(parameterContent);
                                parameterModel.setType(type);
                            }
                        }
                        if (parameterModelList == null) {
                            parameterModelList = new ArrayList<>();
                        }
                        parameterModelList.add(parameterModel);
                    }
                    invokeMethodModel.setParameterList(parameterModelList);
                }
            }
        }
    }

    /**
      * 
      * @param 
      * @return
      * @throws
      * @date 2018/5/30 上午11:25
      * @author sunweisong
      */
    private static String extractParameterTypeFromString(String parameterContent) {
        int newIndex = parameterContent.indexOf("new ");
        int leftIndex = 0;
        if (parameterContent.contains("(")) {
            leftIndex = parameterContent.indexOf("(");
        }
        if (parameterContent.contains("[")) {
            int tempIndex = parameterContent.indexOf("[");
            if (leftIndex > 0) {
                if (tempIndex < leftIndex) {
                    leftIndex = parameterContent.indexOf("]") + 1;
                }
            } else {
                leftIndex = parameterContent.indexOf("]") + 1;
            }
        }
        String type = parameterContent.substring(newIndex + 4, leftIndex).trim();
        return type;
    }

    /**
      *
      * @param
      * @return
      * @throws
      * @date 2018/4/25 下午7:05
      * @author sunweisong
      */
    private static String extractTestCaseNameFromTestCaseString(String testCaseString) {

        /**
         * "@Rule public ExpectedException thrown= ExpectedException.none();
         * "@Test(expected = NullPointerException.class)"
         * 2018.05.11
         */
        String annotation = "@Test";
        if (testCaseString.contains("@org.junit.Test")) {
            annotation = "@org.junit.Test";
        }
        int testAnnotationIndex = testCaseString.indexOf(annotation);
        if (testAnnotationIndex == -1) {
            return "";
        }
        testCaseString = testCaseString.substring(testAnnotationIndex);
        int publicVoidIndex = testCaseString.indexOf("public void");
        if (publicVoidIndex == -1) {
            /**
             * testCaseString = "@Test public static void value(Value value) {...}"
             */
            publicVoidIndex = testCaseString.indexOf("public static void");
        }
        if (publicVoidIndex == -1) {
            /**
             * public final void testEqualsObject() {}
             * 2018.06.12
             */
            publicVoidIndex = testCaseString.indexOf("public final void");
        }

        if (publicVoidIndex == -1) {
            /**
             * public final void testEqualsObject() {}
             * 2019.06.14
             */
            publicVoidIndex = testCaseString.indexOf("public<begin> void test()");
        }

        testCaseString = testCaseString.substring(publicVoidIndex);
        int voidStringIndex = testCaseString.indexOf("void");
        int firstLeftParentheses = testCaseString.indexOf("(");
        String testCaseName = testCaseString.substring(voidStringIndex + 5, firstLeftParentheses);
        return testCaseName.trim();

    }

    /**
      *
      * @param
      * @return
      * @throws
      * @date 2018/4/25 下午7:14
      * @author sunweisong
      */
    private static List<InvokeMethodModel> analyzeInvokeMethodAmongTestFile(List<InvokeMethodModel> invokeMethodModelList
            , String testCaseString, List<String> testCaseStringList
            , String testFileContentString) {
        List<InvokeMethodModel> mutList = new ArrayList<>();
        boolean isStatic = false;
        for (InvokeMethodModel invokeMethodModel : invokeMethodModelList) {
            String object = invokeMethodModel.getObject();

            String testCaseName = invokeMethodModel.getTestCaseName();
            if (!object.contains("(") && !object.contains(")")) {

                // search cutName among testCaseString
                String cutName = searchCutNameAmongTestCaseString(object
                        , testCaseName, testCaseStringList);

                // search cutName among Before or BeforeClass
                if (cutName == null) {
                    cutName = searchCutNameAmongBeforeTestCase(object
                            , testCaseStringList);
                }
                if (cutName == null) {
                    // search cutName among testFileContentString
                    cutName = searchCutNameAmongTestFileContentString(object
                            , testCaseStringList, testFileContentString);
                }
                if (cutName != null) {
                    invokeMethodModel.setCutName(cutName);
                } else {
                    /**
                     * 2018.05.30
                     * Substitution S = new Substitution();
                     * Substitution S2 = S.extend(Var,val);
                     * assertEquals(S2,S2.extend(Var, val));
                     */
                    String regex = "^[A-Z].*";
                    if (Pattern.matches(regex, object)) {
                        isStatic = true;
                        // may be static invoke, such as format: ClassName.Method()
                        invokeMethodModel.setCutName(object);
                    }
                }
            } else {
                if (object.startsWith("new ")) {
                    int newStringIndex = object.indexOf("new ");
                    int leftIndex = object.indexOf("(");
                    String cutName = object.substring(newStringIndex + 4, leftIndex).trim();
                    invokeMethodModel.setCutName(cutName);
                }
            }

            // search the type of parameter
            List<ParameterModel> parameterList = invokeMethodModel.getParameterList();
            if (parameterList != null) {
                setParameterType(parameterList, testCaseName
                        , testCaseStringList, testFileContentString);
            }
            if (invokeMethodModel.getCutName() == null) {
                continue;
            }
            List<Integer> midList = calculateMIDWithInvokeMethodModel(invokeMethodModel);
            for (MUTModel mutModel : mutModelList) {
                int mutMid = mutModel.getMethodId();
                for (int mid : midList) {
                    if (mid == mutMid) {
                        invokeMethodModel.setmId(mutMid);
                        // search related test sentence (test fragment)
                        extractRelatedSentenceFromTestFile(invokeMethodModel
                                ,testCaseString , testCaseName, testCaseStringList
                                , testFileContentString, isStatic);
                        mutList.add(invokeMethodModel);
                    }
                }
            }
            isStatic = false;
        }
        return mutList;
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2018/4/25 下午7:47
     * @author sunweisong
     */
    private static String searchCutNameAmongBeforeTestCase(String object
            , List<String> testCaseStringList) {
        String cutName = null;
        boolean flag = false;
        for (String testCaseString : testCaseStringList) {
            if (!testCaseString.contains("@Before")
                    && !testCaseString.contains("@org.junit.Before")) {
                continue;
            }
            List<String> sentenceAmongTestCaseStringList = getSentenceEndedWithSemicolonFromString(testCaseString);
            if (sentenceAmongTestCaseStringList == null) {
                break;
            }
            for (String sentence : sentenceAmongTestCaseStringList) {
                if (sentence.contains(object + ".")) {
                    continue;
                }

                /**
                 * 2018.06.01
                 */
                if (!isOnlyContainsInstanceAmongInitialStatement(object, sentence)) {
                    continue;
                }

                cutName = extractClassNameFromSentence(object, sentence);
                if (cutName != null && !"".equals(cutName)) {
                    flag = true;
                    break;
                }
            }
            if (flag) {
                return cutName;
            }
        }
        return cutName;
    }


    /**
      * 
      * @param 
      * @return
      * @throws
      * @date 2018/5/16 下午1:32
      * @author sunweisong
      */
    private static void setParameterType(List<ParameterModel> parameterList
            , String testCaseName, List<String> testCaseStringList
            , String testFileContentString) {
        for (ParameterModel parameterModel : parameterList) {
            String parameterType = parameterModel.getType();
            if (parameterType == null) {
                String parameterName = parameterModel.getName();
                if ("null".equals(parameterName)) {
                    continue;
                }

                /**
                 * parameter = new Variable("var")
                 * 2018.05.10
                 */
                /**
                 * p.canDerive(f, new Fact[]{f, f});
                 * 2018.05.15
                 */

                /**
                 * sentence = "res.add(test1.extend(new Variable(""), new Value("")));"
                 * parameterContent = "test1.extend(new Variable(""), new Value(""))";
                 * 2018.05.30
                 */
                boolean flag = false;
                Matcher matcher = Pattern.compile("\\s*\\w+\\.\\w+\\(.*?\\)").matcher(parameterName);
                if (matcher.find()) {
                    int start = matcher.start(0);
                    if (start == 0) {
                        flag = true;
                    }
                }
                if (!flag) {
                    if (parameterName.contains("new ")) {
                        int leftParenthesesIndex = 0;
                        if (parameterName.contains("(")) {
                            leftParenthesesIndex = parameterName.indexOf("(");
                        }
                        if (parameterName.contains("[")) {
                            String regex = "\\s*new\\s\\w+\\[.*?\\]";
                            Matcher matcher1 = Pattern.compile(regex).matcher(parameterName);
                            if (matcher1.find()) {
                                int index1 = parameterName.indexOf("[");
                                if (leftParenthesesIndex > 0) {
                                    if (index1 < leftParenthesesIndex) {
                                        leftParenthesesIndex = parameterName.indexOf("]");;
                                    }
                                } else {
                                    leftParenthesesIndex = parameterName.indexOf("]");
                                }
                            }
                        }
                        int newStringIndex = parameterName.indexOf("new ");
                        parameterType = parameterName.substring(newStringIndex + 4
                                , leftParenthesesIndex).trim();
                    }
                    if (parameterType != null) {
                        parameterModel.setType(parameterType);
                        continue;
                    }
                    parameterType = searchCutNameAmongTestCaseString(parameterName
                            , testCaseName, testCaseStringList);
                    // search cutName among Before or BeforeClass
                    if (parameterType == null) {
                        parameterType = searchCutNameAmongBeforeTestCase(parameterName
                                , testCaseStringList);
                    }
                    // search cutName among test file
                    if (parameterType == null) {
                        parameterType = searchCutNameAmongTestFileContentString(parameterName
                                , testCaseStringList, testFileContentString);
                    }

                    if (parameterType == null) {
                        /**
                         * 2018.06.13
                         */
                        boolean isDigit = Pattern.compile("[0-9]*").matcher(parameterName).matches();
                        if (isDigit) {
                            parameterType = "int";
                        }
                    }

                    if (parameterType == null) {
                        /**
                         * parameterName = "(Fact) face4"
                         */
                        boolean isTypeConvert = Pattern.compile("\\(.*?\\)\\s*\\w+").matcher(parameterName).matches();
                        if (isTypeConvert) {
                            int left = parameterName.indexOf("(");
                            int right = parameterName.indexOf(")");
                            parameterType = parameterName.substring(left + 1, right).trim();
                        }
                    }

                    if (parameterType != null) {
                        parameterModel.setType(parameterType);
                    }


                }
            }
        }
    }

    /**
      * 
      * @param 
      * @return
      * @throws
      * @date 2018/5/29 下午5:41
      * @author sunweisong
      */
    private static String removeUserCustomFunction(String temp, int flag) {
        int r_index = 0;
        if (flag == 1) {
            // there is a space between ')' and '{'
            r_index = temp.indexOf(") {");
        } else if (flag == 2) {
            r_index = temp.indexOf("){");
        } else if (flag == 3) {
            r_index = temp.indexOf(") throws Exception {");
        } else if (flag == 4){
            r_index = temp.indexOf(")throws Exception {");
        } else if (flag == 5) {
            r_index = temp.indexOf(") throws Exception{");
        } else {
            r_index = temp.indexOf(")throws Exception{");
        }

        String functionBody = temp.substring(r_index +1);
        StringBuffer buffer = new StringBuffer();
        int count = 0;
        for (char c : functionBody.toCharArray()) {
            buffer.append(c);
            if (c == '{') {
                count++;
            }
            if (c == '}') {
                count--;
            }
            String bufferContent = buffer.toString().trim();
            if (count == 0 && bufferContent.contains("{") && bufferContent.contains("}")) {
                temp = temp.replace(bufferContent, "");
                break;
            }
        }
        return temp;
    }

    /**
      * 
      * @param 
      * @return
      * @throws
      * @date 2018/5/29 下午5:40
      * @author sunweisong
      */
    private static String removeFunctionsCreatedByUserFromString(String temp) {
        while (temp.contains(") {")) {
            temp = removeUserCustomFunction(temp, 1);
        }

        while (temp.contains("){")) {
            temp = removeUserCustomFunction(temp, 2);
        }

        /**
         * Add.
         * The string of " setUp() throws Exception { ****;" is fund.
         * 2017.12.20
         */
        while (temp.contains(") throws Exception {")) {
            temp = removeUserCustomFunction(temp, 3);
        }

        while (temp.contains(")throws Exception {")) {
            temp = removeUserCustomFunction(temp, 4);
        }

        while (temp.contains(") throws Exception{")) {
            temp = removeUserCustomFunction(temp, 5);
        }

        while (temp.contains(")throws Exception{")) {
            temp = removeUserCustomFunction(temp, 6);
        }
        return temp;
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2018/5/3 下午1:42
     * @author sunweisong
     */
    private static void extractRelatedSentenceFromTestFile(InvokeMethodModel invokeMethodModel
            , String testCaseString, String testCaseName, List<String> testCaseStringList
            , String testFileString, boolean isStatic) {
        String invokeMethodSentence = invokeMethodModel.getInvokeMethod();
        for (String tempTestCaseString : testCaseStringList) {
            testFileString = testFileString.replace(tempTestCaseString, "");
        }

        /**
         * public FactTest() { }  public void testGetValues() { assertEquals(testValues, testFact.getValues()); } }
         *  存在自定义函数
         */
        if (testFileString.contains("){")
                || testFileString.contains(") {")
                || testFileString.contains(") throws Exception {")
                || testFileString.contains(")throws Exception {")
                || testFileString.contains(") throws Exception{")
                || testFileString.contains(")throws Exception{")) {
            testFileString = removeFunctionsCreatedByUserFromString(testFileString);
        }

        List<StatementModel> sentenceAmongTestFileStringList = extractStatementModelFromString(testFileString);

        if (!isStatic) {
            // search instantiate statement
            String instance = invokeMethodModel.getObject();
            List<StatementModel> instantiateStatementModelList = searchInstantiateStatement(instance
                    , invokeMethodSentence
                    , testCaseString
                    , testCaseStringList
                    , sentenceAmongTestFileStringList
                    , false
                    , null
                    , null);
            if (instantiateStatementModelList != null) {
                invokeMethodModel.setRelatedInstantiateStatementList(instantiateStatementModelList);
            }
        }
        // search parameterized statement
        List<ParameterModel> parameterList = invokeMethodModel.getParameterList();
        if (parameterList != null) {
            List<StatementModel> parameterizedStatementModelList = searchParameterizedStatement(parameterList
                    , invokeMethodSentence
                    , testCaseString
                    , testCaseStringList
                    , sentenceAmongTestFileStringList
                    , false
                    , null
                    , null);
            if (parameterizedStatementModelList != null) {
                invokeMethodModel.setRelatedParameterizedStatementList(parameterizedStatementModelList);
            }
        }
        // search invoked statement
        List<StatementModel> invokedStatementModelList = searchInvokedStatement(invokeMethodSentence
                , testCaseString
                , false
                , null
                , null);

        if (invokedStatementModelList != null) {
            invokeMethodModel.setRelatedInvokedStatementList(invokedStatementModelList);
        }
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2018/5/7 上午11:53
     * @author sunweisong
     */
    private static List<StatementModel> searchInstantiateStatement(String instance
            , String invokeMethodSentence
            , String testCaseString
            , List<String> testCaseStringList
            , List<StatementModel> sentenceAmongTestFileStringList
            , boolean isFromTryBlock
            , String tryCatchBlock
            , List<String> tryCatchBlockList) {

        List<StatementModel> instantiateStatementModelList = null;
        List<String> variableList = null;

        /**
         * 2018.06.06
         */
        List<StatementModel> statementModelFromTryBlockList = null;
        if (isFromTryBlock) {
            for (String tempTryCatchBlock : tryCatchBlockList ) {
                testCaseString = testCaseString.replace(tempTryCatchBlock, "");
            }
            // search instantiate statement from try block
            List<StatementModel> statementModelListAmongTryCatchBlockString = extractStatementModelFromTryCatchBlock(tryCatchBlock);
            if (statementModelListAmongTryCatchBlockString != null) {
                statementModelFromTryBlockList = searchStatementModelFromList(instance
                        , invokeMethodSentence
                        , statementModelListAmongTryCatchBlockString);
                if (statementModelFromTryBlockList != null) {
                    if (instantiateStatementModelList == null) {
                        instantiateStatementModelList = new ArrayList<>();
                    }
                    instantiateStatementModelList.addAll(statementModelFromTryBlockList);
                    variableList = searchStatementAboutVariable(statementModelFromTryBlockList
                            , statementModelListAmongTryCatchBlockString
                            , invokeMethodSentence, instantiateStatementModelList);
                }
                // recovery all tag = 0;
                for (StatementModel statementModel : statementModelListAmongTryCatchBlockString) {
                    statementModel.setTag(0);
                }
            }

        }

        // search instantiate statement from test case string.
        List<StatementModel> statementModelList2 = null;
        List<StatementModel> statementModelListAmongTestCaseString = extractStatementModelFromString(testCaseString);
        if (statementModelListAmongTestCaseString != null) {
            statementModelList2 = searchStatementModelFromList(instance
                    , invokeMethodSentence
                    , statementModelListAmongTestCaseString);
            if (statementModelList2 != null) {
                if (instantiateStatementModelList == null) {
                    instantiateStatementModelList = new ArrayList<>();
                }
                instantiateStatementModelList.addAll(statementModelList2);
                variableList = searchStatementAboutVariable(statementModelList2
                        , statementModelListAmongTestCaseString
                        , invokeMethodSentence, instantiateStatementModelList);
            }
            // recovery all tag = 0;
            for (StatementModel statementModel : statementModelListAmongTestCaseString) {
                statementModel.setTag(0);
            }
        }



        // search instantiate statement from Before and BeforeClass.
        List<String> variableListAmongBefore = null;
        for (String tempTestCaseString : testCaseStringList) {
            if (tempTestCaseString.contains("@After")
                    || tempTestCaseString.contains("@org.junit.After")) {
                continue;
            }
            List<StatementModel> statementModelList = null;
            if (tempTestCaseString.contains("@Before")
                    || tempTestCaseString.contains("@org.junit.Before")) {
                List<StatementModel> statementModelListAmongBeforeFunction = extractStatementModelFromString(tempTestCaseString);
                if (statementModelListAmongBeforeFunction != null) {
                    for (StatementModel statementModel : statementModelListAmongBeforeFunction) {
                        String statement = statementModel.getContent();
                        if (statement.contains(instance)
                                && !statement.contains(invokeMethodSentence)
                                && !statement.contains(instance + ".")
                                ) {
                            if (!isOnlyContainsObjectInStatement(instance, statement)) {
                                continue;
                            }
                            statementModel.setTag(1);
                            if (statementModelList == null) {
                                statementModelList = new ArrayList<>();
                            }
                            statementModelList.add(statementModel);
                        }
                    }
                    if (statementModelList != null) {
                        if (instantiateStatementModelList == null) {
                            instantiateStatementModelList = new ArrayList<>();
                        }
                        instantiateStatementModelList.addAll(statementModelList);
                        variableListAmongBefore = searchStatementAboutVariable(statementModelList
                                , statementModelListAmongBeforeFunction
                                , invokeMethodSentence, instantiateStatementModelList);
                    }

                    // search variableList collected from test case among before and beforeclass
                    if (variableList != null) {
                        searchStatementVariableFromTestCaseAmongTargetList(variableList
                                , statementModelListAmongBeforeFunction
                                , instantiateStatementModelList);
                        if (variableListAmongBefore != null) {
                            variableList.addAll(variableListAmongBefore);
                        }
                    } else {
                        if (variableListAmongBefore != null) {
                            variableList = variableListAmongBefore;
                        }
                    }

                    // recovery all tag = 0;
                    for (StatementModel statementModel : statementModelListAmongBeforeFunction) {
                        statementModel.setTag(0);
                    }
                }
            }
        }

        // search instantiate statement from test file string.
        if (sentenceAmongTestFileStringList != null) {
            for (StatementModel statementModel : sentenceAmongTestFileStringList) {
                String statement = statementModel.getContent();
                if (statement.contains(instance)) {
                    if (!isOnlyContainsObjectInStatement(instance, statement)) {
                        continue;
                    }
                    statementModel.setTag(1);
                    if (instantiateStatementModelList == null) {
                        instantiateStatementModelList = new ArrayList<>();
                    }
                    instantiateStatementModelList.add(statementModel);
                }
            }
            if (instantiateStatementModelList != null) {
                List<String> variableList1 = extractVariableListStatementModelList(instantiateStatementModelList);
                if (variableList1 != null) {
                    List<StatementModel> statementModelList1 = searchStatementListAboutVariableInVariableList(variableList1
                            , sentenceAmongTestFileStringList, invokeMethodSentence);
                    if (statementModelList1 != null) {
                        instantiateStatementModelList.addAll(statementModelList1);
                    }
                }
            }

            // search variableList collected from test case and before among test file
            if (variableList != null) {
                searchStatementVariableFromTestCaseAmongTargetList(variableList
                        , sentenceAmongTestFileStringList
                        , instantiateStatementModelList);
            }

            // recovery all tag = 0;
            for (StatementModel statementModel : sentenceAmongTestFileStringList) {
                statementModel.setTag(0);
            }
        }
        return instantiateStatementModelList;
    }

    /**
      * 
      * @param 
      * @return
      * @throws
      * @date 2018/6/6 下午3:41
      * @author sunweisong
      */
    private static List<StatementModel> searchStatementModelFromList (String instance
            , String invokeMethodSentence
            , List<StatementModel> list) {
        List<StatementModel> statementModelList = null;
        for (StatementModel statementModel : list) {
            String statement = statementModel.getContent();
            if (statement.contains(instance)
                    && !statement.contains(invokeMethodSentence)
                    && !statement.contains(instance + ".")) {
                /**
                 * 2018.05.26
                 */
                int leftParenthesesIndex = statement.indexOf("(");
                if (leftParenthesesIndex != -1) {
                    int assertIndex = statement.indexOf("assert");
                    if (assertIndex != -1
                            && assertIndex < leftParenthesesIndex) {
                        continue;
                    }
                }
                if (!isOnlyContainsObjectInStatement(instance, statement)) {
                    continue;
                }

                /**
                 * statement = "pre2.equals(pre1);"
                 * or = "value1.equals(pre1);"
                 * instance = "pre"
                 * 2018.06.01
                 */
                if (!isOnlyContainsInstanceAmongInitialStatement(instance, statement)) {
                    continue;
                }

                statementModel.setTag(1);
                if (statementModelList == null) {
                    statementModelList = new ArrayList<>();
                }
                statementModelList.add(statementModel);
            }
        }
        return statementModelList;
    }

    /**
      * 
      * @param 
      * @return
      * @throws
      * @date 2018/6/5 下午3:26
      * @author sunweisong
      */
    private static int getInstanceIndexAmongStatement(String instance, String statement) {
        int instanceIndex = statement.indexOf(instance);
        int instanceLength = instance.length();
        int alphaBeforeInstanceIndex = instanceIndex - 1;
        int alphaAfterInstanceIndex = instanceIndex + instanceLength;
        boolean beforeFlag = false;
        boolean afterFlag = false;
        int index = -1;
        while (instanceIndex != -1) {
            if (alphaBeforeInstanceIndex < 0) {
                beforeFlag = true;
            } else {
                char alpha = statement.charAt(alphaBeforeInstanceIndex);
                if (!CharUtil.isVariableAlpha(alpha) && alpha != '\"') {
                    beforeFlag = true;
                }
            }
            if (alphaAfterInstanceIndex < statement.length()) {
                char alpha = statement.charAt(alphaAfterInstanceIndex);
                if (!CharUtil.isVariableAlpha(alpha) && alpha != '\"') {
                    afterFlag = true;
                }
            }
            if (beforeFlag == true && afterFlag == true) {
                index = instanceIndex;
                break;
            }
            statement = statement.substring(instanceIndex + 1);
            instanceIndex = statement.indexOf(instance);
            alphaBeforeInstanceIndex = instanceIndex - 1;
            alphaAfterInstanceIndex = instanceIndex + instanceLength;
            beforeFlag = false;
            afterFlag = false;
        }
        return index;
    }

    /**
     *
     * @param instance
     * @param statement
     * @return
     */

    private static boolean isOnlyContainsInstanceAmongInitialStatement(String instance
            , String statement) {
        boolean isOnlyContainsObjectInStatement = false;
        int instanceIndex = statement.indexOf(instance);
        int instanceLength = instance.length();
        int alphaBeforeInstanceIndex = instanceIndex - 1;
        int alphaAfterInstanceIndex = instanceIndex + instanceLength;
        boolean beforeFlag = false;
        boolean afterFlag = false;
        while (instanceIndex != -1) {
            if (alphaBeforeInstanceIndex < 0) {
                beforeFlag = true;
            } else {
                char alpha = statement.charAt(alphaBeforeInstanceIndex);
                if (!CharUtil.isVariableAlpha(alpha) && alpha != '\"') {
                    beforeFlag = true;
                }
            }
            if (alphaAfterInstanceIndex < statement.length()) {
                char alpha = statement.charAt(alphaAfterInstanceIndex);
                if (!CharUtil.isVariableAlpha(alpha) && alpha != '\"') {
                    afterFlag = true;
                }
            }
            if (beforeFlag == true && afterFlag == true) {
                isOnlyContainsObjectInStatement = true;
                break;
            }
            statement = statement.substring(instanceIndex + 1);
            instanceIndex = statement.indexOf(instance);
            alphaBeforeInstanceIndex = instanceIndex - 1;
            alphaAfterInstanceIndex = instanceIndex + instanceLength;
            beforeFlag = false;
            afterFlag = false;
        }

        if (statement.contains("=")) {
            int equalIndex = statement.indexOf("=");
            if (equalIndex < instanceIndex) {
                return false;
            }
        }

        if (statement.contains("(")) {
            int leftIndex = statement.indexOf("(");
            if (leftIndex < instanceIndex) {
                return false;
            }
        }

        return isOnlyContainsObjectInStatement;
    }

    /**
      * 
      * @param 
      * @return
      * @throws
      * @date 2018/5/30 下午3:29
      * @author sunweisong
      */
    private static void searchStatementVariableFromTestCaseAmongTargetList(List<String> variableList
            , List<StatementModel> targetList
            , List<StatementModel> instantiateStatementModelList) {
        for (String variable :variableList) {
            for (StatementModel statementModel : targetList) {
                if (statementModel.getTag() == 1) {
                    continue;
                }
                String statement = statementModel.getContent();
                if (!isOnlyContainsObjectInStatement(variable, statement)) {
                    continue;
                }
                statementModel.setTag(1);
                instantiateStatementModelList.add(statementModel);
            }
        }
    }

    /**
      * 
      * @param 
      * @return
      * @throws
      * @date 2018/5/30 下午2:18
      * @author sunweisong
      */
    private static List<String> searchStatementAboutVariable(List<StatementModel> statementModelList
            , List<StatementModel> statementModelListAmongTestCase
            , String invokeMethodSentence
            , List<StatementModel> targetStatementModelList) {
        List<String> variableList = null;
        List<String> tempVariableList = null;
        while (true) {
            tempVariableList = extractVariableListStatementModelList(statementModelList);
            if (tempVariableList == null) {
                break;
            }
            if (variableList == null) {
                variableList = new ArrayList<>();
            }
            variableList.addAll(tempVariableList);
            statementModelList =  searchStatementListAboutVariableInVariableList(tempVariableList
                    , statementModelListAmongTestCase
                    , invokeMethodSentence);
            if (statementModelList == null) {
                break;
            }
            targetStatementModelList.addAll(statementModelList);
        }
        return variableList;
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2018/5/8 下午5:32
     * @author sunweisong
     */
    private static boolean isOnlyContainsObjectInStatement(String object
            , String statement) {
        boolean isOnlyContainsObjectInStatement = false;
        int instanceIndex = statement.indexOf(object);
        int instanceLength = object.length();
        int alphaBeforeInstanceIndex = instanceIndex - 1;
        int alphaAfterInstanceIndex = instanceIndex + instanceLength;
        boolean beforeFlag = false;
        boolean afterFlag = false;
        /**
         * 2018.05.28
         * 增加while
         */
        while (instanceIndex != -1) {
            if (alphaBeforeInstanceIndex < 0) {
                beforeFlag = true;
            } else {
                char alpha = statement.charAt(alphaBeforeInstanceIndex);
                if (!CharUtil.isVariableAlpha(alpha) && alpha != '\"') {
                    beforeFlag = true;
                }
            }
            if (alphaAfterInstanceIndex < statement.length()) {
                char alpha = statement.charAt(alphaAfterInstanceIndex);
                if (!CharUtil.isVariableAlpha(alpha) && alpha != '\"') {
                    afterFlag = true;
                }
            }
            if (beforeFlag == true && afterFlag == true) {
                isOnlyContainsObjectInStatement = true;
            }
            if (isOnlyContainsObjectInStatement) {
                break;
            }
            statement = statement.substring(instanceIndex + 1);
            instanceIndex = statement.indexOf(object);
            alphaBeforeInstanceIndex = instanceIndex - 1;
            alphaAfterInstanceIndex = instanceIndex + instanceLength;
            beforeFlag = false;
            afterFlag = false;
        }

        /**
         *   instance = "pre";
         *   statement = "String top = pre.toString();"
         * 	 or statement = "Fact fact = new Fact(pre, val);"
         *
         * 	 instance = "var"
         * 	 statement = "sub.extend(var, val);" 未解决
         * 	 2018.05.31
         */
        if (statement.contains("=")) {
            int equalIndex = statement.indexOf("=");
            if (equalIndex < instanceIndex) {
                return false;
            }
        }

        return isOnlyContainsObjectInStatement;
    }

    /**
      * 
      * @param 
      * @return
      * @throws
      * @date 2018/5/7 上午11:53
      * @author sunweisong
      */
    private static List<StatementModel> searchParameterizedStatement(List<ParameterModel> parameterList
            , String invokeMethodSentence
            , String testCaseString, List<String> testCaseStringList
            , List<StatementModel> sentenceAmongTestFileStringList
            , boolean isFromTryBlock, String tryCatchBlock
            , List<String> tryCatchBlockList) {

        List<StatementModel> parameterizedStatementModelList = null;
        List<String> variableList = null;

        /**
         * 2018.06.06
         */
        List<StatementModel> statementModelFromTryBlockList = null;
        if (isFromTryBlock) {
            /**
             * 2018.06.06
             */
            for (String tempTryCatchBlock : tryCatchBlockList) {
                testCaseString = testCaseString.replace(tempTryCatchBlock, "");
            }

            List<StatementModel> statementModelListAmongTryCatchBlockString = extractStatementModelFromTryCatchBlock(tryCatchBlock);
            if (statementModelListAmongTryCatchBlockString != null) {
                statementModelFromTryBlockList = searchParameterizedStatement(parameterList
                        , statementModelListAmongTryCatchBlockString, invokeMethodSentence
                        ,  variableList);
                if (statementModelFromTryBlockList != null) {
                    if (parameterizedStatementModelList == null) {
                        parameterizedStatementModelList = new ArrayList<>();
                    }
                    parameterizedStatementModelList.addAll(statementModelFromTryBlockList);
                }
                for (StatementModel statementModel : statementModelListAmongTryCatchBlockString) {
                    statementModel.setTag(0);
                }
            }

        }

        // search parameterized statement from test case string.
        List<StatementModel> statementModelListAmongTestCaseString = extractStatementModelFromString(testCaseString);
        if (statementModelListAmongTestCaseString != null) {
            List<StatementModel> statementModelList = searchParameterizedStatement(parameterList
                    , statementModelListAmongTestCaseString, invokeMethodSentence
                    ,  variableList);
            if (statementModelList != null) {
                if (parameterizedStatementModelList == null) {
                    parameterizedStatementModelList = new ArrayList<>();
                }
                parameterizedStatementModelList.addAll(statementModelList);
            }
            // recovery all tag = 0;
            for (StatementModel statementModel : statementModelListAmongTestCaseString) {
                statementModel.setTag(0);
            }
        }

        // search parameterized statement from Before or BeforeClass.
        for (String tempTestCaseString : testCaseStringList) {
            List<StatementModel> statementModelList = null;
            if (tempTestCaseString.contains("@Before")
                    || tempTestCaseString.contains("@org.junit.Before")) {
                List<StatementModel> statementModelListAmongBeforeFunction = extractStatementModelFromString(tempTestCaseString);
                if (statementModelListAmongBeforeFunction == null) {
                    continue;
                }

                // search variableList collected from test case among before and beforeclass
                if (variableList != null) {
                    searchStatementVariableFromTestCaseAmongTargetList(variableList
                            , statementModelListAmongBeforeFunction
                            , parameterizedStatementModelList);
                }

                statementModelList = searchParameterizedStatement(parameterList
                        , statementModelListAmongBeforeFunction, invokeMethodSentence
                        , variableList);
                if (statementModelList != null) {
                    if (parameterizedStatementModelList == null) {
                        parameterizedStatementModelList = new ArrayList<>();
                    }
                    parameterizedStatementModelList.addAll(statementModelList);
                }
                // recovery all tag = 0;
                for (StatementModel statementModel : statementModelListAmongBeforeFunction) {
                    statementModel.setTag(0);
                }

            }
        }

        // search parameterized statement from test file string.
        if (sentenceAmongTestFileStringList != null) {
            List<String> tempVariableList = new ArrayList<>();
            List<String> parameterNameList = getParameterNameList(parameterList);
            tempVariableList.addAll(parameterNameList);
            if (parameterizedStatementModelList != null) {
                List<String> variableListFromStatement = extractVariableListStatementModelList(parameterizedStatementModelList);
                if (variableListFromStatement != null) {
                    tempVariableList.addAll(variableListFromStatement);
                }
            }
            List<StatementModel> statementModelList = searchStatementListAboutVariableInVariableList(tempVariableList
                    , sentenceAmongTestFileStringList, invokeMethodSentence);
            if (statementModelList != null) {
                if (parameterizedStatementModelList == null) {
                    parameterizedStatementModelList = new ArrayList<>();
                }
                parameterizedStatementModelList.addAll(statementModelList);
            }

            // search variableList collected from test case and before among test file
            if (variableList != null) {
                searchStatementVariableFromTestCaseAmongTargetList(variableList
                        , sentenceAmongTestFileStringList
                        , parameterizedStatementModelList);
            }

            // recovery all tag = 0;
            for (StatementModel statementModel : sentenceAmongTestFileStringList) {
                statementModel.setTag(0);
            }
        }
        return parameterizedStatementModelList;
    }


    /**
      * 
      * @param 
      * @return
      * @throws
      * @date 2018/5/7 上午11:02
      * @author sunweisong
      */
    private static List<String> getParameterNameList(List<ParameterModel> parameterList) {
        List<String> parameterNameList = new ArrayList<>(parameterList.size());
        for (ParameterModel parameterModel : parameterList ) {
            parameterNameList.add(parameterModel.getName());
        }
        return parameterNameList;
    }

    /**
      * 
      * @param 
      * @return
      * @throws
      * @date 2018/5/7 上午11:53
      * @author sunweisong
      */
    private static List<StatementModel> searchInvokedStatement(String invokeMethodSentence
            , String testCaseString
            , boolean isFromTryBlock
            , String tryCatchBlock
            , List<String> tryCatchBlockList) {
        List<StatementModel> invokedStatementModelList = null;
        List<String> identifierList = null;

        /**
         * 2018.06.06
         */
        if (isFromTryBlock) {
            for (String tempTryCatchBlock : tryCatchBlockList) {
                testCaseString = testCaseString.replace(tempTryCatchBlock, "");
            }
            // search instantiate statement from try block
            List<StatementModel> statementModelListAmongTryCatchBlockString = extractStatementModelFromTryCatchBlock(tryCatchBlock);
            if (statementModelListAmongTryCatchBlockString != null) {
                for (StatementModel statementModel : statementModelListAmongTryCatchBlockString) {
                    String statement = statementModel.getContent();
                    if (statement.contains(invokeMethodSentence)) {
                        statementModel.setTag(1);
                        if (invokedStatementModelList == null) {
                            invokedStatementModelList = new ArrayList<>();
                        }
                        invokedStatementModelList.add(statementModel);
                    }
                }
                if (invokedStatementModelList != null) {
                    identifierList = extractIdentifierFromList(invokedStatementModelList
                            , invokeMethodSentence);
                }
                if (identifierList != null) {
                    for (String identifier : identifierList) {
                        for (StatementModel statementModel : statementModelListAmongTryCatchBlockString) {
                            if (statementModel.getTag() == 1) {
                                continue;
                            }
                            String statement = statementModel.getContent();
                            if (!isOnlyContainsObjectInStatement(identifier, statement)) {
                                continue;
                            }
                            statementModel.setTag(1);
                            invokedStatementModelList.add(statementModel);
                        }
                    }
                }
                // recovery all tag = 0;
                for (StatementModel statementModel : statementModelListAmongTryCatchBlockString) {
                    statementModel.setTag(0);
                }
            }

        }

        List<StatementModel> statementModelListAmongTestCaseString = extractStatementModelFromString(testCaseString);
        if (statementModelListAmongTestCaseString != null) {
            for (StatementModel statementModel : statementModelListAmongTestCaseString) {
                String statement = statementModel.getContent();
                if (statement.contains(invokeMethodSentence)) {
                    statementModel.setTag(1);
                    if (invokedStatementModelList == null) {
                        invokedStatementModelList = new ArrayList<>();
                    }
                    invokedStatementModelList.add(statementModel);
                }
            }

            /**
             * 2018.05.31
             *
             */
            if (invokedStatementModelList != null) {
                List<String> tempIdentifierList = extractIdentifierFromList(invokedStatementModelList
                        , invokeMethodSentence);
                if (tempIdentifierList != null) {
                    if (identifierList == null) {
                        identifierList = new ArrayList<>();
                    }
                    identifierList.addAll(tempIdentifierList);
                }
            }
            if (identifierList != null) {
                for (String identifier : identifierList) {
                    for (StatementModel statementModel : statementModelListAmongTestCaseString) {
                        if (statementModel.getTag() == 1) {
                            continue;
                        }
                        String statement = statementModel.getContent();
                        if (!isOnlyContainsObjectInStatement(identifier, statement)) {
                            continue;
                        }
                        statementModel.setTag(1);
                        invokedStatementModelList.add(statementModel);
                    }
                }
            }

            // recovery all tag = 0;
            for (StatementModel statementModel : statementModelListAmongTestCaseString) {
                statementModel.setTag(0);
            }
        }

        return invokedStatementModelList;
    }

    /**
      *
      * @param
      * @return
      * @throws
      * @date 2018/5/31 下午3:12
      * @author sunweisong
      */
    private static List<String> extractIdentifierFromList(List<StatementModel> invokedStatementModelList
            , String invokeMethodSentence) {
        List<String> identifierList = null;
        for (StatementModel statementModel : invokedStatementModelList) {
            String statement = statementModel.getContent();
            if (!statement.contains("=")) {
                continue;
            }
            int equalIndex = statement.indexOf("=");
            int index = statement.indexOf(invokeMethodSentence);
            if (equalIndex > index) {
                continue;
            }
            String identifier = statement.substring(0, equalIndex).trim();
            if (identifier.contains(" ")) {
                String[] tempArray = identifier.split(" ");
                identifier = tempArray[1];
            }
            if (identifierList == null) {
                identifierList = new ArrayList<>();
            }
            identifierList.add(identifier);
        }
        return identifierList;
    }

    /**
     *
     * @param
     * @return
     * @throws
     * @date 2018/5/6 下午9:47
     * @author sunweisong
     */
    private static List<StatementModel> searchParameterizedStatement(List<ParameterModel> parameterList
            , List<StatementModel> statementModelListAmongTestCaseString
            , String invokeMethodSentence
            , List variableList) {
        List<StatementModel> parameterizedStatementModelList = null;
        int parameterListSize = parameterList.size();
        if (parameterListSize == 0) {
            return null;
        }
        List<String> parameterNameList = new ArrayList<>();
        for (ParameterModel parameterModel : parameterList) {
            String parameterName = parameterModel.getName();
            if (!"null".equals(parameterName)) {
                parameterNameList.add(parameterName);
            }
        }
        if (parameterList.size() == 0) {
            return null;
        }
        List<StatementModel> statementModelList = searchStatementListAboutVariableInVariableList(parameterNameList
                , statementModelListAmongTestCaseString
                , invokeMethodSentence);
        if (statementModelList != null) {
            parameterizedStatementModelList = new ArrayList<>();
            parameterizedStatementModelList.addAll(statementModelList);
            List<String> tempVariableList = searchStatementAboutVariable(statementModelList
                    , statementModelListAmongTestCaseString
                    , invokeMethodSentence, parameterizedStatementModelList);
            if (tempVariableList != null) {
                if (variableList != null) {
                    variableList.addAll(tempVariableList);
                } else {
                    variableList = tempVariableList;
                }
            }
        }
        return parameterizedStatementModelList;
    }

    /**
      * 
      * @param 
      * @return
      * @throws
      * @date 2018/5/6 下午9:49
      * @author sunweisong
      */
    private static List<String> extractVariableListStatementModelList(List<StatementModel> statementModelList) {
        List<String> variableList = null;
        for (StatementModel statementModel : statementModelList) {
            String statement = statementModel.getContent();
            if (statement.contains("(") && statement.contains(")")) {
                List<String> tempList = extractVariableFromStatement(statement);
                if (tempList != null) {
                    if (variableList == null) {
                        variableList = new ArrayList<>();
                    }
                    variableList.addAll(tempList);
                }
            }
        }
        return variableList;
    }

    /**
      * 
      * @param 
      * @return
      * @throws
      * @date 2018/5/6 下午9:47
      * @author sunweisong
      */
    private static List<String> extractVariableFromStatement(String statement) {
        List<String> allVariableList = null;
        String temp = statement;
        if (statement.contains("\"")) {
            temp = removeContentsInQuotes(statement);
        }
        if (temp.contains("(")) {

            /**
             * statement = "Argument a=Argument.value(new Value(\"v\"));"
             * 2018.05.10
             */
            if (StringUtil.isMoreThanOneLayerParenthesesInString(temp)) {
                int leftParenthesesIndex = temp.indexOf("(");
                int lastRightParenthesesIndex = temp.lastIndexOf(")");
                temp = temp.substring(leftParenthesesIndex + 1, lastRightParenthesesIndex);
                if ("".equals(temp.trim())) {
                    return null;
                }
                if (temp.contains(",")) {
                    String[] parameterArray = temp.split(",");
                    for (String parameter : parameterArray) {
                        if ("".equals(parameter.trim())) {
                            continue;
                        }
                        if (parameter.contains("(")) {
                            List<String> variableList = extractVariableFromSentence(parameter);
                            if (variableList == null) {
                                continue;
                            }
                            if (allVariableList == null) {
                                allVariableList = new ArrayList<>();
                            }
                            allVariableList.addAll(variableList);
                            continue;
                        }
                        if (allVariableList == null) {
                            allVariableList = new ArrayList<>();
                        }
                        allVariableList.add(parameter.trim());
                    }
                } else {
                    if (temp.contains("(")) {
                        List<String> variableList = extractVariableFromSentence(temp);
                        if (variableList != null) {
                            if (allVariableList == null) {
                                allVariableList = new ArrayList<>();
                            }
                            allVariableList.addAll(variableList);
                        }
                    } else {
                        if (allVariableList == null) {
                            allVariableList = new ArrayList<>();
                        }
                        allVariableList.add(temp.trim());
                    }

                }
            } else {
                List<String> variableList = extractVariableFromSentence(temp);
                if (variableList != null) {
                    if (allVariableList == null) {
                        allVariableList = new ArrayList<>();
                    }
                    allVariableList.addAll(variableList);
                }
            }
        }
        return allVariableList;
    }

    /**
      * 
      * @param 
      * @return
      * @throws
      * @date 2018/5/10 下午10:41
      * @author sunweisong
      */
    private static List<String> extractVariableFromSentence(String sentence) {
        List<String> variableList = null;
        String temp = sentence;
        int lastLeftParenthesesIndex = temp.lastIndexOf("(");
        int lastRightParenthesesIndex = temp.lastIndexOf(")");
        if (lastLeftParenthesesIndex + 1 >= lastRightParenthesesIndex) {
            // no parameter
            return variableList;
        }
        temp = temp.substring(lastLeftParenthesesIndex + 1, lastRightParenthesesIndex).trim();
        if ("".equals(temp)) {
            // have parameter but is no variable
            return variableList;
        }
        if (temp.contains(",")) {
            // have more than 1 parameter
            String[] variableArray = temp.split(",");
            for (String variable : variableArray) {
                if (variable.contains("\"")) {
                    continue;
                }
                if (variableList == null) {
                    variableList = new ArrayList<>();
                }
                variableList.add(variable.trim());
            }
        } else {
            // only have 1 parameter
            if (!temp.contains("\"")) {
                // the parameter is variable
                if (variableList == null) {
                    variableList = new ArrayList<>();
                }
                variableList.add(temp);
            }
        }
        return variableList;
    }
    
    /**
      * 
      * @param 
      * @return
      * @throws
      * @date 2018/5/6 下午9:47
      * @author sunweisong
      */
    private static List<StatementModel> searchStatementListAboutVariableInVariableList(List<String> variableList
            , List<StatementModel> statementModelListAmongTestCaseString
            , String invokeMethodSentence) {
        List<StatementModel> statementModelList = null;
        for (String variable : variableList) {
            if (variable.contains("\"")) {
                continue;
            }
            for (StatementModel statementModel : statementModelListAmongTestCaseString) {
                String statement = statementModel.getContent();

                /**
                 * 2018.06.06
                 */
                if (statement.contains(".")) {
                    int pointIndex = statement.indexOf(".");
                    String temp = statement.substring(0, pointIndex).trim();
                    if ("System".equals(temp)) {
                        continue;
                    }
                }

                if (statement.contains(variable)
                        && !statement.contains(invokeMethodSentence)
                        && statementModel.getTag() == 0) {
                    /**
                     * 2018.05.26
                     */
                    int leftParenthesesIndex = statement.indexOf("(");
                    if (leftParenthesesIndex != -1) {
                        int assertIndex = statement.indexOf("assert");
                        if (assertIndex != -1
                                && assertIndex < leftParenthesesIndex) {
                            continue;
                        }
                    }

                    if (!isOnlyContainsObjectInStatement(variable, statement)) {
                        continue;
                    }
                    statementModel.setTag(1);
                    if (statementModelList == null) {
                        statementModelList = new ArrayList<>();
                    }
                    statementModelList.add(statementModel);
                }
            }
        }
        return statementModelList;
    }

    /**
      *
      * @param
      * @return
      * @throws
      * @date 2018/5/3 下午12:40
      * @author sunweisong
      */
    private static List<Integer> calculateMIDWithInvokeMethodModel(InvokeMethodModel invokeMethodModel) {
        StringBuffer stringBuffer = new StringBuffer();
        String cutName = invokeMethodModel.getCutName();
        String mutName = invokeMethodModel.getMethodName();
        stringBuffer.append(cutName);
        stringBuffer.append("," + mutName);
        List<ParameterModel> parameterList = invokeMethodModel.getParameterList();
        if (parameterList != null) {
            int parameterListSize = parameterList.size();
            for (int index = 0; index < parameterListSize; index++) {
                for (ParameterModel parameterModel : parameterList) {
                    if (parameterModel.getOrder() == index + 1) {
                        String parameterName = parameterModel.getName();
                        if ("null".equals(parameterName)) {
                            // parameter == null
                            for (MUTModel mutModel : mutModelList) {
                                if (mutModel.getClassName().equals(cutName)
                                        && mutModel.getMethodName().equals(mutName)) {
                                    String mutArguments = mutModel.getArguments();
                                    if (mutArguments != null) {
                                        if ("".equals(mutArguments.trim())) {
                                            continue;
                                        }
                                        if (mutArguments.contains(",")) {
                                            String[] argumentArray = mutArguments.split(",");
                                            if (argumentArray.length == parameterListSize) {
                                                String parameterType = argumentArray[index];
                                                stringBuffer.append("," + parameterType);
                                            }
                                        } else {
                                            if (parameterListSize == 1) {
                                                stringBuffer.append("," + mutArguments.trim());
                                            }
                                        }
                                    } else {
                                        continue;
                                    }
                                }
                            }
                            break;
                        }

                        String parameterType = parameterModel.getType();
                        if (parameterType == null) {
                            // not find parameter type and parameter != null
                            stringBuffer.append("," + "Object");
                        } else {
                            stringBuffer.append("," + parameterType);
                        }
                    }
                }
            }
        }
        String mutString = stringBuffer.toString();
        List<Integer> midList = calculateAllPossibleMid(mutString);
        return midList;
    }

    /**
      * 
      * @param 
      * @return
      * @throws
      * @date 2018/5/8 下午2:48
      * @author sunweisong
      */
    private static List<Integer> calculateAllPossibleMid(String mutString) {
        List<Integer> midList = new ArrayList<>();
        String[] array = mutString.split(",");
        String cutName = array[0];
        String mutName = array[1];
        StringBuffer stringBuffer = new StringBuffer(cutName + mutName);
        int hashCode = 0;
        if (array.length < 3) {
            String temp = stringBuffer.toString();
            hashCode = temp.hashCode();
            midList.add(hashCode);
        } else if (array.length == 3) {
            String type = array[2];
            stringBuffer.append(type);
            String temp = stringBuffer.toString();
            hashCode = temp.hashCode();
            midList.add(hashCode);
            if (!"Object".equals(type)) {
                stringBuffer.setLength(0);
                stringBuffer.append(cutName + mutName + "Object");
                temp = stringBuffer.toString();
                hashCode = temp.hashCode();
                midList.add(hashCode);
            }
        } else {
            int arrayLength = array.length;
            int allLevel = arrayLength - 2;
            int currentLevel = 0;
            String[] typeArray = new String[allLevel];
            for (int i = 2; i < arrayLength; i++) {
                typeArray[i - 2] = array[i];
            }
            List<String> parameterTypeList = getPossibleMUTString(allLevel
                    , currentLevel, typeArray);
            for (String parameterType :parameterTypeList) {
                stringBuffer.append(parameterType);
                String temp = stringBuffer.toString();
                hashCode = temp.hashCode();
                midList.add(hashCode);
                stringBuffer.setLength(0);
                stringBuffer.append(cutName + mutName);
            }
        }
        stringBuffer = null;
        return midList;
    }

    /**
      * 
      * @param 
      * @return
      * @throws
      * @date 2018/5/8 下午2:36
      * @author sunweisong
      */
    private static List<String> getPossibleMUTString (int allLevel
            , int currentLevel, String[] typeArray) {
        if (currentLevel == allLevel) {
            return null;
        }
        List<String> typeList = new ArrayList<>();
        currentLevel++;
        List<String> tempList = null;
        for (int i = 0; i < 2; i++) {
            StringBuffer stringBuffer = null;
            if (i == 0) {
                stringBuffer = new StringBuffer("Object");
                tempList = getPossibleMUTString(allLevel, currentLevel, typeArray);
            } else {
                stringBuffer = new StringBuffer(typeArray[currentLevel - 1]);
            }
            String type = stringBuffer.toString();
            if (tempList != null) {
                for (String temp : tempList) {
                    StringBuffer tempBuffer = new StringBuffer(type);
                    tempBuffer.append(temp);
                    typeList.add(tempBuffer.toString());
                }
            } else {
                typeList.add(type);
            }
        }
        return typeList;
    }

    /**
      *
      * @param
      * @return
      * @throws
      * @date 2018/4/26 下午4:59
      * @author sunweisong
      */
    private static String searchCutNameAmongTestFileContentString(String object
            , List<String> testCaseStringList
            , String testFileContentString) {
        for (String testCaseString : testCaseStringList) {
            testFileContentString = testFileContentString.replace(testCaseString, "");
        }
        List<String> sentenceAmongTestFileContestString = getSentenceEndedWithSemicolonFromString(testFileContentString);
        if (sentenceAmongTestFileContestString == null) {
            return null;
        }
        for (String sentence : sentenceAmongTestFileContestString) {
            if (sentence.contains(object + ".")) {
                continue;
            }

            if (!isOnlyContainsInstanceAmongInitialStatement(object, sentence)) {
                continue;
            }
            /**
             *  sentence = "Predicate p = new Predicate("star");";
             *  object = "d";
             *  2018.05.28
             */

            String cutName = extractClassNameFromSentence(object, sentence);
            if (cutName != null) {
                return cutName;
            }
        }
        return null;
    }

    /**
      *
      * @param
      * @return
      * @throws
      * @date 2018/4/26 下午4:56
      * @author sunweisong
      */
    private static String extractClassNameFromSentence(String object, String sentence) {

        StringBuffer stringBuffer = null;
        boolean typeIsArray = false;
        /**
         * Fact[] fs1 ={new Fact(pre1, v)};
         * 2018.05.16
         */
        String temp = "";
        int equalIndex = sentence.indexOf("=");
        if (equalIndex != -1) {
            temp = sentence.substring(0, equalIndex).trim();
            /**
             *  temp = "Fact []database"
             *  or temp = "Fact factArray[] =  new Fact[]{fact , fact1};"
             *  2018.06.14
             */
            int leftSquareBracketsIndex = temp.indexOf("[");
            if (leftSquareBracketsIndex != -1) {
                typeIsArray = true;
                if (temp.charAt(leftSquareBracketsIndex - 1) == ' ') {
                    temp = temp.substring(0, leftSquareBracketsIndex).trim();
                    stringBuffer = new StringBuffer(temp + "[]");
                    return stringBuffer.toString();
                }
            }

            if (temp.contains(" ")) {
                String[] tempArray = temp.split(" ");
                if (typeIsArray && temp.indexOf("[") == -1) {
                    stringBuffer = new StringBuffer(tempArray[0] + "[]");
                } else {
                    /**
                     * object = "Argument"
                     * sentence =  "Argument[] arguments=null;"
                     * 2018.08.08
                     */
                    String variable = tempArray[1].trim();
                    if (!variable.equals(object)) {
                        return null;
                    }
                    stringBuffer = new StringBuffer(tempArray[0]);
                }
                String cutName = stringBuffer.toString();
                stringBuffer = null;
                return cutName;
            }
            temp = sentence.substring(equalIndex + 1).trim();
            char firstChar = temp.charAt(0);
            if (firstChar == '{') {
                // type is an array
                typeIsArray = true;
                int lastBigParenthesesIndex = temp.lastIndexOf("}");
                temp = temp.substring(1, lastBigParenthesesIndex);
                if (temp.contains("new ")) {
                    int newStringIndex = sentence.indexOf("new ");
                    temp = temp.substring(newStringIndex + 3).trim();
                    if (temp.contains("[")) {
                        int squareBracketsIndex = temp.indexOf("[");
                        temp = temp.substring(0, squareBracketsIndex).trim();
                    }
                    if (temp.contains("(")) {
                        int firstLeftParenthesesIndex = temp.indexOf("(");
                        temp = temp.substring(0, firstLeftParenthesesIndex).trim();
                    }
                    if (typeIsArray) {
                        stringBuffer = new StringBuffer(temp + "[]");
                    } else {
                        stringBuffer = new StringBuffer(temp);
                    }
                    String cutName = stringBuffer.toString();
                    stringBuffer = null;
                    return cutName;
                }
            } else {
                if (temp.contains("new ")) {
                    int newStringIndex = temp.indexOf("new ");
                    temp = temp.substring(newStringIndex + 3).trim();
                    int firstLeftParenthesesIndex = temp.indexOf("(");
                    int firstSquareBracketsIndex = temp.indexOf("[");
                    if (firstSquareBracketsIndex != -1) {
                        if (firstLeftParenthesesIndex != -1) {
                            if (firstSquareBracketsIndex < firstLeftParenthesesIndex) {
                                temp = temp.substring(0, firstSquareBracketsIndex).trim();
                                typeIsArray = true;
                            } else {
                                temp = temp.substring(0, firstLeftParenthesesIndex).trim();
                            }
                        }
                    } else {
                        if (firstLeftParenthesesIndex != -1) {
                            temp = temp.substring(0, firstLeftParenthesesIndex).trim();
                        }
                    }

                    if (typeIsArray) {
                        stringBuffer = new StringBuffer(temp + "[]");
                    } else {
                        stringBuffer = new StringBuffer(temp);
                    }
                    String cutName = stringBuffer.toString();
                    stringBuffer = null;
                    return cutName;
                }
            }
        } else {
            /**
             * object = "c" or "c2";
             * sentence = "Argument c,c2;"
             * 2018.06.05
             */
            if (isOnlyContainsInstanceAmongInitialStatement(object, sentence)) {
                int instanceIndex = getInstanceIndexAmongStatement(object, sentence);
                if (instanceIndex != -1) {
                    sentence = sentence.substring(0, instanceIndex + 1).trim();
                    if (sentence.contains(" ")) {
                        String[] tempArray = sentence.split(" ");
                        stringBuffer = new StringBuffer(tempArray[0]);
                    }
                }
            }


            //            String regex1 = "\\s*\\w+\\s(" + object + ")\\;";
            //            String regex2 = "\\s*\\w+\\s(" + object + ")\\s*\\=(.*?)\\;";
            //            if (Pattern.matches(regex1, sentence)
            //                    || Pattern.matches(regex2, sentence)) {
            //                int objectStringIndex = sentence.indexOf(object);
            //                sentence = sentence.substring(0, objectStringIndex);
            //                stringBuffer = new StringBuffer(sentence.trim());
            //            }
        }

        if (stringBuffer == null) {
            return null;
        }
        temp = stringBuffer.toString();
        stringBuffer = null;
        return temp;
    }

    /**
      *
      * @param
      * @return
      * @throws
      * @date 2018/4/25 下午7:47
      * @author sunweisong
      */
    private static String searchCutNameAmongTestCaseString(String object, String testCaseName
            , List<String> testCaseStringList) {
        String cutName = null;
        for (String testCaseString : testCaseStringList) {
            if (testCaseString.contains("@Before")
                    || testCaseString.contains("@org.junit.Before")
                    || testCaseString.contains("@After")
                    || testCaseString.contains("@org.junit.After")) {
                continue;
            }
            String tempTestCaseName = extractTestCaseNameFromTestCaseString(testCaseString);
            if (tempTestCaseName.equals(testCaseName)) {
                List<String> sentenceAmongTestCaseStringList = getSentenceEndedWithSemicolonFromString(testCaseString);
                if (sentenceAmongTestCaseStringList == null) {
                    break;
                }
                for (String sentence : sentenceAmongTestCaseStringList) {
                    if (sentence.contains(object + ".")) {
                        continue;
                    }

                    /**
                     * sentence = "	Datalog d1=new Datalog(predicate, new Argument[0]);"
                     * object = "Argument";
                     * 2018.06.01
                     */
                    if (!isOnlyContainsInstanceAmongInitialStatement(object, sentence)) {
                        continue;
                    }

                    cutName = extractClassNameFromSentence(object, sentence);
                    if (cutName != null && !"".equals(cutName)) {
                        return cutName;
                    }
                }
            }
        }
        return cutName;
    }

    /**
      *
      * @param
      * @return
      * @throws
      * @date 2018/4/25 下午6:21
      * @author sunweisong
      */
    private static boolean isParenthesesMatchAmongString(String string) {
        char[] alphaArray = string.toCharArray();
        int count = 0;
        StringBuffer buffer = new StringBuffer();
        for (char alpha : alphaArray) {
            buffer.append(alpha);
            if (alpha == '(') {
                count++;
            }
            if (alpha == ')') {
                count--;
            }
            if (buffer.length() > 0 && count == 0
                    && buffer.indexOf("(") != -1) {
                buffer.setLength(0);
            }
        }
        String temp = buffer.toString();
        if (!temp.contains("(") && !temp.contains(")")) {
            return true;
        }
        return false;
    }

    /**
      *
      * @param
      * @return
      * @throws
      * @date 2018/4/25 下午6:12
      * @author sunweisong
      */
    private static String removeContentsInQuotes(String string) {
        Matcher matcher = Pattern.compile("\"(.*?)\"").matcher(string);
        while(matcher.find()){
            string = string.replace(matcher.group(), "");
        }
        return string;
    }

    /**
      *
      * @param
      * @return
      * @throws
      * @date 2018/4/25 下午5:58
      * @author sunweisong
      */
    private static boolean isMoreThanOneSetOfParenthesesAmongString(String string) {
        char[] alphaArray = string.toCharArray();
        int count = 0;
        int setOfParenthesesNumber = 0;
        StringBuffer buffer = new StringBuffer();
        for (char alpha : alphaArray) {
            if (alpha == '(') {
                count++;
            }
            if (alpha == ')') {
                count--;
            }
            buffer.append(alpha);
            if (buffer.length() > 0 && count == 0
                    && buffer.indexOf("(") != -1) {
                setOfParenthesesNumber++;
                buffer.setLength(0);
            }
            if (setOfParenthesesNumber > 1) {
                buffer = null;
                break;
            }
        }
        if (setOfParenthesesNumber > 1) {
            return true;
        }
        return false;
    }

    /**
      *
      * @param
      * @return
      * @throws
      * @date 2018/4/25 下午5:45
      * @author sunweisong
      */
    private static List<String> getSentenceContainsInvokeMethod(List<String> sentenceStringList) {
        List<String> sentenceContainsInvokeMethodList = new ArrayList<>();
        /**
         * assertEquals(false,new Datalog(predicate1,arguments1).compatibleWith(new Fact(predicate2,values1)));
         * 2018.05.16
         */
        String regex = "(\\s*(\\w+\\.)+\\w+\\(.*?\\)\\.*\\s*\\;)|"
                + "(\\s*\\w+\\.\\w+\\(\\.*?\\).*\\s*\\;)|"
                + "(\\s*new\\s\\w+\\(.*?\\)\\.\\w+\\(.*?\\)\\.*?\\s*\\;)";
        for (String sentenceString : sentenceStringList) {
            Matcher matcher = Pattern.compile(regex).matcher(sentenceString);
            while (matcher.find()) {
                String find = matcher.group();
                sentenceContainsInvokeMethodList.add(find.trim());
            }
        }
        return sentenceContainsInvokeMethodList;
    }

    /**
     * Get sentences ended with semicolon from functions with "Test" annotation.
     *
     * @param string
     * @return 2017年10月31日
     */
    public static List<String> getSentenceEndedWithSemicolonFromString(String string) {
        int start = string.indexOf("{");
        int end = string.lastIndexOf("}");
        String temp = string.substring(start + 1, end).trim();
        if ("".equals(temp)) {
            return null;
        }
        StringBuffer stringBuffer = new StringBuffer(" " + temp);
        temp = stringBuffer.toString();
        if (!"".equals(temp)) {
            List<String> sentences = null;
            String regex = "((\\s\\w+)|(\\s\\w+\\s\\w+)|(\\s*\\w+\\.\\w+))(\\s*\\=\\s*new\\s\\w+(\\<.*?\\>)*\\(.*?\\)\\s*\\;)|"
                    + "(\\s\\w+\\s\\w+)(\\<.*?\\>)*(\\;|\\s*\\=\\s*\\w+\\;)|"
                    + "(\\s\\w+\\s*\\=\\s*\\w+\\;)|"
                    + "(\\s\\w+\\s*\\=\\s*(.*?)\\;)|"
                    + "(\\s*\\w+\\s\\w+\\s*\\=\\s*\\w+\\.\\w+\\(.*?\\)\\;)|"
                    + "(\\s*\\w+\\s*\\=\\s*\\w+\\.\\w+\\(.*?\\)\\;)|"
                    + "(\\s*\\w+\\[\\]\\s*\\w+\\s*\\=((\\s*new\\s\\w+\\[\\])|(\\s*))\\{.*?\\}\\;)|"
                    + "(((\\s\\w+)|(\\s*\\w+\\.\\w+))(\\.\\w+\\(.*?\\)\\;))|"
                    + "(\\s\\w+\\[\\]\\s\\w+\\s*\\=\\s*\\w+\\.\\w+\\(.*?\\)\\;)|"
                    + "(\\s\\w+\\[\\]\\s\\w+\\;)|"
                    + "((\\s\\w+\\[\\]\\s\\w+\\s*\\=\\s*new\\s\\w+\\[(.*?)\\]\\;))|"
                    + "(\\s*\\w+\\[\\]\\s\\w+\\s*\\=\\s*new\\s\\w+\\(.*?\\)\\;)|"
                    + "(\\s*\\w+\\s\\w+\\[\\]\\s*\\=\\s*(((()|(new\\s\\w+\\[\\]))\\{.*?\\})|(\\w+\\.\\w+\\(.*?\\)))\\;)|"
                    + "(\\s*\\w+\\s*\\[\\]\\s*\\w+\\s*\\=\\s*\\{.*?\\}\\;)|"
                    + "(\\s*\\w+\\s*\\[\\]\\s*\\w+\\s*\\=\\s*null\\s*\\;)|"
                    + "(\\s*\\w+\\<.*?\\>\\s\\w+(.*?)\\;)|"
                    + "(\\s*\\w+\\s\\w+\\s*\\;)|"
                    + "(\\s*\\w+\\s\\w+\\s*\\=\\s*\\\"\\w+\\\"\\s*\\;)|"
                    + "(\\s*\\w+\\s\\w+\\s*=\\s*\\w+\\s*\\;)|"
                    + "(\\s*\\w+\\s\\w+\\s*\\=\\s*(new)\\s\\w+\\(.*?\\)\\s*\\;)|"
                    + "(\\s*\\w+\\s(\\w+\\,\\s*)+\\w+\\;)|"
                    + "(\\s\\w+\\s*(\\(.*?\\))\\;)";
            Matcher m = Pattern.compile(regex).matcher(temp);
            while (m.find()) {
                String findString = m.group();

                /**
                 * fail("expected NullPointerException for value");
                 * 2018.06.19
                 */
                Boolean isFailFunction = Pattern.matches("\\s*fail\\s*\\(.*?\\)\\;", findString);
                if (isFailFunction) {
                    continue;
                }

                /**
                 * Modify.
                 * The string like " for(****) { ***;" are fund.
                 * 2017年12月20日
                 */
                if (findString.contains("for")) {
                    while (true) {
                        if (isIdentifierAmongStatement("for", findString)) {
                            int leftBraceIndex = findString.indexOf("{");
                            if (leftBraceIndex != -1) {
                                findString = findString.substring(leftBraceIndex + 1);
                            } else {
                                leftBraceIndex = findString.indexOf(")");
                                findString = findString.substring(leftBraceIndex + 1);
                            }
                        } else {
                            break;
                        }
                    }
                }


                if (findString.contains("if")) {
                    String regex2 = "\\s*if\\s*\\(.*?\\)\\s*\\{.*?\\}\\s*else\\s*\\{.*?\\}";
                    Matcher matcher = Pattern.compile(regex2).matcher(findString);
                    while (matcher.find()) {
                        String findIfString = m.group();
                        findString = findString.replace(findIfString, "");
                    }

                    while (true) {
                        if (isIdentifierAmongStatement("if", findString)) {
                            int tempIndex = findString.indexOf("{");
                            if (tempIndex == -1) {
                                tempIndex = findString.indexOf(")");
                            }
                            findString = findString.substring(tempIndex + 1);
                        } else {
                            break;
                        }
                        if (isIdentifierAmongStatement("else", findString)) {
                            int tempIndex = findString.indexOf("{");
                            findString = findString.substring(tempIndex + 1);
                        } else {
                            break;
                        }
                    }
                }
                if (sentences == null) {
                    sentences = new ArrayList<String>();
                }
                sentences.add(findString);
            }
            return sentences;
        } else {
            return null;
        }
    }

    /**
      * 
      * @param 
      * @return
      * @throws
      * @date 2018/6/12 下午2:48
      * @author sunweisong
      */
    private static boolean isIdentifierAmongStatement(String instance
            , String statement) {
        boolean isOnlyContainsObjectInStatement = false;
        int instanceIndex = statement.indexOf(instance);
        int instanceLength = instance.length();
        int alphaBeforeInstanceIndex = instanceIndex - 1;
        int alphaAfterInstanceIndex = instanceIndex + instanceLength;
        boolean beforeFlag = false;
        boolean afterFlag = false;
        while (instanceIndex != -1) {
            if (alphaBeforeInstanceIndex < 0) {
                beforeFlag = true;
            } else {
                char alpha = statement.charAt(alphaBeforeInstanceIndex);
                if (!CharUtil.isVariableAlpha(alpha) && alpha != '\"') {
                    beforeFlag = true;
                }
            }
            if (alphaAfterInstanceIndex < statement.length()) {
                char alpha = statement.charAt(alphaAfterInstanceIndex);
                if (!CharUtil.isVariableAlpha(alpha) && alpha != '\"') {
                    if (alpha == ' ') {
                        alpha = statement.charAt(alphaAfterInstanceIndex + 1);
                        if (!CharUtil.isVariableAlpha(alpha) && alpha != '\"') {
                            afterFlag = true;
                        }
                    } else {
                        afterFlag = true;
                    }
                }
            }
            if (beforeFlag == true && afterFlag == true) {
                isOnlyContainsObjectInStatement = true;
                break;
            }
            statement = statement.substring(instanceIndex + 1);
            instanceIndex = statement.indexOf(instance);
            alphaBeforeInstanceIndex = instanceIndex - 1;
            alphaAfterInstanceIndex = instanceIndex + instanceLength;
            beforeFlag = false;
            afterFlag = false;
        }
        return isOnlyContainsObjectInStatement;
    }

    /**
      * 
      * @param 
      * @return
      * @throws
      * @date 2018/5/6 下午6:51
      * @author sunweisong
      */
    public static List<StatementModel> extractStatementModelFromString(String string) {
        int start = string.indexOf("{");
        int end = string.lastIndexOf("}");
        String temp = string.substring(start + 1, end).trim();
        if ("".equals(temp)) {
            return null;
        }
        StringBuffer stringBuffer = new StringBuffer(" " + temp);
        temp = stringBuffer.toString();
        List<StatementModel> statementModelList = null;
        /**
         * Substitution test,test1;
         * 2018.05.30
         */
        /**
         * Datalog bodys2[] = { new Datalog(predicate2, arguments2) };
         */
        String regex = "((\\s\\w+)|(\\s\\w+\\s\\w+)|(\\s*\\w+\\.\\w+))(\\s*\\=\\s*new\\s\\w+(\\<.*?\\>)*\\(.*?\\)\\s*\\;)|"
                + "(\\s\\w+\\s\\w+)(\\<.*?\\>)*(\\;|\\s*\\=\\s*\\w+\\;)|"
                + "(\\s\\w+\\s*\\=\\s*\\w+\\;)|"
                + "(\\s\\w+\\s*\\=\\s*(.*?)\\;)|"
                + "(\\s*\\w+\\s\\w+\\s*\\=\\s*\\w+\\.\\w+\\(.*?\\)\\;)|"
                + "(\\s*\\w+\\s*\\=\\s*\\w+\\.\\w+\\(.*?\\)\\;)|"
                + "(\\s*\\w+\\[\\]\\s\\w+\\s*\\=((\\s*new\\s\\w+\\[\\])|(\\s*))\\{.*?\\}\\;)|"
                + "(((\\s\\w+)|(\\s*\\w+\\.\\w+))(\\.\\w+\\(.*?\\)\\;))|"
                + "(\\s\\w+\\[\\]\\s\\w+\\s*\\=\\s*\\w+\\.\\w+\\(.*?\\)\\;)|"
                + "(\\s\\w+\\[\\]\\s\\w+\\;)|"
                + "((\\s\\w+\\[\\]\\s\\w+\\s*\\=\\s*new\\s\\w+\\[(.*?)\\]\\;))|"
                + "(\\s*\\w+\\[\\]\\s\\w+\\s*\\=\\s*new\\s\\w+\\(.*?\\)\\;)|"
                + "(\\s*\\w+\\s\\w+\\[\\]\\s*\\=\\s*(((()|(new\\s\\w+\\[\\]))\\{.*?\\})|(\\w+\\.\\w+\\(.*?\\)))\\;)|"
                + "(\\s*\\w+\\s*\\[\\]\\s*\\w+\\s*\\=\\s*\\{.*?\\}\\;)|"
                + "(\\s*\\w+\\s*\\[\\]\\s*\\w+\\s*\\=\\s*null\\s*\\;)|"
                + "(\\s*\\w+\\<.*?\\>\\s\\w+(.*?)\\;)|"
                + "(\\s*\\w+\\s\\w+\\s*\\;)|"
                + "(\\s*\\w+\\s\\w+\\s*\\=\\s*\\\"\\w+\\\"\\s*\\;)|"
                + "(\\s*\\w+\\s\\w+\\s*=\\s*\\w+\\s*\\;)|"
                + "(\\s*\\w+\\s\\w+\\s*\\=\\s*(new)\\s\\w+\\(.*?\\)\\s*\\;)|"
                + "(\\s*\\w+\\s(\\w+\\,\\s*)+\\w+\\;)|"
                + "(\\s\\w+\\s*\\(.*?\\)\\;)";
        Matcher m = Pattern.compile(regex).matcher(temp);
        int count = 0;
        while (m.find()) {
            String findString = m.group();

            Boolean isFailFunction = Pattern.matches("\\s*fail\\s*\\(.*?\\)\\;", findString);
            if (isFailFunction) {
                continue;
            }
            count++;

            /**
             * "catch(NullPointerException e){ } Predicate predicate2 = new Predicate("name");"
             * 2018.05.26
             */
            if (findString.contains("(")) {
                int leftParenthesesIndex = findString.indexOf("(");
                String temp1 = findString.substring(0, leftParenthesesIndex).trim();
                if ("catch".equals(temp1)) {
                    int firstRightBraceIndex = findString.indexOf("}");
                    if (firstRightBraceIndex != -1) {
                        findString = findString.substring(firstRightBraceIndex + 1);
                    }
                }
            }
            if (findString.contains("for")) {
                while (true) {
                    if (isIdentifierAmongStatement("for", findString)) {
                        int leftBraceIndex = findString.indexOf("{");
                        if (leftBraceIndex != -1) {
                            findString = findString.substring(leftBraceIndex + 1);
                        } else {
                            leftBraceIndex = findString.indexOf(")");
                            findString = findString.substring(leftBraceIndex + 1);
                        }
                    } else {
                        break;
                    }
                }

            }


            /**
             * find = "if (index != -1) { if (!to.get(index).equals(val)) { Assert.assertEquals(index, val);"
             * 2018.05.31
             *  if (argument==null) throw new value("aa");
             *  2018.06.01
             */
            if (findString.contains("if")) {
                String regex2 = "\\s*if\\s*\\(.*?\\)\\s*\\{.*?\\}\\s*else\\s*\\{.*?\\}";
                Matcher matcher = Pattern.compile(regex2).matcher(findString);
                while (matcher.find()) {
                    String findIfString = m.group();
                    findString = findString.replace(findIfString, "");
                }
                while (true) {
                    if (isIdentifierAmongStatement("if", findString)) {
                        int tempIndex = findString.indexOf("{");
                        if (tempIndex == -1) {
                            tempIndex = findString.indexOf(")");
                        }
                        findString = findString.substring(tempIndex + 1);
                    } else {
                        break;
                    }
                    if (isIdentifierAmongStatement("else", findString)) {
                        int tempIndex = findString.indexOf("{");
                        findString = findString.substring(tempIndex + 1);
                    } else {
                        break;
                    }
                }
            }


            if (statementModelList == null) {
                statementModelList = new ArrayList<>();
            }

            StatementModel statementModel = new StatementModel(findString, count, 0);
            statementModelList.add(statementModel);
        }
        return statementModelList;
    }


    /**
     * Extract all functions annotated by junit's annotation
     *
     * @param string
     * @return
     * @throws
     * @date 2018/4/11 下午8:20
     * @author sunweisong
     */
    private static List<String> extractMethodWithJUnitAnnotationFromString(String string) {
        List<String> testCaseStringList = new ArrayList<>();
        String content = string;
        if (content.contains("\"")) {
            content = ParenthesisUtil.removeBracesFromString(content);
        }
        StringBuffer buffer = new StringBuffer();

        /**
         * "@RunWith(value=Parameterized.class) public class ProgramTest {...}"
         * 2018.05.26
         */
        int classIndex = content.indexOf(" class ");
        int atIndex = content.indexOf("@");
        if (atIndex < classIndex) {
            content = content.substring(atIndex + 1);
        }

        int index = content.indexOf("@");
        int index1 = index;
        int judge = 0;
        while (index != -1) {
            index1 = index;
            char[] charArray = content.toCharArray();
            for (; index1 < charArray.length; index1++) {
                buffer.append(charArray[index1]);
                if (charArray[index1] == '{') {
                    judge++;
                }
                if (charArray[index1] == '}') {
                    judge--;
                }
                String temp = buffer.toString();
                if (judge == 0 && temp.contains("@")
                        && temp.contains("{") && temp.contains("}")) {
                    if (temp.contains(" void ")) {
                        testCaseStringList.add(temp);
                    }

                    buffer.setLength(0);
                    break;
                }
            }
            if (index1 == charArray.length) {
                break;
            }
            content = content.substring(index1 + 1);
            index = content.indexOf("@");
        }
        return testCaseStringList;
    }


    /**
      * 
      * @param 
      * @return
      * @throws
      * @date 2018/6/19 下午10:52
      * @author sunweisong
      */
    private static Map<Integer, InvokeMethodModel> cleanUpContestantTestFragment(List<TestFileModel> testFileModelList) {
        Map<Integer, InvokeMethodModel> allTFMap = new HashMap<>();
        for (TestFileModel testFileModel : testFileModelList) {
            List<InvokeMethodModel> testMethodList = testFileModel.getTestMethodList();
            if (testMethodList == null || testMethodList.size() == 0) {
                continue;
            }
            for (InvokeMethodModel newInvokeMethodModel : testMethodList) {
                int mid = newInvokeMethodModel.getmId();
                InvokeMethodModel oldInvokeMethodModel = allTFMap.get(mid);
                if (oldInvokeMethodModel == null) {
                    allTFMap.put(mid, newInvokeMethodModel);
                } else {
                    int invokeTimes = oldInvokeMethodModel.getInvokeTimes() + 1;
                    oldInvokeMethodModel.setInvokeTimes(invokeTimes);
                    mergeInvokeMethodModelWithSameMID(oldInvokeMethodModel, newInvokeMethodModel);
                }
            }
        }
        if (allTFMap.size() > 0) {
            return allTFMap;
        } else {
            allTFMap = null;
            return null;
        }

    }

    /**
      * 
      * @param 
      * @return
      * @throws
      * @date 2018/6/19 下午10:47
      * @author sunweisong
      */
    private static void mergeInvokeMethodModelWithSameMID(InvokeMethodModel oldInvokeMethodModel
            , InvokeMethodModel newInvokeMethodModel) {
        List<StatementModel> oldRelatedInstantiateStatementList = oldInvokeMethodModel.getRelatedInstantiateStatementList();
        List<StatementModel> newRelatedInstantiateStatementList = newInvokeMethodModel.getRelatedInstantiateStatementList();
        if (oldRelatedInstantiateStatementList == null) {
            if (newRelatedInstantiateStatementList != null) {
                oldInvokeMethodModel.setRelatedInstantiateStatementList(newRelatedInstantiateStatementList);
            }
        } else {
            List<StatementModel> statementList = new ArrayList<>();
            statementList.addAll(oldRelatedInstantiateStatementList);
            if (newRelatedInstantiateStatementList != null) {
                boolean flag = false;
                for (StatementModel newModel : newRelatedInstantiateStatementList) {
                    for (StatementModel oldModel : oldRelatedInstantiateStatementList) {
                        if (oldModel.isEqualStatement(newModel)) {
                            flag = true;
                            break;
                        }
                    }
                    if (flag) {
                        continue;
                    } else {
                        statementList.add(newModel);
                    }
                }
            }
            if (statementList.size() > oldRelatedInstantiateStatementList.size()) {
                oldInvokeMethodModel.setRelatedInstantiateStatementList(statementList);
            }
        }

        List<StatementModel> oldRelatedParameterizedStatementList = oldInvokeMethodModel.getRelatedParameterizedStatementList();
        List<StatementModel> newRelatedParameterizedStatementList = newInvokeMethodModel.getRelatedParameterizedStatementList();
        if (oldRelatedParameterizedStatementList == null) {
            if (newRelatedParameterizedStatementList != null) {
                oldInvokeMethodModel.setRelatedParameterizedStatementList(newRelatedParameterizedStatementList);
            }
        } else {
            List<StatementModel> statementList = new ArrayList<>();
            statementList.addAll(oldRelatedParameterizedStatementList);
            if (newRelatedParameterizedStatementList != null) {
                boolean flag = false;
                for (StatementModel newModel : newRelatedParameterizedStatementList) {
                    for (StatementModel oldModel : oldRelatedParameterizedStatementList) {
                        if (oldModel.isEqualStatement(newModel)) {
                            flag = true;
                            break;
                        }
                    }
                    if (flag) {
                        continue;
                    } else {
                        statementList.add(newModel);
                    }
                }
            }
            if (statementList.size() > oldRelatedParameterizedStatementList.size()) {
                oldInvokeMethodModel.setRelatedParameterizedStatementList(statementList);
            }
        }

        List<StatementModel> oldRelatedInvokedStatementList = oldInvokeMethodModel.getRelatedInvokedStatementList();
        List<StatementModel> newRelatedInvokedStatementList = newInvokeMethodModel.getRelatedInvokedStatementList();
        if (oldRelatedInvokedStatementList == null) {
            if (newRelatedInvokedStatementList != null) {
                oldInvokeMethodModel.setRelatedInvokedStatementList(newRelatedInvokedStatementList);
            }
        } else {
            List<StatementModel> statementList = new ArrayList<>();
            statementList.addAll(oldRelatedInvokedStatementList);
            if (newRelatedInvokedStatementList != null) {
                boolean flag = false;
                for (StatementModel newModel : newRelatedInvokedStatementList) {
                    for (StatementModel oldModel : oldRelatedInvokedStatementList) {
                        if (oldModel.isEqualStatement(newModel)) {
                            flag = true;
                            break;
                        }
                    }
                    if (flag) {
                        continue;
                    } else {
                        statementList.add(newModel);
                    }
                }
            }
            if (statementList.size() > oldRelatedInvokedStatementList.size()) {
                oldInvokeMethodModel.setRelatedInvokedStatementList(statementList);
            }
        }
    }
}