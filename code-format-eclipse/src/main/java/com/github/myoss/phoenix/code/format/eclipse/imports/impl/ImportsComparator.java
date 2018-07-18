/*
 * Copyright 2018-2018 https://github.com/myoss
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.myoss.phoenix.code.format.eclipse.imports.impl;

import java.util.Comparator;

import org.apache.commons.lang3.ClassUtils;

import com.github.myoss.phoenix.code.format.eclipse.utils.ImportsUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Java import代码格式化工具排序规则
 *
 * <pre>
 *     参考：https://github.com/krasa/EclipseCodeFormatter/blob/master/src/java/krasa/formatter/plugin/ImportsComparator.java
 * </pre>
 *
 * @author Jerry.Chen
 * @since 2018年7月18日 下午1:35:17
 */
@Slf4j
public class ImportsComparator implements Comparator<String> {
    @Override
    public int compare(String o1, String o2) {
        String simpleName1 = simpleName(o1);
        String containerName1 = getPackage(o1, simpleName1);

        String simpleName2 = simpleName(o2);
        String containerName2 = getPackage(o2, simpleName2);

        int i = containerName1.compareTo(containerName2);

        if (i == 0) {
            i = simpleName1.compareTo(simpleName2);
        }
        return i;
    }

    private String getPackage(String qualified, String simple) {
        String substring;
        if (qualified.length() > simple.length()) {
            substring = qualified.substring(0, qualified.length() - simple.length() - 1);
        } else {
            substring = ImportsUtils.getPackage(qualified);
        }
        return substring;
    }

    private String simpleName(String qualified) {
        Class<?> clazz = getClass(qualified);
        if (clazz != null) {
            Class containingClass = clazz;
            StringBuilder simpleName = new StringBuilder(clazz.getSimpleName());
            while (containingClass != null && containingClass.getDeclaringClass() != null) {
                containingClass = containingClass.getDeclaringClass();
                if (containingClass != null) {
                    simpleName.insert(0, containingClass.getSimpleName() + ".");
                }
            }
            return simpleName.toString();
        } else {
            return ImportsUtils.getSimpleName(qualified);
        }
    }

    private Class<?> getClass(String qualified) {
        try {
            return ClassUtils.getClass(qualified);
        } catch (Throwable ex) {
            // Class or one of its dependencies is not present...
            return null;
        }
    }
}
