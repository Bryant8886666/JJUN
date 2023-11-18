package com.two.model.request;

import lombok.Data;

import java.io.Serializable;


@Data
public class UserLoginRequest implements Serializable {
    private static final long serialVersionUID = 7299283250557764255L;
    private String userAccount;
    private String userPassword;
}




//@Data
//public class UserLoginRequest implements Serializable {
//
//    private static final long serialVersionUID = 2284125278983051596L;
//
////    private static final long serialVersionUID = 0;
//
//    String userPassword;
//    String userAccount;
//    String avatarUrl;
//    String gender;
//    String phone;
//    String email;
//    int useRole;
//
//
//}
