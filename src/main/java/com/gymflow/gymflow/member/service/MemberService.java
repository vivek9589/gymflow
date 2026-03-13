package com.gymflow.gymflow.member.service;

import com.gymflow.gymflow.member.dto.request.MemberJoinRequest;
import com.gymflow.gymflow.member.dto.request.MemberUpdateRequest;
import com.gymflow.gymflow.member.dto.response.MemberResponse;
import com.gymflow.gymflow.member.entity.Member;
import org.springframework.data.domain.Page;

import java.util.List;

public interface MemberService {

    Member registerMember(MemberJoinRequest request);
    void renewSubscription(Long memberId, Long planId);
    Page<Member> getAllMembersByGym(Long gymId, int page, int size, String status, String search, String planName);

    void deleteMember(Long memberId);
    MemberResponse updateMember(Long id, MemberUpdateRequest request);

    Member getMemberById(Long id);

    List<Member> searchMembers(Long gymId, String query);
}