package com.github.charlemaznable.instrument;

import lombok.val;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.util.HashMap;
import java.util.Map;

public class Agent {

    public static void premain(String agentArgs, Instrumentation instrumentation) {
        val allLoadedClasses = instrumentation.getAllLoadedClasses();
        val allLoadedClassesMap = new HashMap<String, Class>();
        try {
            for (val loadedClass : allLoadedClasses) {
                if (loadedClass == null) continue;
                if (loadedClass.getCanonicalName() == null) continue;
                allLoadedClassesMap.put(loadedClass.getCanonicalName(), loadedClass);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(allLoadedClassesMap);
        Map<String, byte[]> rewriteClasses = ClassesLoadUtil.getRewriteClasses(agentArgs);
        if (allLoadedClassesMap.size() == 0 || rewriteClasses.size() == 0) {
            return;
        }

        for (String className : rewriteClasses.keySet()) {
            byte[] classBytes = rewriteClasses.get(className);

            if (classBytes == null || classBytes.length == 0) {
                System.out.println("从 rewriteClasses 找不到class: " + className);
                continue;
            }

            Class redefineClass = allLoadedClassesMap.get(className);
            if (redefineClass == null) {
                System.out.println("从 allLoadedClassesMap 找不到class: " + className);
                continue;
            }

            System.out.println("开始redefineClasses: " + className);
            ClassDefinition classDefinition = new ClassDefinition(redefineClass, classBytes);
            try {
                instrumentation.redefineClasses(classDefinition);

                System.out.println("结束redefineClasses: " + className);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
