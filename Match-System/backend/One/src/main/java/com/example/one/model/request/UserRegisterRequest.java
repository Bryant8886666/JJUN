package com.example.one.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = -1220246299508899122L;
    String userAccount;
    String userPassword;
    String checkPassword;
}
