# JSON序列化,Long类型被转成Integer类型
```java
public class JSON_BUG {
    public static void main(String[] args) {

        Long idValue = 3000L;
        Map<String, Object> data = new HashMap<>(2);
        data.put("id", idValue);
        data.put("name", "小A");

        System.out.println(idValue ==  data.get("id"));//输出： true
        String jsonString = JSON.toJSONString(data);

        // 反序列化时Long被转为了Integer
        Map map = JSON.parseObject(jsonString, Map.class);
        Object idObj = map.get("id");
        System.out.println("反序列化的类型是否为Integer：" + (idObj instanceof Integer));
        // 输出：反序列化的类型是否为Integer：true
    }
}
```
