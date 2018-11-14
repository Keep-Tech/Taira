# Taira
轻量的数据 byte 序列化/反序列化工具

## 特点
- 简单易用的 API。`toBytes()`和`fromBytes()`轻松解决 byte 序列化/反序列化
- 高效的处理效率、极小的数据量
- 为 IoT 而生（或是任何对传输数据量有要求的场景）
***

## 快速开始

#### 添加依赖
Gradle
```gradle
dependencies {
    compile 'com.gotokeep.keep:taira:0.1.0'
}
```
Pom
```xml
<dependency>
  <groupId>com.gotokeep.keep</groupId>
  <artifactId>taira</artifactId>
  <version>0.1.0</version>
  <type>pom</type>
</dependency>
```
#### Sample

```java
Foo foo = new Foo();
// 序列化到 bytes
byte[] result = Taira.DEFAULT.toBytes(foo)
// 反序列化到 Object
Foo receivedFoo = Taira.DEFAULT.fromBytes(receivedBytes);
```

```java
/**
 * Model 定义
 */
class Foo implements TairaData {
    @ParamField(order = 0)
    public int value;
}
```
***

## 详细使用

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

## 简单对比 Gson 

- [Sample](https://github.com/Keep-Tech/Taira/blob/master/TairaSample/src/main/java/com/gotokeep/keep/taira/samples/Main.java)  处理一个三层嵌套包含各种类型的 data class，执行 1000 次
- 结果：序列化/反序列化速度快于 Gson，且数据长度只有 Gson 的 1/5
```
// 原始 data class 结构
fooObject: Foo{byteField=2, barField=Bar{innerArrayVal=[Baz{bazinga=1}, Baz{bazinga=3}, Baz{bazinga=5}], floatVal=123.2, shortVal=11, longVal=1242354, booleanVal=true}, intField=103, doubleField=123.21, charField=$, bytesField=[11, 22, 33, 44], stringField='world', intListField=[3, 5, 9]}

// Taira 序列化
Taira serialize x 1000 time cost: 132
Taira serialize data size: 59

// Taira 反序列化
Taira deserialize x 1000 time cost: 58
Taira deserialize result: Foo{byteField=2, barField=Bar{innerArrayVal=[Baz{bazinga=1}, Baz{bazinga=3}, Baz{bazinga=5}], floatVal=123.2, shortVal=11, longVal=1242354, booleanVal=true}, intField=103, doubleField=123.21, charField=$, bytesField=[11, 22, 33, 44, 0], stringField='world', intListField=[3, 5, 9]}

// Gson 序列化
Gson serialize x 1000 time cost: 218
Gson serialize data size: 279

// Gson 反序列化
Gson deserialize x 1000 time cost: 83
Gson deserialize result: Foo{byteField=2, barField=Bar{innerArrayVal=[Baz{bazinga=1}, Baz{bazinga=3}, Baz{bazinga=5}], floatVal=123.2, shortVal=11, longVal=1242354, booleanVal=true}, intField=103, doubleField=123.21, charField=$, bytesField=[11, 22, 33, 44], stringField='world', intListField=[3, 5, 9]}
```

***


## License 

All assets and code are under the [![license](https://img.shields.io/github/license/GarageGames/Torque3D.svg)](https://github.com/Keep-Tech/Taira/blob/master/LICENSE)
