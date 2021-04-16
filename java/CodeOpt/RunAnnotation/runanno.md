# 运行Java注释里的代码
`Author:zxu`  
```java
class Test{
    public static void main(String[] args) {
        String name = "空大";
        // \u000dname+="牛仔";
        System.out.println(name);
    }
    // 因为通过允许 Java 源代码包含 Unicode 字符,可以确保在世界上任何一个区域编写的代码在其他地方执行.\u000d是换行符
    public static void main(String[] args) {
    // 虽然编译软件提示文件错误,但是仍然可以运行
    // 不知道 π 这个字符怎么敲出来,那么他就可以选择使用 \u03C0 来替代——编译器知道 \u03C0 就是 π 这个变量（编译器会在编译其他代码之前先解析 Unicode 字符）
        double π = Math.PI;
        System.out.println(\u03C0);
    }
}

```
