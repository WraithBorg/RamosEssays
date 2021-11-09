# 搜索指定文件的内容
```java
/**
 * 使用场景:搜索指定文件的内容
 */
public class SearchTextInFiles {
    static String searchText = "DbNameThread.set("; // 指定搜索内容
    static String searchdictionary = "C:\\zxu_gitspace\\sprdemo\\src\\com"; //指定搜索目录
    public static void main(String[] args) {
        toDoSearch(searchdictionary);//查找该路径下的代码
    }
    public static void toDoSearch(String targetFolder) {
        File dirFile = new File(targetFolder);
        ArrayList<String> dirAllStrArr = DirSearching.DirAll(dirFile);
        for (String s : dirAllStrArr) {
            // 自定义文件过滤规则
            if (!s.contains("Filter.java")) {
                continue;
            }
            searchingCode(s, readFileByLines(s));
        }
    }
    private static void searchingCode(String fileName, String text) {
        if (text.contains(searchText)) {
            System.out.println(fileName);
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
