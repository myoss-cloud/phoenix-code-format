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

package app.myoss.cloud.code.format.eclipse.imports.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;

import app.myoss.cloud.code.format.eclipse.imports.ImportsSorter;
import app.myoss.cloud.code.format.eclipse.utils.ImportsUtils;

/**
 * Java import代码格式化工具
 *
 * <pre>
 *     参考：https://github.com/krasa/EclipseCodeFormatter/blob/master/src/java/krasa/formatter/plugin/ImportsSorter452.java
 * </pre>
 *
 * @author Jerry.Chen
 * @since 2018年7月18日 下午1:27:49
 */
public class ImportsSorter452 implements ImportsSorter {
    private List<String>               importOrder         = new ArrayList<>();
    private Set<String>                allImportOrderItems = new HashSet<>();
    private Comparator<? super String> importsComparator;

    /**
     * Java import代码格式化工具
     *
     * @param importOrder 排序规则
     * @param comparator 自定义排序规则
     */
    public ImportsSorter452(List<String> importOrder, ImportsComparator comparator) {
        this.importsComparator = comparator;
        List<String> importOrderCopy = (importOrder != null ? new ArrayList<>(importOrder) : defaultOrder());
        normalizeStaticOrderItems(importOrderCopy);
        putStaticItemIfNotExists(importOrderCopy);
        this.importOrder.addAll(importOrderCopy);
        this.allImportOrderItems.addAll(importOrderCopy);
    }

    /**
     * Java import代码格式化工具
     *
     * @param comparator 自定义排序规则
     */
    public ImportsSorter452(ImportsComparator comparator) {
        this(null, comparator);
    }

    @Override
    public String sort(List<String> imports) {
        List<String> template = new ArrayList<>(importOrder);
        ArrayListMultimap<String, String> matchingImports = ArrayListMultimap.create();
        ArrayList<String> notMatching = new ArrayList<>();
        filterMatchingImports(matchingImports, notMatching, imports);
        mergeMatchingItems(template, matchingImports);
        mergeNotMatchingItems(template, notMatching);
        removeNewLines(template);
        return ImportsUtils.getImportResult(template);
    }

    private void removeNewLines(List<String> template) {
        List<String> temp = new ArrayList<>();

        boolean previousWasNewLine = false;
        boolean anyContent = false;
        for (String s : template) {
            if (!anyContent && s.equals(ImportsUtils.N)) {
                continue;
            }
            if (s.equals(ImportsUtils.N)) {
                if (previousWasNewLine) {
                    continue;
                } else {
                    temp.add(s);
                }
                previousWasNewLine = true;
            } else {
                previousWasNewLine = false;
                anyContent = true;
                temp.add(s);
            }
        }

        Collections.reverse(temp);
        List<String> temp2 = trimNewLines(temp);
        Collections.reverse(temp2);

        template.clear();
        template.addAll(temp2);
    }

    private List<String> trimNewLines(List<String> temp) {
        List<String> temp2 = new ArrayList<>();
        boolean anyContent = false;
        for (String s : temp) {
            if (!anyContent && s.equals(ImportsUtils.N)) {
                continue;
            }
            anyContent = true;
            temp2.add(s);
        }
        return temp2;
    }

    private void putStaticItemIfNotExists(List<String> allImportOrderItems) {
        boolean contains = false;
        for (String allImportOrderItem : allImportOrderItems) {
            if ("static ".equals(allImportOrderItem)) {
                contains = true;
            }
        }
        if (!contains) {
            allImportOrderItems.add(0, "static ");
        }
    }

    private void normalizeStaticOrderItems(List<String> allImportOrderItems) {
        for (int i = 0; i < allImportOrderItems.size(); i++) {
            String s = allImportOrderItems.get(i);
            if (s.startsWith("\\#") || s.startsWith("#")) {
                allImportOrderItems.set(i, s.replace("\\#", "static ").replace("#", "static "));
            }
        }
    }

    /**
     * returns not matching items and initializes internal state
     *
     * @param imports import packages
     * @param matchingImports matched import packages
     * @param notMatching no matched import packages
     */
    private void filterMatchingImports(ArrayListMultimap<String, String> matchingImports, ArrayList<String> notMatching,
                                       List<String> imports) {
        for (String anImport : imports) {
            String orderItem = getBestMatchingImportOrderItem(anImport);
            if (orderItem != null) {
                matchingImports.put(orderItem, anImport);
            } else {
                notMatching.add(anImport);
            }
        }
        notMatching.addAll(allImportOrderItems);
    }

    private String getBestMatchingImportOrderItem(String anImport) {
        String matchingImport = null;
        for (String orderItem : allImportOrderItems) {
            if (anImport.startsWith(
                    // 4.5.1+ matches exact package name
                    "static ".equals(orderItem) || "".equals(orderItem) ? orderItem : orderItem + ".")) {
                if (matchingImport == null) {
                    matchingImport = orderItem;
                } else {
                    matchingImport = ImportsUtils.betterMatching(matchingImport, orderItem, anImport);
                }
            }
        }
        return matchingImport;
    }

    /**
     * not matching means it does not match any order item, so it will be
     * appended before or after order items
     *
     * @param template import packages template
     * @param notMatching no matched import packages
     */
    private void mergeNotMatchingItems(List<String> template, ArrayList<String> notMatching) {
        notMatching.sort(importsComparator);

        template.add(ImportsUtils.N);
        for (String notMatchingItem : notMatching) {
            if (!matchesStatic(false, notMatchingItem)) {
                continue;
            }
            boolean isOrderItem = isOrderItem(notMatchingItem, false);
            if (!isOrderItem) {
                template.add(notMatchingItem);
            }
        }
        template.add(ImportsUtils.N);
    }

    private boolean isOrderItem(String notMatchingItem, boolean staticItems) {
        boolean contains = allImportOrderItems.contains(notMatchingItem);
        return contains && matchesStatic(staticItems, notMatchingItem);
    }

    private boolean matchesStatic(boolean staticItems, String notMatchingItem) {
        boolean isStatic = notMatchingItem.startsWith("static ");
        return (isStatic && staticItems) || (!isStatic && !staticItems);
    }

    private void mergeMatchingItems(List<String> template, ArrayListMultimap<String, String> matchingImports) {
        for (int i = 0; i < template.size(); i++) {
            String item = template.get(i);
            if (allImportOrderItems.contains(item)) {
                // find matching items for order item
                Collection<String> strings = matchingImports.get(item);
                if (strings == null || strings.isEmpty()) {
                    // if there is none, just remove order item
                    template.remove(i);
                    i--;
                    continue;
                }
                ArrayList<String> matchingItems = new ArrayList<>(strings);
                matchingItems.sort(importsComparator);

                // replace order item by matching import statements
                // this is a mess and it is only a luck that it works :-]
                template.remove(i);
                if (i != 0 && !template.get(i - 1).equals(ImportsUtils.N)) {
                    template.add(i, ImportsUtils.N);
                    i++;
                }
                if (i + 1 < template.size() && !template.get(i + 1).equals(ImportsUtils.N)
                        && !template.get(i).equals(ImportsUtils.N)) {
                    template.add(i, ImportsUtils.N);
                }
                template.addAll(i, matchingItems);
                if (i != 0 && !template.get(i - 1).equals(ImportsUtils.N)) {
                    template.add(i, ImportsUtils.N);
                }

            }
        }
        // if there is \n on the end, remove it
        if (template.size() > 0 && template.get(template.size() - 1).equals(ImportsUtils.N)) {
            template.remove(template.size() - 1);
        }
    }
}
