# 泛型和通配符区别
## 泛型
泛型的好处就是在编译的时候能够检查类型安全,并且所有的强制转换都是自动和隐式  
+ ？表示不确定的 java 类型
+ T (type) 表示具体的一个java类型
+ K V (key value) 分别代表java键值中的Key Value
+ E (element) 代表Element

### 无界通配符和上界通配符
```
/**
 * ？无界通配符
 * 不确定或者不关心实际要操作的类型,可以使用无限制通配符（尖括号里一个问号,即 <?> ）,表示可以持有任何类型.
 * 像 eat 方法中,限定了上届,但是不关心具体类型是什么,
 * 所以对于传入的 Animal 的所有子类都可以支持,并且不会报错
 * 
 * 上界通配符 < ? extends E>
 * 上界用 extends 关键字声明,表示参数化的类型可能是所指定的类型,或者是此类型的子类.
 */
public class TestAni {
    public static void main(String[] args) {
        List<Dog> dogList = new ArrayList<>();
//        eat01(dogList);// 报错
        eat02(dogList);
    }
    public static void eat01(List<Animal> list) {}
    public static void eat02(List<? extends Animal> list) {}
    class Animal {}
    class Dog extends Animal {}
}
```
```
public class TestAni<E> {
    public static void main(String[] args) {
        List<Dog> dogList = new ArrayList<>();
//        new TestAni<Animal>().eat01(dogList);// 报错
        new TestAni<Animal>().eat02(dogList);
    }
    public void eat01(List<Animal> list) {}
    public void eat02(List<? extends E> list) {}
    static  class Animal {}
    static class Dog extends Animal {}
}
```

### 下界通配符
```
/**
 * 下界通配符 < ? super E>
 * 下界用 super 进行声明,表示参数化的类型可能是所指定的类型,或者是此类型的父类型,直至 Object
 */
public class TestAni<E> {
    public static void main(String[] args) {
        List<Dog> dogs = new ArrayList<>();
        List<Animal> animalList = new ArrayList<>();
        new TestAni<Dog>().eat(dogs);
        new TestAni<Dog>().eat(animalList);
    }
    public void eat(List<? super E> list) {}
    static  class Animal {}
    static class Dog extends Animal {}
}
```

### ？和 T 的区别
？和 T 都表示不确定的类型,区别在于我们可以对 T 进行操作,但是对 ？不行,如T t = operate();  
T 是一个 确定的类型,通常用于泛型类和泛型方法的定义, 
？是一个 不确定的类型,通常用于泛型方法的调用代码和形参,不能用于定义类和泛型方法  

#### 1.可以通过T来确保泛型参数的一致性
```
// 通过 T 来 确保 泛型参数的一致性
public <T extends Number> void test01(List<T> listA,List<T> listB) {}
// 通配符是 不确定的,所以这个方法不能保证两个 List 具有相同的元素类型
public void test02(List<? extends Number> listA,List<? extends Number> listB) {}
```

#### 2.类型参数可以多重限定而通配符不行
```
// 使用 & 符号设定多重边界（Multi Bounds),指定泛型类型T必须是Animal和Male,White的共有子类型,此时变量t就具有了所有限定的方法和属性
对于通配符来说,因为它不是一个确定的类型,所以不能进行多重限定
public class TestAni<E> {
    class Animal {}
    interface Male {}
    interface White {}
    public <T extends Animal & Male & White> void test01(List<T> listA,List<T> listB) {}
}
```

#### 3.通配符可以使用超类限定而类型参数不行
```
T extends A

? extends A
? super A
```

#### Class<T>和 Class<?>区别
```
public class TestAni{
    public static void main(String[] args) throws Exception {
        // 上面方法如果反射类型不是Animal一定会报ClassCastException异常
        Animal animal = (Animal)Class.forName("com.fx.share.a1212.TestAni.Animal").newInstance();
        // 使用下面的代码来代替,使得在在编译期就能直接 检查到类型的问题
        Animal animal01 = createInstance01(Animal.class);
//        Male animal02 = createInstance01(Animal.class);//编译器提示异常
    }
    public static <T> T createInstance01(Class<T> clazz ) throws Exception {
        return clazz.newInstance();
    }
    class Animal {}
}
```

```
public class TestAni{
    // 可以
    public Class<?> clazz;
    // 不可以,因为 T 需要指定类型
    public Class<T> clazzT;
}
```

```
public class TestAni<T>{
    // 可以
    public Class<?> clazz;
    // 可以
    public Class<T> clazzT;
}
```


















