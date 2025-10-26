package org.jetlinks.community.standalone.web;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.jetlinks.community.auth.enums.UserEntityType;

import javax.persistence.Column;
import javax.persistence.Table;

@Getter
@Setter
@Table(name = "s_user",schema = "public")
public class UserDetails {
    @Schema(description = "用户ID")
    @Column
    private String id;

    @Schema(description = "用户名")
    @Column
    private String username;

    @Schema(hidden = true)
    @Column
    private String password;

    @Schema(hidden = true)
    private String typeId;

    @Schema(hidden = true)
    private UserEntityType type;

    @Schema(description = "用户状态。1启用，0禁用")
    private Byte status;

    @Schema(description = "是否授权")
    private Boolean loggedIn;

    @Schema(description = "salt")
    private String salt;
}
