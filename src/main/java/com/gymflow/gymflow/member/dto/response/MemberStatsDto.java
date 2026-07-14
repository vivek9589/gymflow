package com.gymflow.gymflow.member.dto.response;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberStatsDto {

    private long totalMembers;

    private long activeMembers;

    private long expiredMembers;

}