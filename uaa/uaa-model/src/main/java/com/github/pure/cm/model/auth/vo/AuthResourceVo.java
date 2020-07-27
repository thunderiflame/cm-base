package com.github.pure.cm.model.auth.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author 陈欢
 * @since 2020/7/6
 */
@Data
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel("权限资源")
public class AuthResourceVo {

    /**
     * 菜单项url，只用于展示
     */
    @ApiModelProperty("菜单项url，只用于展示")
    private String url;

    /**
     * 资源ID
     */
    @ApiModelProperty("资源ID")
    @NotBlank(message = "资源ID不能为空")
    private String resourceId;
    /**
     * 资源名称
     */
    @ApiModelProperty("资源名称")
    @NotBlank(message = "资源名称不能为空")
    private String name;

    /**
     * 父级ID
     */
    @ApiModelProperty("资源父级ID")
    @NotBlank(message = "资源父级ID不能为空")
    private String parentId;

    /**
     * 资源权限码
     */
    @ApiModelProperty(value = "资源权限码", example = "ROLE_ADMIN;_auth_server_test")
    @NotBlank(message = "资源权限码不能为空")
    private String authCode;
    /**
     * 服务名称
     */
    @ApiModelProperty(value = "服务名称", example = "uaa-server")
    private String appName;
    /**
     * 服务编码
     */
    @ApiModelProperty(value = "服务编码", example = "uaa-server")
    private String appCode;
    /**
     * 角色
     */
    @ApiModelProperty("角色")
    private List<AuthRoleVo> authRoleVos;

}
