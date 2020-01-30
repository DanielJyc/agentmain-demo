# Java Agent 之二：进阶实例

上一篇讲了premain方式的一个简单实例，且不需要mvn构建。本篇首先将通过mvn依赖的方式来对agent进行打包，讲解premain模式；然后，再进一步讲解attach模式。
<a name="6WMZU"></a>
# premain模式
一般情况下，我们的应用场景是，将额外的代码注入到已有应用中。这里，我们定义已有应用代码模块为agentmain-app，需要注入的代码模块为agentmain-premain。
<a name="slpjp"></a>
## 应用模块agentmain-app
文档结构代码结构：
```shell
agentmain-app
├── pom.xml
├── src
│   └── main
│       ├── java
│       │   ├── ProducerApplication.java
│       │   └── ProductModel.java
│       └── resources
│           └── META-INF
```

关键文件ProducerApplication.java代码如下。启动后，将持续生产产品
```java
public class ProducerApplication {

    public static void main(String[] args) {
        int count = 1;
        System.out.println("start execute main function.\n");
        while (true) {
            produceProduct(new ProductModel(count++, "pcode", "pname"));
        }
    }

    private static void produceProduct(ProductModel productModel) {
        System.out.println("生产完成" + productModel.getId() + " 件。");
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
        }
    }
}
```

<a name="nkc5p"></a>
## agentmain-premain模块
该模块的代码是需要注入的代码。文档结构如下：
```
agentmain-premain
├── agentmain-demo.iml
├── pom.xml
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── danieljyc
│   │   │           └── agent
│   │   │               └── premain
│   │   │                   ├── PremainMain.java
│   │   │                   └── ProducerTransformer.java
│   │   └── resources
│   │       └── META-INF
│   │           └── MANIFEST.MF

```

- PremainMain类在agentmain-app模块的main函数之前执行。函数拦截和代码注入，通过 `addTransformer(new ProducerTransformer())` 完成。
```java
public class PremainMain {
    /**
     * 注意，这个premain方法签名是Java Agent约定的，不要随意修改
     */
    public static void premain(String agentArgs, Instrumentation instrumentation) {
        System.out.println("start execute premain function.");
        instrumentation.addTransformer(new ProducerTransformer());
    }

}
```

- ProducerTransformer类用来在需要注入代码的函数，执行前后注入需要执行的代码。如下代码，通过javassist技术，使用insertBefore在执行函数前插入代码 `System.out.println(\"开始生产...\");` 在执行函数后插入代码 `System.out.println(\"生产结束。\");` 。
```java
public class ProducerTransformer implements ClassFileTransformer {

    @Override
    public byte[] transform(ClassLoader loader, String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) {
        // 只处理ProducerApplication类
        if (!className.endsWith("ProducerApplication")) {
            return classfileBuffer;
        }

        // 好像使用premain这个className是没问题的，但使用attach时className的.变成了/，
        // 所以如果是attach，那么这里需要替换
        className = className.replace('/', '.');


        // 通过javasssist，在函数执行前后注入信息
        try {
            ClassPool classPool = ClassPool.getDefault();
            CtClass ctClass = classPool.get(className);
            CtMethod[] declaredMethods = ctClass.getDeclaredMethods();
            for (CtMethod declaredMethod : declaredMethods) {
                // 只处理produceProduct方法
                if (Objects.equals("produceProduct", declaredMethod.getName())) {
                    //  在方法执行之前加入打印语句
                    declaredMethod.insertBefore("System.out.println(\"开始生产...\");");
                    // 在方法执行之后加入打印语句
                    declaredMethod.insertAfter("System.out.println(\"生产结束。\");");
                }
            }
            return ctClass.toBytecode();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return classfileBuffer;
    }
}
```
<a name="dBM7S"></a>
## 执行

- 打包应用agentmain-app
```
# 进入目录
cd agentmain-app/
# 打包
mvn clean package
```

- 打包agentmain-premain模块
```
# 进入目录
cd agentmain-premain/
# 打包
mvn clean package
```

- 执行
```
# 执行
cd agentmain-app/target/classes
java -javaagent:/Users/daniel/gitworkspace/agent/agentmain-demo/agentmain-premain/target/agentmain-premain-1.0-SNAPSHOT-jar-with-dependencies.jar ProducerApplication
```

