# 通用对象构造器
```java
/**
 * 打工人
 */
public class Worker {
    private String name;
    private int age;
    private String address;
    private List<String> hobby;
    private Map<String, String> score;
    /************************************* Constructor *************************************/
    @Override
    public String toString () {
        return "Worker{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", hobby=" + hobby +
                ", address='" + address + '\'' +
                ", gift=" + score +
                '}';
    }
    /************************************ Getter Setter ************************************/
    public String getName () {
        return name;
    }
    public void setName (String name) {
        this.name = name;
    }
    public void setAge (int age) {
        this.age = age;
    }
    public void setHobby (String hobby) {
        this.hobby = Optional.ofNullable(this.hobby).orElse(new ArrayList<>());
        this.hobby.add(hobby);
    }
    public void setAddress (String address) {
        this.address = address;
    }
    public void setGift (String day,String gift) {
        this.score = Optional.ofNullable(this.score).orElse(new HashMap<>());
        this.score.put(day,gift);
    }
    public int getAge () {
        return age;
    }
    public List<String> getHobby () {
        return hobby;
    }
    public void setHobby (List<String> hobby) {
        this.hobby = hobby;
    }
    public String getAddress () {
        return address;
    }
    public Map<String, String> getScore () {
        return score;
    }
    public void setScore (Map<String, String> score) {
        this.score = score;
    }
} 
```
```java
/**
 * 对象构造器
 */
public class SprBuilder<T> {
    private final Supplier<T> instantiator;
    private List<Consumer<T>> modifier = new ArrayList<>();
    public SprBuilder (Supplier<T> instantiator) {
        this.instantiator = instantiator;
    }
    public static <T> SprBuilder<T> of (Supplier<T> instantiator) {
        return new SprBuilder<>(instantiator);
    }
    public <P1> SprBuilder<T> with (Consumer1<T, P1> consumer, P1 p1) {
        Consumer<T> c = instance -> consumer.accept(instance, p1);
        modifier.add(c);
        return this;
    }
    public <P1, P2> SprBuilder<T> with (Consumer2<T, P1, P2> consumer, P1 p1, P2 p2) {
        Consumer<T> c = instance -> consumer.accept(instance, p1, p2);
        modifier.add(c);
        return this;
    }
    public <P1, P2, P3> SprBuilder<T> with (Consumer3<T, P1, P2, P3> consumer, P1 p1, P2 p2, P3 p3) {
        Consumer<T> c = instance -> consumer.accept(instance, p1, p2, p3);
        modifier.add(c);
        return this;
    }
    public T build () {
        T value = instantiator.get();
        modifier.forEach(modifier -> modifier.accept(value));
        modifier.clear();
        return value;
    }
    @FunctionalInterface
    public interface Consumer1<T, P1> {
        void accept (T t, P1 p1);
    }
    @FunctionalInterface
    public interface Consumer2<T, P1, P2> {
        void accept (T t, P1 p1, P2 p2);
    }
    @FunctionalInterface
    public interface Consumer3<T, P1, P2, P3> {
        void accept (T t, P1 p1, P2 p2, P3 p3);
    }
}
```
```java
/**
 * 测试
 */
public class BuilderTest {
    public static void main (String[] args) {
        Worker worker = SprBuilder.of(Worker::new)
                .with(Worker::setName, "Zzz")
                .with(Worker::setAge, 18)
                .with(Worker::setAddress, "上海")
                .with(Worker::setHobby, "吃")
                .with(Worker::setHobby, "喝")
                .with(Worker::setGift, "语文", "99")
                .with(Worker::setGift, "数学", "100")
                .build();
        System.out.println(worker);
    }
}
```
