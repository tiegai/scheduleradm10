package com.nike.ncp.scheduler.core.model;

import org.springframework.util.StringUtils;

public class XxlJobUser {
    private int id;
    private String username;        // 账号
    private String password;        // 密码
    private int role;                // 角色：0-普通用户、1-管理员
    private String permission;    // 权限：执行器ID列表，多个逗号分割

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    private static final int ROLE_V_ONE = 1;

    // plugin
    public boolean validPermission(int jobGroup) {
        if (this.role == ROLE_V_ONE) {
            return true;
        } else {
            if (StringUtils.hasText(this.permission)) {
                for (String permissionItem : this.permission.split(",")) {
                    if (String.valueOf(jobGroup).equals(permissionItem)) {
                        return true;
                    }
                }
            }
            return false;
        }

    }

}
