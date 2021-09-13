# 一些 paradigm 与加速开发的方法

Delphynium 2021/9/2

## 0 lombok 库

lombok 库提供了快速声明自定义 java 类的一系列 annotations。

在类的声明前，加上

```
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
```

那么即使不显式定义，该类的所有 private 成员变量都有对应的 get 和 set 方法；同时该类也获得一个默认构造函数和全参数构造函数。

例子：

```java
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class Something {
  private Integer anything;
}
```

等价于

```java
public class Something {
  private Integer anything;
  
  public Something() {anything = new Integer();}
  public Something(Integer anything) {this.anything = anything;}
  public void setAnything(Integer anything) {this.anything = anything;}
  public Integer getAnything() {return anything;}
}
```

另外，`@Getter` 和 `@Setter` 也可以用在成员变量前，上文还等价于

```java
@NoArgsConstructor
@AllArgsConstructor
public class Something {
  private @Getter Integer anything;
  public void setAnything(Integer anything) {this.anything = anything;}
}
```

这在快速定义新的数据库 object 类时很有用。

## 1 Network 类 (`com.lapluma.knowledg.util.Network`)

由我自己实现，封装了所有与网络请求有关的代码，不排除有这样那样的问题。

但理论上，后来者不需要在任何 java 文件中再使用 okhttp 这种极其难用的库，直接使用 Network 就行了。

### 1.1 Network.RestResponse 类

一个泛型类。和与后端约定的 http response body 具有相同的结构：

```
{
	code: xxx,
	message: "xxx",
	data: {
		key0: value0,
		key1: "value1",
		key2: [value2, value3],
		...
	}
```

```java
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public static class RestResponse<T> {
    /** generic class for rest responses.
     * always deserialize json response through this.
     * if is pure message response, set T as anything, e.g. Object.
     */
    private int code;
    private String message;
    private T data;
    public boolean isSuccessful() {
        return code < 300 && code >= 200;
    }
}
```

向后端请求数据本质上是请求特定的 data 字段（可以是任何东西）。所以在与后端通信获得某一类的 data 前，先在 model 中定义对应的类。比如 login 接口返回一个 `data: {token: "xxx"}`，则定义 model.TokenPayload 类：

```java
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TokenPayload {
    private String token;
}
```

那么后续通过 Network 库，可以轻松的将整个 response body 反序列化到一个 `RestResponse<TokenPayload>` 类的实例中，token 也可以轻松的取出。

注意这里的命名，因为 token 仅在这里有用，预计后面也不会加入数据库，所以取类命叫 TokenPayload；对于可以普遍使用的数据类型，比如用户 User 类或者知识实体 Knowledge 类，没必要加 Payload 后缀。

### 1.2 Network.Postman 抽象类

对 okhttp 层层封装而来。顾名思义，负责在一个 android Activity 和一个远端 url 之间进行 post request 和 response。

不必深究它的结构和原理，只用了解使用方法就行了。从任何一个 Activity 发起 post 请求的方法（以 Json 请求为例）使用到 Postman 的子类（这里是 JsonPostman）的一个实例。

```java
JsonPostman postman = new JsonPostman(Activity.this, "localhost:8080/some-api");
postman.put("key0", value0);
postman.put("key1", "value1");
```

假设 some-api 返回的 body 中，data 字段是一个 `List<Integer>`，那么直接

```java
postman.post(new TypeReference<RestResponse<List<Integer>>>(){}, new CallbackOnResponse() {
  @Override
  public void processResponse(RestResponse<List<Integer>> restResponse) {
    // doSomething();
  }
});
```

`CallbackOnResponse` 也是 Network 类里定义的 interface，有一个 `processResponse` 方法需要被 override。

