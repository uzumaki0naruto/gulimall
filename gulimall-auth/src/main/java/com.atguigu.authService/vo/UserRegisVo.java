package com.atguigu.authService.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisVo {
    @NotEmpty(message = "用户名必须提交")
    @Length(min = 6, max = 18, message = "用户名必须是6-18位字符")
    private String userName;

    @NotEmpty(message = "密码必须填写")
    @Length(min = 6, max = 18, message = "密码必须是6-18位字符")
    private String password;

    /**
     * ①、[1] 代表开头是1
     * ②、([3-9]) 代表 3到9 的一个数字、一个手机号都是 15几、13几 等等、
     * ③、[0-9] 代表 0到9之间的数字
     * ④、{9} 代表9 是 [0-9] 的要有九位数字
     */

    @NotEmpty(message = "邮箱必须填写")
//    @Pattern(regexp = "^[1]([3-9])[0-9]{9}$", message = "手机号格式不正确")
    private String email;

    @NotEmpty(message = "验证码必须填写")
    private String code;
}
