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

package com.github.myoss.phoenix.code.format.eclipse;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;

import org.assertj.core.util.Lists;
import org.eclipse.jface.text.BadLocationException;
import org.junit.Assert;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.github.myoss.phoenix.code.format.eclipse.imports.ImportsSorter;
import com.github.myoss.phoenix.code.format.eclipse.imports.impl.ImportsComparator;
import com.github.myoss.phoenix.code.format.eclipse.imports.impl.ImportsSorter452;
import com.github.myoss.phoenix.code.format.eclipse.utils.FileUtils;
import com.github.myoss.phoenix.code.format.eclipse.utils.ImportsUtils;
import com.github.myoss.phoenix.core.exception.BizRuntimeException;

import lombok.extern.slf4j.Slf4j;

/**
 * {@link JavaCodeFormatter} 测试类
 *
 * @author Jerry.Chen
 * @since 2018年7月18日 上午10:09:56
 */
@Slf4j
public class JavaCodeFormatterTests {
    @Test
    public void importOrderFileTest1() {
        Properties properties = ImportsUtils.readPropertiesFile("eclipse-formatter-config/Default.importorder");
        List<String> list = ImportsUtils.loadImportOrderFile(properties);
        for (String s : list) {
            log.info("{}", s);
        }
        List<String> excepted = Lists.newArrayList("java", "javax", "org", "com");
        Assert.assertEquals(excepted, list);
    }

    /**
     * 比较2个配置文件的格式差异点
     */
    //    @Test
    public void compareEclipseCodeFormatConfigFileTest1() {
        String path1 = "";
        String path2 = "";

        Properties properties1 = FileUtils.readXmlJavaSettingsFile(path1, "Default");
        Properties properties2 = FileUtils.readXmlJavaSettingsFile(path2, "Default");

        Iterator<Entry<Object, Object>> iterator = properties2.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<Object, Object> next = iterator.next();
            String key2 = (String) next.getKey();
            if (properties1.containsKey(key2)) {
                String value2 = (String) next.getValue();
                String value1 = properties1.getProperty(key2);
                if (value1.equals(value2)) {
                    properties1.remove(key2);
                    iterator.remove();
                } else {
                    log.info("key: {}, value1: {}, value2: {}", key2, value1, value2);
                }
            }
        }

