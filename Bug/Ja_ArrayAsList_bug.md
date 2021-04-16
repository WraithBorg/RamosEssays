# Arrays.asList常见BUG
```java

/**
 * Arrays.asList的几个坑
 */
public class ArrayAsListTest {
    /**
     * ArrayList.toArray() 强转的坑
     * <p>
     * 因为返回的是Object类型,Object类型数组强转String数组,会发生ClassCastException.
     * 解决方案是,使用toArray()重载方法toArray(T[] a)
     */
    private static void testCast() {
        List<String> list = new ArrayList<String>(1);
        list.add("AA");
        list.add("BB");
//        String[] array21 = (String[])list.toArray();//类型转换异常
//        System.out.println(JSON.toJSONString(array21)); // 返回结果：java.lang.ClassCastException: [Ljava.lang.Object; cannot be cast to [Ljava.lang.String;
        String[] array1 = list.toArray(new String[0]);// 正常做法 
        System.out.println(JSON.toJSONString(array1)); // 返回结果：["AA","BB"]
    }

    /**
     * 使用Arrays.asLis的时候,对原始数组的修改会影响到我们获得的那个List
     */
    private static void testModify() {
        String[] arr = {"1", "2", "3"};
        List list = Arrays.asList(arr);
        arr[1] = "4";
        System.out.println("原始数组" + Arrays.toString(arr));// 返回结果：原始数组[1, 4, 3]
        System.out.println("list数组" + list);// 返回结果：list数组[1, 4, 3]
    }

    /**
     * Arrays.asList 返回的 List 不支持增删操作
     * <p>
     * Arrays.asList 返回的 List 并不是我们期望的 java.util.ArrayList,而是 Arrays 的内部类 ArrayList.
     * 内部类的ArrayList没有实现add方法,而是父类的add方法的实现,是会抛出异常的呢.
     */
    private static void testAdd() {
        String[] array = {"1", "2", "3"};
        List list = Arrays.asList(array);
        list.add("5");
        System.out.println(list.size());// 返回结果：java.lang.UnsupportedOperationException
    }

    /**
     * 基本类型不能作为 Arrays.asList方法的参数,否则会被当做一个参数
     */
    private static void testSize() {
        int[] array = {1, 2, 3};
        List list = Arrays.asList(array);
        System.out.println(list.size());// 返回结果： 1
    }
}
```
