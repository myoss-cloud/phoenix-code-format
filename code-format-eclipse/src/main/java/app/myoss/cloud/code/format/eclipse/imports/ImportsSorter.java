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

package app.myoss.cloud.code.format.eclipse.imports;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Java import代码格式化工具
 *
 * @author Jerry.Chen
 * @since 2018年7月18日 下午1:37:35
 */
public interface ImportsSorter {
    /**
     * 获取默认排序规则
     *
     * @return 默认排序规则
     */
    default List<String> defaultOrder() {
        return Stream.of("java", "javax", "org", "com").collect(Collectors.toList());
    }

    /**
     * 将 import package 进行排序
     *
     * @param imports import packages
     * @return 排序之后的结果
     */
    String sort(List<String> imports);
}
