### 创建多级菜单数据结构
```java
/*
* 菜单对象
*/
public class SprMenu {
    private String id;
    private String name;
    private String parentId;
    private List<SprMenu> childs;
    /************************************* Constructor *************************************/
    public SprMenu (String id, String name, String parentId) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
    }
    public SprMenu (String id, String name, String parentId, List<SprMenu> childs) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
        this.childs = childs;
    }
    /************************************ Getter Setter ************************************/
    public String getId () {
        return id;
    }
    public void setId (String id) {
        this.id = id;
    }
    public String getName () {
        return name;
    }
    public void setName (String name) {
        this.name = name;
    }
    public String getParentId () {
        return parentId;
    }
    public void setParentId (String parentId) {
        this.parentId = parentId;
    }
    public List<SprMenu> getChilds () {
        return childs;
    }
    public void setChilds (List<SprMenu> childs) {
        this.childs = childs;
    }
}
// 多级菜单工具类
public class SprMenuUtils {
    public static void group (List<SprMenu> menus) {
        List<SprMenu> collect = menus.stream().filter(m -> m.getParentId().equals(""))
                .peek(m -> m.setChilds(getChilds(m, menus))).collect(Collectors.toList());
        System.out.println(JSONArray.toJSONString(collect));

    }
    private static List<SprMenu> getChilds (SprMenu root, List<SprMenu> all) {
        return all.stream().filter(m -> Objects.equals(m.getParentId(), root.getId()))
                .peek(m -> m.setChilds(getChilds(m, all))).collect(Collectors.toList());
    }
}
// 测试
public class SprMenuTest {
    public static void main (String[] args) {
        List<SprMenu> menus = Arrays.asList(
                new SprMenu("1", "根节点1", ""),
                new SprMenu("2", "根节点2", ""),
                new SprMenu("3", "根节点3", ""),
                new SprMenu("4", "子节点4", "2"),
                new SprMenu("5", "子节点5", "2"),
                new SprMenu("6", "子节点6", "2"),
                new SprMenu("7", "子节点7", "3"),
                new SprMenu("8", "子节点8", "3"),
                new SprMenu("9", "子节点9", "3"),
                new SprMenu("10", "子子节点10", "9"),
                new SprMenu("11", "子子节点11", "9"),
                new SprMenu("12", "子子节点12", "9")
        );
        SprMenuUtils.group(menus);
    }
}
```
