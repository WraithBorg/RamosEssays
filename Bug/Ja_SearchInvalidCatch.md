# 检索不规范的catch代码块
`Author:zxu`
**使用场景:**
> 当发生空指针异常时,后端没有输出异常信息,而直接将e.getMessage返回给前端
  这样,前端无法显示异常信息,后端日志也找不到异常信息
  所以会造成无法定位代码异常的情况,导致线上程序难以维护

#### 代码示例
```java
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * 使用场景:当发生空指针异常时,后端没有输出异常信息,而直接将e.getMessage返回给前端
 * 这样,前端无法显示异常信息,后端日志也找不到异常信息
 * 所以会造成无法定位代码异常的情况,导致线上程序难以维护
 * <p>
 * 此工具就是为了查找上述情况而设计,下面代码是一个错误示例
 */
public class SearchInvalidCatch {
    private static AtomicInteger atomicInteger = new AtomicInteger();
    /**
     * main
     */
    public static void main(String[] args) {
//        applicableScope();//错误代码演示
        toDoSearch( "C:\\zxu_gitspace\\sprdemo\\src\\com");//查找该路径下的错误代码
        System.out.println("共检索不规范代码:" + atomicInteger.get() + "处");
    }
    /**
     * 使用场景:当发生空指针异常时,后端没有输出异常信息,而直接将e.getMessage返回给前端
     * 这样,前端无法显示异常信息,后端日志也找不到异常信息
     * 所以会造成无法定位代码异常的情况,导致线上程序难以维护
     * <p>
     * 此工具就是为了查找上述情况而设计,下面代码是一个错误示例
     */
    private static Map applicableScope() {
        Map<String, Object> map = new HashMap<>();
        Object a = null;
        // 1.捕获 Exception
        try {
            a.toString();
        } catch (Exception e) {
            map.put("success", false);
            map.put("msg", e.getMessage());
        }
        System.out.println(map.toString());// 输出结果：{msg=null, success=false}

        // 1.捕获 RuntimeException
        try {
            a.toString();
        } catch (RuntimeException e) {
            map.put("success", false);
            map.put("msg", e.getMessage());
        }
        System.out.println(map.toString());// 输出结果：{msg=null, success=false}
        return map;
    }

    public static void toDoSearch(String targetFolder) {
        File dirFile = new File(targetFolder);
        ArrayList<String> dirAllStrArr = DirSearching.DirAll(dirFile);
        for (String s : dirAllStrArr) {
            if (!s.contains(".java")) {
                continue;
            }
            searchingInvalidCatchCode(s, readFileByLines(s));
        }
    }

    /**
     * 检索无法输出空指针异常的catch
     */
    private static void searchingInvalidCatchCode(String fileName, String text) {
        final Pattern catchPatt = Pattern.compile("catch([\\w\\W]*?)\\{[\\w\\W]*?}");//获取catch内容
        Matcher matcher = catchPatt.matcher(text);
        while (matcher.find()) {
            String group = matcher.group();
            // 白名单
            if (!group.contains("Exception")) {
                continue;
            }
            if (group.contains("\"Exception\"")) {
                continue;
            }
            // 只查找空指针异常
            if (true) {
                if (group.contains("NoSuchAlgorithmException")) {
                    continue;
                }
                if (group.contains("BusinessException")) {
                    continue;
                }
                if (group.contains("OutStoreException")) {
                    continue;
                }
                if (group.contains("IOException")) {
                    continue;
                }
                if (group.contains("MaximumLimitException")) {
                    continue;
                }
                if (group.contains("UnknownAccountException")) {
                    continue;
                }
                if (group.contains("SQLException")) {
                    continue;
                }
                if (group.contains("AuthenticationException")) {
                    continue;
                }
                if (group.contains("IncorrectCredentialsException")) {
                    continue;
                }
                if (group.contains("NumberFormatException")) {
                    continue;
                }
                if (group.contains("ParseException")) {
                    continue;
                }
                if (group.contains("UnsupportedEncodingException")) {
                    continue;
                }
                if (group.contains("ExplicitException")) {
                    continue;
                }
            }
            // 校验code
            if (!group.contains(".error(") // 排除logger.error,logger.warn,.printStackTrace(),throw new Exception
                    && !group.contains(".warn(")
                    && !group.contains(".printStackTrace()")
                    && !group.contains("throw")
            ) {
                try {
                    atomicInteger.getAndIncrement();
                    String classFName = fileName.substring(fileName.lastIndexOf("\\") + 1);
                    if (!group.contains("【【") || !group.contains("】】")) {//处理catch代码块只占一行的情况
                        System.out.println(classFName + ":" + group);
                        continue;
                    }
                    String substring = group.substring(group.lastIndexOf("【【") + 2, group.lastIndexOf("】】"));
                    System.out.println(classFName + ":" + substring);
                } catch (StringIndexOutOfBoundsException e) {
                    e.getMessage();
                }
            }
        }
    }

    /**
     * 以行为单位读取文件
     */
    private static String readFileByLines(String fileName) {
        StringBuilder sb = new StringBuilder();
        File file = new File(fileName);
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String tempString;
            int line = 1;
            while ((tempString = reader.readLine()) != null) {
                sb.append("【【").append(line).append("】】").append(tempString);
                line++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * 查询文件夹下的所有文件
     */
    private static class DirSearching {
        static ArrayList<String> dirAllStrArr = new ArrayList<>();

        static ArrayList<String> DirAll(File dirFile) {
            if (dirFile.exists()) {
                File[] files = dirFile.listFiles();
                assert files != null;
                for (File file : files) {
                    if (file.isDirectory()) {
                        DirAll(file);
                    } else {
                        if (dirFile.getPath().endsWith(File.separator)) {
                            dirAllStrArr.add(dirFile.getPath() + file.getName());
                        } else {
                            dirAllStrArr.add(dirFile.getPath() + File.separator + file.getName());
                        }
                    }
                }
            }
            return dirAllStrArr;
        }
    }
}
```

#### 输出结果
```
ChartDataTask.java:69
BaseDaoImpl.java:157
BaseDaoImpl.java:215
```