- 执行结果：
```java
start execute premain function.
start execute main function.
开始生产...
生产完成1 件。
生产结束。
开始生产...
生产完成2 件。
生产结束。

```

<a name="ktPic"></a>
# attach模式
<a name="J9FU3"></a>
## 应用模块agentmain-app
同premain模式
<a name="CFdTj"></a>
## agent模块：agentmain-attach
文档结构：
```
agentmain-attach
├── pom.xml
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── danieljyc
│   │   │           └── agent
│   │   │               ├── attach
│   │   │               │   └── AttachAgent.java
│   │   │               └── transformer
│   │   │                   └── ProducerTransformer.java
│   │   └── resources
│   │       └── META-INF
│   │           └── MANIFEST.MF
```

- ProducerTransformer.java跟前面的premain模式保持一致
- AttachAgent.java通过` instrumentation.addTransformer(``**new **``ProducerTransformer(), ``**true**)`,把ProducerTransformer添加到instrumentation中。然后，通过 `instrumentation.retransformClasses(clazz)` 对ProducerApplication应用类进行转换。
```java
public class AttachAgent {
    /**
     * 注意：agentmain的方法签名也是约定好的，不能随意修改
     * 其实如果要支持premain和attach两种方式的话，可以把premain和agentmain两个方法写在一个类里
     */
    public static void agentmain(String agentArgs, Instrumentation instrumentation) {
        String targetClassPath = "ProducerApplication";
        for (Class<?> clazz : instrumentation.getAllLoadedClasses()) {
            // 过滤掉不能修改的类
            if (!instrumentation.isModifiableClass(clazz)) {
                continue;
            }

            // 这里只修改我们关心的类
            if (clazz.getName().equals(targetClassPath)) {
                // 最根本的目的还是把ProducerTransformer添加到instrumentation中
                instrumentation.addTransformer(new ProducerTransformer(), true);
                try {
                    instrumentation.retransformClasses(clazz);
                } catch (UnmodifiableClassException e) {
                    e.printStackTrace();
                }
                //找到ProducerApplication后，处理结束后，不再后续遍历
                return;
            }
        }
    }
}
```

<a name="vBG68"></a>
## 执行注入模块：agentmain-attach-main
```java
agentmain-attach-main
├── pom.xml
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── danieljyc
│   │   │           └── agent
│   │   │               └── attach
│   │   │                   └── AttachMain.java
```
其中，AttachMain.java 如下
```java
public class AttachMain {
    public static void main(String[] args) {
        // ProducerApplication的jvm进程ID
        String jvmPid = "19582";

        File agentFile = new File("/Users/daniel/gitworkspace/agent/agentmain-demo/agentmain-attach/target/agentmain-attach-1.0-SNAPSHOT-jar-with-dependencies.jar");

        if (!agentFile.isFile()) {
            System.out.println("jar 不存在");
            return;
        }

        try {
            VirtualMachine jvm = VirtualMachine.attach(jvmPid);
            jvm.loadAgent(agentFile.getAbsolutePath());
            jvm.detach();
            System.out.println("attach 成功");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            TimeUnit.SECONDS.sleep(10000);
        } catch (InterruptedException e) {
        }
    }
}
```
<a name="zJO9D"></a>
## 执行

- 在IDEA中执行 `ProducerApplication#main` 
```
生产完成1 件。
生产完成2 件。
生产完成3 件。
生产完成4 件。
生产完成5 件。

```

- `jvmPid` 通过 `jps` 命令获取pid为19582
```
$ jps
11760
19582 ProducerApplication
18685 Jps
18638 Launcher
```

- 打包agent
```
cd agentmain-attach
mvn clean package
```

- 在IDEA中执行AttachMain，动态加载agent，并动态attach到ProducerApplication代码中。
  - AttachMain.main执行结果
```
attach 成功
```

  - 此时，ProducerApplication结果变为
```shell
生产完成25 件。
生产完成26 件。
ProducerApplication
insert code ...
开始生产...
生产完成27 件。
生产结束
开始生产...
生产完成28 件。
生产结束
```

<a name="zwlrv"></a>
# git代码

<a name="tjB5d"></a>
# 参考

- [Java Agent实战](https://blog.csdn.net/manzhizhen/article/details/100178857)

