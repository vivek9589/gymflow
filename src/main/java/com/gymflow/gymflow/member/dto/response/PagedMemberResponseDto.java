package com.gymflow.gymflow.member.dto.response;


import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagedMemberResponseDto {

    private List<MemberListResponseDto> members;

    private int page;

    private int size;

    private int totalPages;

    private long totalMembers;

    private MemberStatsDto stats;

}