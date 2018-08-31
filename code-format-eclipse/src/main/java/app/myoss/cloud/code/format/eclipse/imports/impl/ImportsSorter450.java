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
 *     参考：https://github.com/krasa/EclipseCodeFormatter/blob/master/src/java/krasa/formatter/plugin/ImportsSorter450.java
 * </pre>
 *
 * @author Jerry.Chen
 * @since 2018年7月17日 下午11:11:06
 */
public class ImportsSorter450 implements ImportsSorter {
    private List<String>       importOrder         = new ArrayList<>();
    private Set<String>        allImportOrderItems = new HashSet<>();
    private Comparator<String> comparator;

    /**
     * Java import代码格式化工具
     *
     * @param importOrder 排序规则
     */
    public ImportsSorter450(List<String> importOrder) {
        List<String> importOrderCopy = new ArrayList<>(importOrder);
        normalizeStaticOrderItems(importOrderCopy);
        putStaticItemIfNotExists(importOrderCopy);
        this.importOrder.addAll(importOrderCopy);
        this.allImportOrderItems.addAll(importOrderCopy);
        this.comparator = (o1, o2) -> {
            String containerName1 = (allImportOrderItems.contains(o1) ? o1 : ImportsUtils.getPackage(o1));
            String simpleName1 = (allImportOrderItems.contains(o1) ? "" : ImportsUtils.getSimpleName(o1));

            String containerName2 = (allImportOrderItems.contains(o2) ? o2 : ImportsUtils.getPackage(o2));
            String simpleName2 = (allImportOrderItems.contains(o2) ? "" : ImportsUtils.getSimpleName(o2));
            int i = containerName1.compareTo(containerName2);

            if (i == 0) {
                i = simpleName1.compareTo(simpleName2);
            }
            return i;
        };
    }

    @Override
    public String sort(List<String> imports) {
        List<String> template = new ArrayList<>(importOrder);
        ArrayListMultimap<String, String> matchingImports = ArrayListMultimap.create();
        ArrayList<String> notMatching = new ArrayList<>();
        filterMatchingImports(imports, matchingImports, notMatching);
        mergeNotMatchingItems(false, template, notMatching);
        mergeNotMatchingItems(true, template, notMatching);
        mergeMatchingItems(template, matchingImports);
        return ImportsUtils.getImportResult(template);
    }

    private void putStaticItemIfNotExists(List<String> allImportOrderItems) {
        boolean contains = false;
        int indexOfFirstStatic = 0;
        for (int i = 0; i < allImportOrderItems.size(); i++) {
            String allImportOrderItem = allImportOrderItems.get(i);
            if ("static ".equals(allImportOrderItem)) {
                contains = true;
            }
            if (allImportOrderItem.startsWith("static ")) {
                indexOfFirstStatic = i;
            }
        }
        if (!contains) {
            allImportOrderItems.add(indexOfFirstStatic, "static ");
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
    private void filterMatchingImports(List<String> imports, ArrayListMultimap<String, String> matchingImports,
                                       ArrayList<String> notMatching) {
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
            if (anImport.startsWith(orderItem)) {
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
     * @param staticItems static import package
     * @param template import packages template
     * @param notMatching no matched import packages
     */
    private void mergeNotMatchingItems(boolean staticItems, List<String> template, ArrayList<String> notMatching) {
        notMatching.sort(comparator);

        int firstIndexOfOrderItem = getFirstIndexOfOrderItem(notMatching, template, staticItems);
        int indexOfOrderItem = 0;
        for (String notMatchingItem : notMatching) {
            if (!matchesStatic(staticItems, notMatchingItem)) {
                continue;
            }
            boolean isOrderItem = isOrderItem(notMatchingItem, staticItems);
            if (isOrderItem) {
                indexOfOrderItem = template.indexOf(notMatchingItem);
            } else {
                if (indexOfOrderItem == 0 && firstIndexOfOrderItem != 0) {
                    // insert before alphabetically first order item
                    template.add(firstIndexOfOrderItem, notMatchingItem);
                    firstIndexOfOrderItem++;
                } else if (firstIndexOfOrderItem == 0) {
                    // no order is specified
                    if (template.size() > 0 && (template.get(template.size() - 1).startsWith("static"))) {
                        // insert N after last static import
                        template.add(ImportsUtils.N);
                    }
                    template.add(notMatchingItem);
                } else {
                    // insert after the previous order item
                    template.add(indexOfOrderItem + 1, notMatchingItem);
                    indexOfOrderItem++;
                }
            }
        }
    }

    private boolean isOrderItem(String notMatchingItem, boolean staticItems) {
        boolean contains = allImportOrderItems.contains(notMatchingItem);
        return contains && matchesStatic(staticItems, notMatchingItem);
    }

    /**
     * gets first order item from sorted input list, and finds out it's index in
     * template.
     *
     * @param notMatching not matching
     * @param template import packages template
     * @param staticItems static import package
     * @return finds out index
     */
    private int getFirstIndexOfOrderItem(List<String> notMatching, List<String> template, boolean staticItems) {
        int firstIndexOfOrderItem = 0;
        for (String notMatchingItem : notMatching) {
            if (!matchesStatic(staticItems, notMatchingItem)) {
                continue;
            }
            boolean isOrderItem = isOrderItem(notMatchingItem, staticItems);
            if (isOrderItem) {
                firstIndexOfOrderItem = template.indexOf(notMatchingItem);
                break;
            }
        }
        return firstIndexOfOrderItem;
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
                matchingItems.sort(comparator);

                // replace order item by matching import statements
                // this is a mess and it is only a luck that it works :-]
                template.remove(i);
                if (i != 0 && !template.get(i - 1).equals(ImportsUtils.N)) {
                    template.add(i, ImportsUtils.N);
                    i++;
                }
                if (i < template.size() && !template.get(i).equals(ImportsUtils.N)
                        && !template.get(i).equals(ImportsUtils.N)) {
                    template.add(i, ImportsUtils.N);
                }
                template.addAll(i, matchingItems);
            }
        }
        // if there is \n on the end, remove it
        if (template.size() > 0 && template.get(template.size() - 1).equals(ImportsUtils.N)) {
            template.remove(template.size() - 1);
        }
    }
}
