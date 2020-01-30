package com.danieljyc.agent.transformer;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Objects;

/**
 * @description: 通过transform拦截
 * @author: jiayancheng
 * @email: jiayancheng@foxmail.com
 * @datetime: 2020/1/18 8:18 PM
 * @version: 1.0.0
 */

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
        System.out.println(className);

        // 通过javasssist，在函数执行前后注入信息
        try {
            ClassPool classPool = ClassPool.getDefault();
            CtClass ctClass = classPool.get(className);
            CtMethod[] declaredMethods = ctClass.getDeclaredMethods();
            for (CtMethod declaredMethod : declaredMethods) {
                // 只处理produceProduct方法
                if (Objects.equals("produceProduct", declaredMethod.getName())) {
                    System.out.println("insert code ...");
                    //  在方法执行之前加入打印语句
                    declaredMethod.insertBefore("System.out.println(\"开始生产...\");");
                    // 打印所有入参
                    declaredMethod.insertBefore("System.out.println($$);");
                    // 在方法执行之后加入打印语句
                    declaredMethod.insertAfter("System.out.println(\"生产结束\");");
                    // 打印所有返回结果
                    declaredMethod.insertAfter("System.out.println($_);");
                }
            }
            return ctClass.toBytecode();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return classfileBuffer;
    }
}