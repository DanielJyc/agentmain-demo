package com.danieljyc.agent.attach;

import com.sun.tools.attach.VirtualMachine;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * @description: 先运行需要植入代码的ProducerApplication类
 * @author: jiayancheng
 * @email: jiayancheng@foxmail.com
 * @datetime: 2020/1/18 9:04 PM
 * @version: 1.0.0
 */
public class AttachMain {
    public static void main(String[] args) {
        // ProducerApplication的jvm进程ID
        String jvmPid = "2773";

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
