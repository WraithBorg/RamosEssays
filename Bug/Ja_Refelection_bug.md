# JAVA 反射遇到重载方法产生的BUG
```java
public class Reflection_BUG {
    private void score(int score) {
        System.out.println("int grade =" + score);
    }

    private void score(Integer score) {
        System.out.println("Integer grade =" + score);
    }

    public static void main(String[] args) throws Exception {
        Reflection_BUG reflectionTest = new Reflection_BUG();
        reflectionTest.score(100);
        reflectionTest.score(Integer.valueOf(100));

        reflectionTest.getClass().getDeclaredMethod("score", Integer.TYPE).invoke(reflectionTest, Integer.valueOf("60"));
        reflectionTest.getClass().getDeclaredMethod("score", Integer.class).invoke(reflectionTest, Integer.valueOf("60"));
    }
    // 输出结果
    // int grade =100
    // Integer grade =100
    // int grade =60
    // Integer grade =60
    
    // 如果「不通过反射」,传入Integer.valueOf(100),走的是Integer重载.     
    // int grade =100
    // Integer grade =100
    
    // 但是呢,反射不是根据入参类型确定方法重载的,而是「以反射获取方法时传入的方法名称和参数类型来确定」的
    // int grade =60
    // Integer grade =60
}

```
