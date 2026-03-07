package com.gymflow.gymflow.auth.service;


import com.gymflow.gymflow.auth.dto.request.LoginRequest;
import com.gymflow.gymflow.auth.dto.request.OwnerRegisterRequest;
import com.gymflow.gymflow.auth.dto.response.LoginResponse;

public interface AuthService {


    LoginResponse login(LoginRequest request);
    void register(OwnerRegisterRequest request);
}