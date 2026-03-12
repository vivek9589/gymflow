package com.gymflow.gymflow.gym.service;



import com.gymflow.gymflow.gym.dto.request.GymRequest;
import com.gymflow.gymflow.gym.dto.response.GymResponse;

import java.util.List;

public interface GymService {
    GymResponse getGymById(Long id);
    GymResponse getGymByCode(String code);
    GymResponse updateGym(Long id, GymRequest gymRequest);
    List<GymResponse> getAllGyms();
}