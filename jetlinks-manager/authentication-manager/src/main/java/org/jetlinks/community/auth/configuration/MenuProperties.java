/*
 * Copyright 2025 JetLinks https://www.jetlinks.cn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetlinks.community.auth.configuration;

import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.DefaultDimensionType;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@ConfigurationProperties(prefix = "menu")
public class MenuProperties {

    //默认只有角色和可以绑定菜单
    private Set<String> dimensions = Sets.newHashSet(
        DefaultDimensionType.role.getId()
    );

    private Set<String> allowAllMenusUsers = new HashSet<>(Collections.singletonList("admin"));
    private String allowPermission = "menu";

    public boolean isAllowAllMenu(Authentication auth) {
        return allowAllMenusUsers.contains(auth.getUser().getUsername());
//            || auth.hasPermission(allowPermission);
    }
}
