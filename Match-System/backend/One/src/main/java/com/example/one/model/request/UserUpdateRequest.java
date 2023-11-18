package com.example.one.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserUpdateRequest implements Serializable {
    private Long id;
    private String userName;
    private String userAccount;
    private String avataUrl;
    private String gender;
    private String phone;
    private String email;
    private Integer userRole;

}
