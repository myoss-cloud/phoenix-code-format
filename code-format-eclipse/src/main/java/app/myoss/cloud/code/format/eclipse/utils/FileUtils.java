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

package app.myoss.cloud.code.format.eclipse.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import app.myoss.cloud.core.exception.BizRuntimeException;

/**
 * 文件工具类
 *
 * @author Jerry.Chen
 * @since 2018年7月19日 上午2:40:55
 */
public class FileUtils {
    /**
     * 读取 Eclipse 代码格式化规则文件，转换为 properties
     *
     * @param formatConfigFile EclipseCodeFormatter 格式化规则文件路径
     * @param formatConfigFileProfile EclipseCodeFormatter 格式化规则文件中的 name
     *            属性，具体使用哪个 profile
     * @return 格式化规则属性配置
     */
    public static Properties readXmlJavaSettingsFile(URL formatConfigFile, String formatConfigFileProfile) {
        Properties properties = new Properties();
        try (InputStream inputStream = formatConfigFile.openStream()) {
            return readXmlJavaSettingsFile(inputStream, properties, formatConfigFileProfile);
        } catch (IOException ex) {
            throw new BizRuntimeException("read file: " + formatConfigFile, ex);
        }
    }

    /**
     * 读取 Eclipse 代码格式化规则文件，转换为 properties
     *
     * @param formatConfigFile EclipseCodeFormatter 格式化规则文件路径
     * @param formatConfigFileProfile EclipseCodeFormatter 格式化规则文件中的 name
     *            属性，具体使用哪个 profile
     * @return 格式化规则属性配置
     */
    public static Properties readXmlJavaSettingsFile(String formatConfigFile, String formatConfigFileProfile) {
        Properties properties = new Properties();
        File file = new File(formatConfigFile);
        try (InputStream inputStream = new FileInputStream(file)) {
            return readXmlJavaSettingsFile(inputStream, properties, formatConfigFileProfile);
        } catch (IOException ex) {
            throw new BizRuntimeException("read file: " + formatConfigFile, ex);
        }
    }

    /**
     * 读取 Eclipse 代码格式化规则文件，转换为 properties
     *
     * @param file EclipseCodeFormatter 格式化规则文件
     * @param properties 现有的规则配置
     * @param profile EclipseCodeFormatter 格式化规则文件中的 name 属性，具体使用哪个 profile
     * @return 格式化规则属性配置
     */
    public static Properties readXmlJavaSettingsFile(InputStream file, Properties properties, String profile) {
        int defaultSize = properties.size();
        if (profile == null) {
            throw new IllegalStateException("no profile selected, go to settings and select proper settings file");
        }
        boolean profileFound = false;
        try {
            // load file profiles
            org.w3c.dom.Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
            doc.getDocumentElement().normalize();

            NodeList profiles = doc.getElementsByTagName("profile");
            if (profiles.getLength() == 0) {
                throw new IllegalStateException(
                        "loading of profile settings failed, file does not contain any profiles");
            }
            for (int temp = 0; temp < profiles.getLength(); temp++) {
                Node profileNode = profiles.item(temp);
                if (profileNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element profileElement = (Element) profileNode;
                    String name = profileElement.getAttribute("name");
                    if (profile.equals(name)) {
                        profileFound = true;
                        NodeList childNodes = profileElement.getElementsByTagName("setting");
                        if (childNodes.getLength() == 0) {
                            throw new IllegalStateException(
                                    "loading of profile settings failed, profile has no settings elements");
                        }
                        for (int i = 0; i < childNodes.getLength(); i++) {
                            Node item = childNodes.item(i);
                            if (item.getNodeType() == Node.ELEMENT_NODE) {
                                Element attributeItem = (Element) item;
                                String id = attributeItem.getAttribute("id");
                                String value = attributeItem.getAttribute("value");
                                properties.setProperty(id.trim(), value.trim());
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }
        if (!profileFound) {
            throw new IllegalStateException("profile not found in the file " + file);
        }
        if (properties.size() == defaultSize) {
            throw new IllegalStateException("no properties loaded, something is broken, file:");
        }
        return properties;
    }
}
