# 业务描述
`Author:zxu`  

> App调用 后台getInBill或getOutBill 接口获取单据信息,后端要根据前端App的要求返回相关数据  
> 后端查询结果 经过convert转换器 转换成VO对象传递给App  
> convert转换器 是各种单据（门店入库单,门店出库单,中心入库单,中心出库单等）共用的一套转换器,随着业务的扩展,会支持各种单据  
> 所以convert的的if判断会越来越多,代码越来越冗余,而且每次修改都可能会影响其他单据,不符合开闭原则  
> 故对此进行了优化 消除了所有if判断和魔法值等

#### 目录结构
+ aold包 是优化之前的代码
+ anew包 是优化之后的代码
+ common包 是共用代码,包括 service,entity等没有参考价值的代码

#### 新旧 controller 对比
+ 新controller去掉了魔法值 "billType", "getOrgan"

#### 新旧 convert 对比
+ 新convert 利用枚举 优化了“设置单据状态”的业务代码
+ 新convert 没有任何if判断,并且不会因为业务的扩展导致代码越来越冗余,并且完全遵循开闭原则

#### 可通过 Test类 测试两个convert的输出

# 代码简介
* [右键打开链接下载代码](TempCode.zip)

## 旧代码
#### OldStoreBillController
``` java
@Controller
public class OldStoreBillController {
    /**
     * 查询入库单据信息
     */
    @RequestMapping(name = "getInBill")
    public static StoreBillVO getInBill(String billId) {
        // 查询数据 并 转换成VO给前端显示
        // storeBillService.get 方法是封装的hibernate查询方法
        // billType（单据类型）和getOrgan（收货机构） 是懒加载对象
        StoreBill bill = StoreBillService.get(billId, new String[]{"billType", "getOrgan"});
        return OldBillConverter.from(bill);
    }
    /**
     * 查询出库单据信息
     */
    @RequestMapping(name = "getOutBill")
    public static StoreBillVO getOutBill(String billId) {
        // 查询数据 并 转换成VO给前端显示
        StoreBill bill = StoreBillService.get(billId, new String[]{"billType", "sendOrgan"});
        return OldBillConverter.from(bill);
    }
}
```
#### OldBillConverter
``` java
@Component
public class OldBillConverter {
    /**
     * 将单据 StoreBill 转换成 StoreBillVO(前端展示数据结构)
     * // 1:不符合 开放封闭原则,当程序需要改变时,应该通过拓展的方式来实现变化,而不是通过修改已有代码来实现
     * // 2:随着单据类型 和 条件分支的增加,会变得更冗余,方法体会更长
     */
    public static StoreBillVO from(StoreBill billDB) {
        StoreBillVO billDTO = new StoreBillVO();
        BillTypeVO billTypeVO = null;//      单据类型
        //  为什么会有 （if ！= null）判断,因为 billType,sendOrgan和getOrgan等对象是 billDB（单据对象）的懒加载属性,
        //  而不同业务或不同单据类型 情况下,这些懒加载对象可能为空
        if (billDB.getBillType() != null) {
            billTypeVO = new BillTypeVO();
            billTypeVO.setId(billDB.getBillType().getId());
            billTypeVO.setName(billDB.getBillType().getName());
        }
        ShopVO rdcDTO = null; //      ## 发货机构
        if (billDB.getSendOrgan() != null) {
            rdcDTO = new ShopVO(billDB.getSendOrgan().getId(), billDB.getSendOrgan().getName(), billDB.getSendOrgan().getCode());
        }
        ShopVO inShopDTO = null;//      ## 收货机构
        if (billDB.getGetOrgan() != null) {
            inShopDTO = new ShopVO(billDB.getGetOrgan().getId(), billDB.getGetOrgan().getName(), billDB.getGetOrgan().getCode());
        }
        billDTO.setWorkDate(DDateUtil.formYMdHms(billDB.getBusDate()));// 设置单据日期
        if (billDB.getBillState() == 1) { // 设置单据状态 1：待提交；2：待审核； 3：已审核
            billDTO.setBillStateFlag("0");
            billDTO.setBillStateFlagName("待提交");
        } else if (billDB.getBillState() == 2) {
            billDTO.setBillStateFlag("2");
            billDTO.setBillStateFlagName("待审核");
        } else {
            billDTO.setBillStateFlag("1");
            billDTO.setBillStateFlagName("已审核");
        }
        billDTO.setInShopDTO(inShopDTO);
        billDTO.setBillBusType(billTypeVO);
        billDTO.setRdcDTO(rdcDTO);
        // 设置其他通用属性 比如金额 数量
        return billDTO;
    }
}
```
# 分割线
## 新代码
#### NewStoreBillController
``` java
@Controller
public class NewStoreBillController {
    /**
     * 查询入库单据信息 新写法
     */
    @RequestMapping(name = "getInBill")
    public static StoreBillVO getInBill(String billId) {
        // 通过链式编程 设定装配那些属性,然后查询数据 并转换成VO
        HsmBFuncUtils installProps = new HsmBFuncUtils().billType().getOrgan();
        StoreBill bill = StoreBillService.get(billId, installProps.props());
        return NewBillConverter.form(bill, installProps);
    }
    /**
     * 查询出库单据信息 新写法
     */
    @RequestMapping(name = "getOutBill")
    public static StoreBillVO getOutBill(String billId) {
        // 通过链式编程 设定装配那些属性,然后查询数据 并转换成VO
        HsmBFuncUtils installProps = new HsmBFuncUtils().billType().sendOrgan();
        StoreBill bill = StoreBillService.get(billId, installProps.props());
        return NewBillConverter.form(bill, installProps);
    }
}
```
#### NewBillConverter
``` java
@Component
public class NewBillConverter {
    /**
     * 将单据 StoreBill 转换成 StoreBillVO
     */
    public static StoreBillVO form(StoreBill bill, HsmBFuncUtils hsmBFunc) {
        StoreBillVO dto = generateBaseProps(bill);// 装配单据通用的基本属性
        install(bill, hsmBFunc, dto);// 自动根据不同情况,装配不同属性
        return dto;
    }
    /**
     * 组装基本属性,这个可能会随着业务的扩展 会有微小变动,不过改动范围已经控制在最小范围
     */
    private static StoreBillVO generateBaseProps(StoreBill billDB) {
        StoreBillVO billDTO = new StoreBillVO();
        billDTO.setId(billDB.getId());
        billDTO.setWorkDate(DDateUtil.formYMdHms(billDB.getBusDate()));// 设置单据日期
        BillStateEnum stateEnum = BillStateEnum.getEnm(billDB.getBillState());// 设置单据状态
        billDTO.setBillStateFlag(stateEnum.fVal());
        billDTO.setBillStateFlagName(stateEnum.fName());
        // 设置其他通用属性 比如金额 数量
        return billDTO;
    }
    /**
     * 根据不同情况,组装vo对象, 这个方法不会在有任何修改
     */
    private static void install(StoreBill bill, HsmBFuncUtils hsmBFunc, StoreBillVO dto) {
        for (BiFunction<StoreBill, StoreBillVO, StoreBillVO> fun : hsmBFunc.li()) {
            fun.apply(bill, dto);
        }
    }
}
```
#### HsmBFuncUtils
``` java
/**
 * 自定义单据属性装配器
 */
public class HsmBFuncUtils {
    /************************************* Constructor *************************************/
    private List<BiFunction<StoreBill, StoreBillVO, StoreBillVO>> li;
    private List<String> props;
    public HsmBFuncUtils() {
        this.props = new ArrayList<>();
        this.li = new ArrayList<>();
    }
    public List<BiFunction<StoreBill, StoreBillVO, StoreBillVO>> li() {
        return li;
    }
    public String[] props() {
        return props.toArray(new String[]{});
    }
    /************************************* Props *************************************/
    // TODO 如果业务更加复杂的话,可以将 下面的方法剥离出去形成单独的类
    public HsmBFuncUtils billType() {// 单据类型 适配器
        BiFunction<StoreBill, StoreBillVO, StoreBillVO> func = (v1, v2) -> {
            BillTypeVO businessTypeDTO = new BillTypeVO();
            businessTypeDTO.setId(v1.getBillType().getId());
            businessTypeDTO.setName(v1.getBillType().getName());
            v2.setBillBusType(businessTypeDTO);
            return v2;
        };
        li.add(func);
        props.add(StoreBillTableColumns.billType);
        return this;
    }
    public HsmBFuncUtils sendOrgan() { // 发货机构 适配器
        BiFunction<StoreBill, StoreBillVO, StoreBillVO> func = (v1, v2) -> {
            ShopVO rdcDTO = new ShopVO(v1.getSendOrgan().getId(), v1.getSendOrgan().getName(), v1.getSendOrgan().getCode());
            v2.setRdcDTO(rdcDTO);
            return v2;
        };
        li.add(func);
        props.add(StoreBillTableColumns.sendOrgan);
        return this;
    }
    public HsmBFuncUtils getOrgan() {// 收货机构 适配器
        BiFunction<StoreBill, StoreBillVO, StoreBillVO> func = (v1, v2) -> {
            ShopVO inShopDTO = new ShopVO(v1.getGetOrgan().getId(), v1.getGetOrgan().getName(), v1.getGetOrgan().getCode());
            v2.setInShopDTO(inShopDTO);
            return v2;
        };
        li.add(func);
        props.add(StoreBillTableColumns.getOrgan);
        return this;
    }
}
```
