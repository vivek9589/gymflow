package com.gymflow.gymflow.member.service;

import com.gymflow.gymflow.member.dto.request.MemberJoinRequest;
import com.gymflow.gymflow.member.dto.request.MemberUpdateRequest;
import com.gymflow.gymflow.member.dto.response.MemberResponse;
import com.gymflow.gymflow.member.entity.Member;

import java.util.List;

public interface MemberService {

    Member registerMember(MemberJoinRequest request);
    void renewSubscription(Long memberId, Long planId);
    List<Member> getAllMembersByGym(Long gymId);

    void deleteMember(Long memberId);
    MemberResponse updateMember(Long id, MemberUpdateRequest request);

    Member getMemberById(Long id);

    List<Member> searchMembers(Long gymId, String query);
}