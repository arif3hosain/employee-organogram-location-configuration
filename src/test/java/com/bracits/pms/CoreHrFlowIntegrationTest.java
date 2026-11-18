package com.bracits.pms;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end through Controller -> Action -> Service -> DB on the seed data (V2 migration).
 * Seed ids follow insert order: locations BD=1, B-HO=7, B-MIR10=8, B-AGR=9 (V4 removed the sample regions/areas 2-6
 * and added divisions/districts/upazilas from id 10);
 * org units ORG=1, F-TECH=2, F-CS=3, D-SE=4, D-QA=5, D-HR=6, D-FIN=7, U-ERP=8;
 * designations CEO=1 ... OFF=6; grades G1=1 ... G6=6 (V3, CEO -> G1 ... OFF -> G6); employees E0001=1, E0002=2, E0006=3, E0003=4, E0007=5, E0008=6, E0004=7, E0005=8.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CoreHrFlowIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Test
    void locationTreeIsTypeWise() throws Exception {
        // V4: Bangladesh > 8 divisions > 64 districts > 495 upazilas (BBS geocodes as codes)
        mvc.perform(get("/api/locations/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].typeCode").value("COUNTRY"))
                .andExpect(jsonPath("$.data[0].children", hasSize(8)))
                .andExpect(jsonPath("$.data[0].children[0].typeCode").value("DIVISION"));

        mvc.perform(get("/api/locations").param("typeId", "6"))          // every DISTRICT
                .andExpect(jsonPath("$.data", hasSize(64)));
        mvc.perform(get("/api/locations").param("typeId", "7"))          // every UPAZILA
                .andExpect(jsonPath("$.data", hasSize(495)));
        mvc.perform(get("/api/locations").param("typeId", "4"))          // every BRANCH / OFFICE
                .andExpect(jsonPath("$.data", hasSize(3)));

        mvc.perform(get("/api/locations/7"))                              // Head Office now sits under Dhaka district
                .andExpect(jsonPath("$.data.parentName").value("Dhaka"))
                .andExpect(jsonPath("$.data.levelNo").value(5));
    }

    @Test
    void locationLevelRuleIsEnforced() throws Exception {
        // type ids after V4: COUNTRY=1, BRANCH=4 (level 5), DIVISION=5, DISTRICT=6, UPAZILA=7
        // a BRANCH directly under the COUNTRY is allowed: levels may be skipped
        mvc.perform(post("/api/locations").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"B-REMOTE","name":"Remote Office","typeId":4,"parentId":1}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.path", startsWith("/1/")));

        // a DIVISION (level 2) under a BRANCH (level 5) is not
        mvc.perform(post("/api/locations").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"DIV-BAD","name":"Bad Division","typeId":5,"parentId":7}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void organogramTreeAndNatureFilter() throws Exception {
        mvc.perform(get("/api/org-units/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].code").value("ORG"))
                .andExpect(jsonPath("$.data[0].children", hasSize(2)));

        mvc.perform(get("/api/org-units").param("nature", "NON_FUNCTIONAL"))
                .andExpect(jsonPath("$.data", hasSize(3)));                // F-CS, D-HR, D-FIN
    }

    @Test
    void designationsCarryTheirGrade() throws Exception {
        mvc.perform(get("/api/grades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(6)))
                .andExpect(jsonPath("$.data[0].code").value("G1"));

        mvc.perform(get("/api/designations"))                               // most senior grade first
                .andExpect(jsonPath("$.data[0].code").value("CEO"))
                .andExpect(jsonPath("$.data[0].gradeCode").value("G1"))
                .andExpect(jsonPath("$.data[5].gradeName").value("Grade 6 - Staff"));

        // a designation without a grade fails preCondition
        mvc.perform(post("/api/designations").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"INT","name":"Intern"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Grade is required"));

        mvc.perform(post("/api/designations").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"INT","name":"Intern","gradeId":6}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.gradeCode").value("G6"));

        // a grade used by designations cannot be deleted; a duplicate rank is rejected
        mvc.perform(delete("/api/grades/6")).andExpect(status().isConflict());
        mvc.perform(post("/api/grades").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"G9","name":"Dup rank","rankNo":3}
                                """))
                .andExpect(status().isBadRequest());

        // employees show the grade of their designation
        mvc.perform(get("/api/employees/8"))
                .andExpect(jsonPath("$.data.gradeCode").value("G5"));
    }

    @Test
    void sanctionedPostsShowVacancy() throws Exception {
        mvc.perform(get("/api/org-units/8/posts"))                          // U-ERP
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].designationCode").value("MGR"))
                .andExpect(jsonPath("$.data[0].sanctioned").value(1))
                .andExpect(jsonPath("$.data[0].filled").value(1))
                .andExpect(jsonPath("$.data[0].vacancy").value(0));
    }

    @Test
    void employeeCrudFlow() throws Exception {
        // search the whole Technology subtree (F-TECH=2): E0002, E0003, E0004, E0005
        mvc.perform(get("/api/employees").param("orgUnitId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(4));

        // preCondition failure -> 400, nothing saved
        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"employeeCode":"E0100","fullName":"New Person","email":"bad-email",
                                 "joinDate":"2025-01-01","designationId":6,"orgUnitId":8,"locationId":8}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email is not valid"));

        // create -> 201
        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"employeeCode":"E0100","fullName":"New Person","email":"new.person@example.com",
                                 "joinDate":"2025-01-01","designationId":6,"orgUnitId":8,"locationId":8,"supervisorId":7}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.employeeCode").value("E0100"))
                .andExpect(jsonPath("$.data.supervisorName").value("Tanvir Hasan"))
                .andExpect(jsonPath("$.data.version").value(0));

        // duplicate code -> 400 from preCondition
        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"employeeCode":"E0100","fullName":"Dup","email":"dup@example.com",
                                 "joinDate":"2025-01-01","designationId":6,"orgUnitId":8,"locationId":8}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    void reportingLoopIsRejectedInService() throws Exception {
        // E0002 (id 2) reports to E0001. Make E0001 (id 1) report to E0003 (id 4), who is under E0002 -> loop.
        mvc.perform(put("/api/employees/1").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"employeeCode":"E0001","fullName":"Rahim Uddin","email":"rahim.uddin@example.com",
                                 "joinDate":"2015-01-01","designationId":1,"orgUnitId":1,"locationId":7,
                                 "supervisorId":4,"version":0}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("loop")));
    }

    @Test
    void staleVersionIsRejected() throws Exception {
        mvc.perform(put("/api/employees/8").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"employeeCode":"E0005","fullName":"Farhana Akter","email":"farhana.akter@example.com",
                                 "joinDate":"2022-01-10","designationId":5,"orgUnitId":8,"locationId":8,
                                 "supervisorId":7,"version":99}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void cannotDeleteASupervisor() throws Exception {
        mvc.perform(delete("/api/employees/7"))                              // E0004 supervises E0005
                .andExpect(status().isBadRequest());
        mvc.perform(delete("/api/employees/8"))                              // E0005 supervises nobody
                .andExpect(status().isOk());
    }

    @Test
    void movingAnOrgUnitRebasesItsSubtree() throws Exception {
        // move D-SE (4, with child U-ERP 8) from Technology (2) to Corporate Services (3)
        mvc.perform(put("/api/org-units/4").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"D-SE","name":"Software Engineering","typeId":3,"nature":"FUNCTIONAL",
                                 "parentId":3,"locationId":7,"sortOrder":1,"version":0}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.path").value("/1/3/4/"));

        mvc.perform(get("/api/org-units/8"))
                .andExpect(jsonPath("$.data.path").value("/1/3/4/8/"));

        // moving it under its own child must fail
        mvc.perform(put("/api/org-units/4").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"D-SE","name":"Software Engineering","typeId":3,"parentId":8,"version":1}
                                """))
                .andExpect(status().isConflict());
    }
}
