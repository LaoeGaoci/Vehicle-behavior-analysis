package com.cms.backend.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class User {

    @TableField("username")
    private String username;

    @TableField("age")
    private Integer age;

    @TableField("driver_years")
    private Integer driver_years;

    @TableField("gender")
    private String gender;

}