与 okhttp 的回调机制不同，Network 在回调这个方法前会做完一切处理，包括接收 http response、json 反序列化、处理超时/连接异常等。可以保证，Network 只要回调了 `processResponse` ，通过参数传入的这个 `RestResponse<?> restResponse` 是基本可信的，其内部变量 data 中的数据可以直接拿来渲染 UI。比如我在上面例子里想要取出 list 的首个元素放在一个本地变量 x 里，直接把 doSomething 置换为

```java
x = restResponse.data[0];
```

值得注意的是，对于 UI 的修改不能直接写在回调函数 `processResponse` 的域里，需要在外面包上一层

```
runOnUiThread(()->{
  // doSomething();
  // doOtherThings();
});
```

大致是因为 UI 的相关操作必须在主线程中进行，否则不并发安全。但 Postman 的请求和处理等是在分线程中运行的，所以不用担心卡顿。

### 1.3 网络调试

经过 Network 类的封装，目前所有 status code 在 [200, 300) 范围外的 response 均被视为无效，并不会回调 `processResponse`。

只要一次请求成功发出去了，但 status code 不在上述范围或者压根没收到 repsonse，都会以 snackbar 的形式在 android 视图底部打印有关提示，同时 exception 相关的栈信息也会打印在控制台。

## 2 SharedPreferences 的使用

截至目前还没有引入 SQLite 等数据库结构（因为太重了，不过后面缓存什么的估计还是得用），暂时使用 SharedPreferences 作为轻量化的替代解决方案。

SharedPreferences 是所有 Activity 都有 access 的一个数据持久化存储类。它能把各种复杂的 object 序列化成 xml 并保存在特定的空间里（不随 Appication 的生命周期销毁）。

在 ./data/MainPref.java 里定义了 MainPref 类，这不是一个单例类，但其所有实例都拥有读写 MAIN_PREF 偏好文件的权限。

目前仅有 MainPref 一个相关的类，用来读写与软件用户系统的核心有关的一些数据，比如用户的用户名、token 等。具体的实现和使用可以直接看代码，非常简单。

在需要用到 MAIN_PREF 的 Activity 里，实例化一个对象并对其操作就可以了。后续要储存更多数据，比如用户的浏览历史等，可以在 ./data 目录下新建 Java 文件，比如 HistoryPref.java，等等。

需要注意的是为了方便调试，我在 MainActivity 的 onCreate 里写了一句

```java
mainPref.clearAll();    // ONLY FOR DEBUGGING!! REMOVE BEFORE RELEASE!!
```

所以目前的持久化存储实际与 main activity 的生命周期同步。如果把这句话注释掉，那么即使 kill 掉程序的后台再进入，用户的登录状态等信息仍然是保持的。

## 3 Tool 类 (`com.lapluma.knowledg.util.Tool`)

一个小工具的集合。

### 3.1 makeSnackBar

`makeSnackBar(Activity activity, String s)` 在 activity 对应 View 的 root view（或者最合适的 parent view）的底部弹出一个好看的小 message bar，显示一条消息 s。

## 4 为了代码简洁可读的一些约定

1. 要设计一个比如按钮时，最好不要在 xml 设计界面把 onClick 属性绑定到一个 Activity 中的某个自定义函数如 `onClickSomething()` 上然后实现这个函数，而是在 onCreate 时就重载这个 View 的 onClickListener 的 `onClick(View view)` 方法。这样可以减少 Activity 中方法的数量，并减少方法命名在语义上的重复。
2. 可以在 xml 中 include 时尽量 include，不要直接改原 xml。比如在主页的中心区域加入一个推荐内容显示视图，建议另开一个 xml 设计，然后在 activity_main.xml 中 include，不然会让一个 xml 变得很长，而且 IDE 会很卡。
3. 时刻保持项目体积的最小化非常重要。不要引入不必要的资源文件。比如在利用 MaterialX 的资源时，正确的做法是先复制 layout xml 文件到自己的项目，然后手动开一个空的 activity，对照着只对自己需要的功能编写响应代码，遇到缺失的资源再引入。