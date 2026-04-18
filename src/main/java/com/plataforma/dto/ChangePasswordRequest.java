// src/main/java/com/plataforma/dto/ChangePasswordRequest.java
package com.plataforma.dto;

import lombok.Data;

@Data
public class ChangePasswordRequest
{
    private String oldPassword;
    private String newPassword;
}
