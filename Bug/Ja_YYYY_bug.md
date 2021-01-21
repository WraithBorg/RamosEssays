# YYYY-MM-dd引起的BUG
```java
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TestC {
    public static void main(String[] args) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = df.parse("2020-12-30");//// 2020年12月27日
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SimpleDateFormat df1 = new SimpleDateFormat("YYYY-MM-dd");
        SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");

        System.out.println("YYYY-MM-dd = " + df1.format(date));// YYYY-MM-dd = 2021-12-30
        System.out.println("yyyy-MM-dd = " + df2.format(date));// yyyy-MM-dd = 2020-12-30

        //因为YYYY是week-based-year，
        // 表示：当天所在的周属于的年份，一周从周日开始，周六结束，只要本周跨年，那么这周就算入下一年。
    }
}
```
