package com.bracits.pms;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;

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
 * End-to-end through Controller -> Service -> DB on the seed data (V2 migration).
 * Seed ids follow insert order: locations B-HO=7, B-MIR10=8, B-AGR=9, divisions 10-17 (Barishal=10, Chattogram=11,
 * Dhaka=12, ...), districts and upazilas after them. V5 moved the country into the country table (Bangladesh=1) and
 * removed the old "Bangladesh" location (id 1), so the divisions are the roots.
 * Location type ids after V5: BRANCH=4 (level 4), DIVISION=5 (1), DISTRICT=6 (2), UPAZILA=7 (3), all in Bangladesh;
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
        // V5: country table (Bangladesh) > 8 divisions (roots) > 64 districts > 495 upazilas (BBS geocodes as codes)
        mvc.perform(get("/api/locations/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(8)))
                .andExpect(jsonPath("$.data[0].typeCode").value("DIVISION"))
                .andExpect(jsonPath("$.data[0].countryCode").value("BD"));

        mvc.perform(get("/api/locations").param("typeId", "6"))          // every DISTRICT
                .andExpect(jsonPath("$.data", hasSize(64)));
        mvc.perform(get("/api/locations").param("typeId", "7"))          // every UPAZILA
                .andExpect(jsonPath("$.data", hasSize(495)));
        mvc.perform(get("/api/locations").param("typeId", "4"))          // every BRANCH / OFFICE
                .andExpect(jsonPath("$.data", hasSize(3)));

        mvc.perform(get("/api/locations/7"))                              // Head Office sits under Dhaka district
                .andExpect(jsonPath("$.data.parentName").value("Dhaka"))
                .andExpect(jsonPath("$.data.levelNo").value(4))
                .andExpect(jsonPath("$.data.path").value(startsWith("/12/")))
                .andExpect(jsonPath("$.data.countryName").value("Bangladesh"));
    }

    @Test
    void locationLevelRuleIsEnforced() throws Exception {
        // a BRANCH (level 4) directly under a DIVISION (level 1) is allowed: levels may be skipped
        mvc.perform(post("/api/locations").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"B-REMOTE","name":"Remote Office","typeId":4,"parentId":12}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.path", startsWith("/12/")));

        // a DIVISION (level 1) is a root: it cannot sit under a BRANCH
        mvc.perform(post("/api/locations").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"DIV-BAD","name":"Bad Division","typeId":5,"parentId":7}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void countriesDefineTheirOwnLevels() throws Exception {
        mvc.perform(get("/api/countries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].iso2").value("BD"))
                .andExpect(jsonPath("$.data[0].currencyCode").value("BDT"));

        // Bean Validation on the body: ISO2 must be 2 letters
        mvc.perform(post("/api/countries").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"iso2":"IND","iso3":"IND","name":"India"}
                                """))
                .andExpect(status().isBadRequest());

        // India (id 2) gets its own STATE and DISTRICT types; DISTRICT may repeat across countries
        mvc.perform(post("/api/countries").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"iso2":"IN","iso3":"IND","name":"India","currencyCode":"INR","phoneCode":"+91"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(2));
        mvc.perform(post("/api/location-types").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"countryId":2,"code":"STATE","name":"State","levelNo":1}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.countryCode").value("IN"));
        String created = mvc.perform(post("/api/location-types").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"countryId":2,"code":"DISTRICT","name":"District","levelNo":2}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Number indianDistrictId = JsonPath.read(created, "$.data.id");
        // but not twice in the same country
        mvc.perform(post("/api/location-types").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"countryId":1,"code":"DISTRICT","name":"District again","levelNo":2}
                                """))
                .andExpect(status().isConflict());

        mvc.perform(get("/api/location-types").param("countryId", "2"))
                .andExpect(jsonPath("$.data", hasSize(2)));

        // an Indian DISTRICT (the type just created) cannot sit under Dhaka division (Bangladesh)
        mvc.perform(post("/api/locations").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"IN-X\",\"name\":\"Wrong place\",\"typeId\":" + indianDistrictId + ",\"parentId\":12}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("Bangladesh")));

        // a country that has location types cannot be deleted
        mvc.perform(delete("/api/countries/1")).andExpect(status().isConflict());
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

        // a designation without a grade fails Bean Validation -> 400
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
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Another grade already has rank 3"));

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

        // Bean Validation failure -> 400, nothing saved
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

        // duplicate code -> 409 from the service
        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"employeeCode":"E0100","fullName":"Dup","email":"dup@example.com",
                                 "joinDate":"2025-01-01","designationId":6,"orgUnitId":8,"locationId":8}
                                """))
                .andExpect(status().isConflict())
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
                .andExpect(status().isConflict());
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
