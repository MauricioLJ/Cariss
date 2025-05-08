package com.mauledji.app.cariss.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegisterRequest {
    
    private String username;
    private String userFullName;
    private String userEmail;
    private String userPassword;

}
