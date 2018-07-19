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

package com.github.myoss.phoenix.code.format.eclipse.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import com.github.myoss.phoenix.core.constants.PhoenixConstants;
import com.github.myoss.phoenix.core.exception.BizRuntimeException;
import com.github.myoss.phoenix.core.lang.io.StreamUtil;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Java import代码格式化工具类
 *
 * @author Jerry.Chen
 * @since 2018年7月18日 下午1:45:21
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ImportsUtils {
    /**
     * 换行符
     */
    public static final String N = "\n";

    /**
     * get class simple name
     *
     * @param qualified class qualified
     * @return class simple name
     */
    public static String getSimpleName(String qualified) {
        int lastDot = qualified.lastIndexOf(".");
        if (lastDot == -1) {
            return qualified;
        }
        return qualified.substring(lastDot + 1, qualified.length());
    }

    /**
     * get class package name
     *
     * @param qualified class qualified
     * @return class package name
     */
    public static String getPackage(String qualified) {
        int lastDot = qualified.lastIndexOf(".");
        if (lastDot == -1) {
            return "";
        }
        return qualified.substring(0, lastDot);
    }

    /**
     * convert imports to collection
     *
     * @param imports import packages
     * @return imports to collection
     */
    public static List<String> trimImports(String imports) {
        String[] split = imports.split(ImportsUtils.N);
        Set<String> strings = new HashSet<>();
        for (String s : split) {
            if (s.startsWith("import ")) {
                s = s.substring(7, s.indexOf(";"));
                strings.add(s);
            }
        }
        return new ArrayList<>(strings);
    }

    /**
     * import sorter
     *
     * @param order1 import order1
     * @param order2 import order2
     * @param anImport to be import
     * @return order1 or order2
     */
    public static String betterMatching(String order1, String order2, String anImport) {
        if (order1.equals(order2)) {
            throw new IllegalArgumentException("orders are same");
        }
        for (int i = 0; i < anImport.length() - 1; i++) {
            if (order1.length() - 1 == i && order2.length() - 1 != i) {
                return order2;
            }
            if (order2.length() - 1 == i && order1.length() - 1 != i) {
                return order1;
            }
            char orderChar1 = (order1.length() != 0 ? order1.charAt(i) : ' ');
            char orderChar2 = (order2.length() != 0 ? order2.charAt(i) : ' ');
            char importChar = anImport.charAt(i);

            if (importChar == orderChar1 && importChar != orderChar2) {
                return order1;
            } else if (importChar != orderChar1 && importChar == orderChar2) {
                return order2;
            }

        }
        return null;
    }

    /**
     * 转换排序好的 import packages 为字符串
     *
     * @param template 排序后的 import packages
     * @return 排序后的 import packages 字符串
     */
    public static String getImportResult(List<String> template) {
        StringBuilder strings = new StringBuilder();

        for (String s : template) {
            if (s.equals(ImportsUtils.N)) {
                strings.append(s);
            } else {
                strings.append("import ").append(s).append(";").append(ImportsUtils.N);
            }
        }
        return strings.deleteCharAt(strings.length() - 1).toString();
    }

    /**
     * 读取 properties 文件
     *
     * @param file properties文件路径
     * @return 获取到的 properties 属性
     */
    public static Properties readPropertiesFile(String file) {
        Properties formatterOptions;
        try (InputStream stream = ImportsUtils.class.getClassLoader().getResourceAsStream(file)) {
            formatterOptions = new Properties();
            String s = StreamUtil.copyToString(stream, PhoenixConstants.DEFAULT_CHARSET);
            StringReader reader = new StringReader(s.replace("=\\#", "=#"));
            formatterOptions.load(reader);
        } catch (IOException ex) {
            throw new BizRuntimeException("config file read error", ex);
        }
        return formatterOptions;
    }

    /**
     * 转换 EclipseCodeFormatter 排序规则
     *
     * @param file EclipseCodeFormatter 排序规则属性
     * @return EclipseCodeFormatter 排序规则集合
     */
    @SuppressWarnings("unchecked")
    public static List<String> loadImportOrderFile(Properties file) {
        TreeMap treeMap = new TreeMap(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return Integer.parseInt(o1) - Integer.parseInt(o2);
            }
        });
        treeMap.putAll(file);
        return new ArrayList<String>(treeMap.values());
    }
}
