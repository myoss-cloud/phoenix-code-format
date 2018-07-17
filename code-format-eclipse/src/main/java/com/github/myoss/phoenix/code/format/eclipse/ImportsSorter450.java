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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;

/**
 * Java import代码格式化工具 https://github.com/krasa/EclipseCodeFormatter
 *
 * @author Jerry.Chen
 * @since 2018年7月17日 下午11:11:06
 */
public class ImportsSorter450 {
    private List<String>                      template            = new ArrayList<>();
    private ArrayListMultimap<String, String> matchingImports     = ArrayListMultimap.create();
    private ArrayList<String>                 notMatching         = new ArrayList<>();
    private Set<String>                       allImportOrderItems = new HashSet<>();
    private Comparator<String>                comparator;

    /**
     * Java import代码格式化工具
     *
     * @param importOrder 排序规则
     */
    public ImportsSorter450(List<String> importOrder) {
        List<String> importOrderCopy = new ArrayList<>(importOrder);
        normalizeStaticOrderItems(importOrderCopy);
        putStaticItemIfNotExists(importOrderCopy);
        template.addAll(importOrderCopy);
        this.allImportOrderItems.addAll(importOrderCopy);
        comparator = (o1, o2) -> {
            String containerName1 = (allImportOrderItems.contains(o1) ? o1 : getPackage(o1));
            String simpleName1 = (allImportOrderItems.contains(o1) ? "" : getSimpleName(o1));

            String containerName2 = (allImportOrderItems.contains(o2) ? o2 : getPackage(o2));
            String simpleName2 = (allImportOrderItems.contains(o2) ? "" : getSimpleName(o2));
            int i = containerName1.compareTo(containerName2);

            if (i == 0) {
                i = simpleName1.compareTo(simpleName2);
            }
            return i;
        };
    }

    /**
     * 将 import package 进行排序
     *
     * @param imports import packages
     * @return 排序之后的结果
     */
    public String sort(List<String> imports) {
        filterMatchingImports(imports);
        mergeNotMatchingItems(false);
        mergeNotMatchingItems(true);
        mergeMatchingItems();
        return getResult();
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
     */
    private void filterMatchingImports(List<String> imports) {
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
                    matchingImport = betterMatching(matchingImport, orderItem, anImport);
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
     */
    private void mergeNotMatchingItems(boolean staticItems) {
        notMatching.sort(comparator);

        int firstIndexOfOrderItem = getFirstIndexOfOrderItem(notMatching, staticItems);
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
                        template.add(JavaCodeFormatter.N);
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
     * @param staticItems static import package
     * @return finds out index
     */
    private int getFirstIndexOfOrderItem(List<String> notMatching, boolean staticItems) {
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

    private void mergeMatchingItems() {
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
                ArrayList<String> matchingItems = new ArrayList<String>(strings);
                matchingItems.sort(comparator);

                // replace order item by matching import statements
                // this is a mess and it is only a luck that it works :-]
                template.remove(i);
                if (i != 0 && !template.get(i - 1).equals(JavaCodeFormatter.N)) {
                    template.add(i, JavaCodeFormatter.N);
                    i++;
                }
                if (i < template.size() && !template.get(i).equals(JavaCodeFormatter.N)
                        && !template.get(i).equals(JavaCodeFormatter.N)) {
                    template.add(i, JavaCodeFormatter.N);
                }
                template.addAll(i, matchingItems);
            }
        }
        // if there is \n on the end, remove it
        if (template.size() > 0 && template.get(template.size() - 1).equals(JavaCodeFormatter.N)) {
            template.remove(template.size() - 1);
        }
    }

    private String getResult() {
        StringBuilder strings = new StringBuilder();

        for (String s : template) {
            if (s.equals(JavaCodeFormatter.N)) {
                strings.append(s);
            } else {
                strings.append("import ").append(s).append(";").append(JavaCodeFormatter.N);
            }
        }
        return strings.deleteCharAt(strings.length() - 1).toString();
    }

    private String getSimpleName(String s) {
        int lastDot = s.lastIndexOf(".");
        if (lastDot == -1) {
            return s;
        }
        return s.substring(lastDot + 1, s.length());
    }

    private String getPackage(String s) {
        int lastDot = s.lastIndexOf(".");
        if (lastDot == -1) {
            return "";
        }
        return s.substring(0, lastDot);
    }

    private String betterMatching(String order1, String order2, String anImport) {
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
}
