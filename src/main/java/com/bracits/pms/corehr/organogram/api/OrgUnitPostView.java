package com.bracits.pms.corehr.organogram.api;

/** A sanctioned post line with headcount: vacancy = sanctioned - filled (negative = over-staffed). */
public record OrgUnitPostView(Long id, Long orgUnitId, Long designationId, String designationCode,
                              String designationName, String gradeCode, Integer gradeRank,
                              int sanctioned, int filled, int vacancy) {
}
