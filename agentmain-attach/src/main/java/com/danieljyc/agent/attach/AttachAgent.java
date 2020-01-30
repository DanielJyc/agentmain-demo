package com.danieljyc.agent.attach;

import com.danieljyc.agent.transformer.ProducerTransformer;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

/**
 * @description: agentmain模式
 * @author: jiayancheng
 * @email: jiayancheng@foxmail.com
 * @datetime: 2020/1/18 8:52 PM
 * @version: 1.0.0
 */
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
