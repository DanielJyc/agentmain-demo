package com.danieljyc.agent.premain;

import java.lang.instrument.Instrumentation;

/**
 * @description: premain 方式
 * @author: jiayancheng
 * @email: jiayancheng@foxmail.com
 * @datetime: 2020/1/18 8:26 PM
 * @version: 1.0.0
 */
public class PremainMain {
    /**
     * 注意，这个premain方法签名是Java Agent约定的，不要随意修改
     */
    public static void premain(String agentArgs, Instrumentation instrumentation) {
        System.out.println("start execute premain function.");
        instrumentation.addTransformer(new ProducerTransformer());
    }

}
