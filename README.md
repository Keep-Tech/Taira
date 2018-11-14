# Taira
一个 byte 序列化库，助力使用 byte 协议的解析和生成。

## Taira - 轻量的数据 byte 序列化/反序列化工具
### 特点
- 简单易用的 API
- 高效的处理效率、极小的数据量
- 为 IoT 而生（或是任何对传输数据量有要求的场景）
***
### 快速开始

build.gradle
```
dependencies {
    compile 'com.gotokeep.keep:taira:0.1.0'
}
```
pom.xml
```
<dependency>
  <groupId>com.gotokeep.keep</groupId>
  <artifactId>taira</artifactId>
  <version>0.1.0</version>
  <type>pom</type>
</dependency>
```

```
/**
 * 实现 TairaData，@ParamField 标注字段
 */
class Foo implements TairaData {
    @ParamField(order = 0)
    private int value;
    public void setValue(int value) {
        this.value = value;
    }
}

// 序列化/反序列化
Foo foo = new Foo();
foo.setValue(123);
byte[] result = Taira.DEFAULT.toBytes(foo)
byte[] receivedBytes = ...; // data transfer
Foo receivedFoo = Taira.DEFAULT.fromBytes(receivedBytes);
```

***

### 详细使用

#### TairaData

- 实现这个接口的 data class 才允许序列化/反序列化，且实现类必须有**无参构造函数**

##### 支持的字段类型

- 基本类型：byte、boolean、char、short、int、float、long、double
- ByteArray 类型：String、byte[]
- 集合类型：List、Set、非 byte 的 Array
- 嵌套 TairaData 类型

> 一些限制：
> 集合类型的成员类型只能为**基本类型、嵌套 TairaData 类型**（但是不允许定义为 interface 和 abstract）

##### ParamField 注解

- order：定义 field 的顺序，用于所有类型字段
- bytes：定义 field 序列化使用的 byte 长度，可用在基本类型上时可以用于兼容其他平台的数据长度、节约传输数据量；也可用于定长类型用于限制长度
- length：定义 List、Set、数组的长度

> 一些限制：
> order 必须是从 0 递增的连续整数，任意两个 field 的 order 不能相同
> ByteArray 类型必须指定 bytes 值
> length 用在最大 order 的字段上时可以省略，否则必须指定 length；嵌套的 TairaData 无论是否最大 order 都必须指定 length

##### 字节序/字符集

- 默认可以直接使用 Taira.DEFAULT，如果需要指定字节序或处理 String 时的字符集，可以使用 `Taira(ByteOrder order, Charset charset)` 构造实例

##### 异常

- TairaAnnotationException：序列化/反序列化之前会根据上述规则进行检查，违反规则的时候会抛出
- TairaIllegalValueException：序列化的时候会检查实际数据是否满足定义长度，超出定义的 bytes/length 值的时候会抛出
- TairaInternalException：内部错误，设置`Taira.DEBUG = true`时会抛出

***