        log.info("path1 和 path2 配置{}", ((properties1.size() == 0 && properties2.size() == 0) ? "相同" : "不相同"));
        log.info("\n");
        log.info("不同属性 path1: {} \n{}", path1, JSON.toJSONString(properties1, true));
        log.info("\n");
        log.info("不同属性 path2: {} \n{}", path2, JSON.toJSONString(properties2, true));
    }

    public static String getRootOutputPath(String childDirectorName, boolean isPhoenixCoreSrc) {
        Objects.requireNonNull(childDirectorName);
        Path targetFolder;
        try {
            targetFolder = Paths.get(JavaCodeFormatterTests.class.getResource("/").toURI()).getParent();
        } catch (URISyntaxException ex) {
            throw new BizRuntimeException(ex);
        }
        Objects.requireNonNull(targetFolder);
        if (isPhoenixCoreSrc) {
            return targetFolder.getParent()
                    .getParent()
                    .getParent()
                    .resolve("phoenix-core")
                    .resolve(childDirectorName)
                    .toString();
        } else {
            return targetFolder.getParent().resolve(childDirectorName).toString();
        }
    }

    // @Test
    public void formatDirectoryTest1() {
        String sourceCodePath = getRootOutputPath("src/main/java", true);
        ImportsSorter importsSorter = new ImportsSorter452(new ImportsComparator());
        JavaCodeFormatter javaCodeFormatter = new JavaCodeFormatter(importsSorter);
        List<String> result = javaCodeFormatter.formatDirectory(sourceCodePath);
        for (String s : result) {
            log.info("{}", s);
        }
        Assert.assertEquals(0, result.size());
    }

    //    @Test
    public void formatFileTest1() {
        String sourceCodePath = "";
        ImportsSorter importsSorter = new ImportsSorter452(new ImportsComparator());
        JavaCodeFormatter javaCodeFormatter = new JavaCodeFormatter(importsSorter);
        boolean result = javaCodeFormatter.formatFile(sourceCodePath);
        Assert.assertTrue(result);
    }

    @Test
    public void formatTextTest1() throws BadLocationException {
        String source = "import java.io.IOException;\n" + "import java.io.StringReader;\n"
                + "import com.github.myoss.phoenix.core.constants.PhoenixConstants;\n" + "import java.util.ArrayList;\n"
                + "import lombok.AccessLevel;\n" + "\n" + "import java.io.InputStream;\n"
                + "import com.github.myoss.phoenix.core.exception.BizRuntimeException;\n" + "\n"
                + "import com.github.myoss.phoenix.core.lang.io.StreamUtil;\n" + "import java.util.Properties;\n"
                + "import lombok.NoArgsConstructor;\n" + "\n" + "import java.util.HashSet;\n"
                + "import java.util.Comparator;\n" + "import java.util.Set;\n" + "import java.util.List;\n"
                + "import java.util.TreeMap;\n" + "/**\n" + " * Java import代码格式化工具类\n" + " *\n"
                + " * @author Jerry.Chen\n" + " * @since    2018年7月18日 下午1:45:21\n" + " */\n"
                + "@NoArgsConstructor(access = AccessLevel.PRIVATE)\n" + "public class ImportsUtils {\n" + "\t/**\n"
                + "\t\t * 换行符\n" + "\t */\n" + "\tpublic static final String N = \"\\n\";\n" + "\n" + "\t/**\n" + "* \n"
                + " *\n" + "\t *   get class simple name\n" + "\t *\n" + "\t *      \n"
                + "\t *        @param qualified class qualified\n" + "\t *     @return class simple name\n" + "\t */\n"
                + "\tpublic static String getSimpleName(String       qualified) {\n"
                + "\t\tint lastDot = qualified.lastIndexOf(\".\");\n" + "\t\tif (lastDot == -1) {\n"
                + "\t\t\t      return qualified;\n" + "\t\t}\n"
                + "\t\treturn      qualified.substring(lastDot + 1, qualified.length());\n" + "\t}\n" + "}";

        ImportsSorter importsSorter = new ImportsSorter452(new ImportsComparator());
        JavaCodeFormatter javaCodeFormatter = new JavaCodeFormatter(importsSorter);
        String formatText = javaCodeFormatter.formatText(new StringBuilder(source));

        String excepted = "import java.io.IOException;\n" + "import java.io.InputStream;\n"
                + "import java.io.StringReader;\n" + "import java.util.ArrayList;\n" + "import java.util.Comparator;\n"
                + "import java.util.HashSet;\n" + "import java.util.List;\n" + "import java.util.Properties;\n"
                + "import java.util.Set;\n" + "import java.util.TreeMap;\n" + "\n"
                + "import com.github.myoss.phoenix.core.constants.PhoenixConstants;\n"
                + "import com.github.myoss.phoenix.core.exception.BizRuntimeException;\n"
                + "import com.github.myoss.phoenix.core.lang.io.StreamUtil;\n" + "\n" + "import lombok.AccessLevel;\n"
                + "import lombok.NoArgsConstructor;\n" + "\n" + "/**\n" + " * Java import代码格式化工具类\n" + " *\n"
                + " * @author Jerry.Chen\n" + " * @since 2018年7月18日 下午1:45:21\n" + " */\n"
                + "@NoArgsConstructor(access = AccessLevel.PRIVATE)\n" + "public class ImportsUtils {\n" + "    /**\n"
                + "     * 换行符\n" + "     */\n" + "    public static final String N = \"\\n\";\n" + "\n" + "    /**\n"
                + "     * get class simple name\n" + "     *\n" + "     * @param qualified class qualified\n"
                + "     * @return class simple name\n" + "     */\n"
                + "    public static String getSimpleName(String qualified) {\n"
                + "        int lastDot = qualified.lastIndexOf(\".\");\n" + "        if (lastDot == -1) {\n"
                + "            return qualified;\n" + "        }\n"
                + "        return qualified.substring(lastDot + 1, qualified.length());\n" + "    }\n}\n";
        log.info("\n\n{}", excepted);
        Assert.assertEquals(excepted, formatText);
    }
}
