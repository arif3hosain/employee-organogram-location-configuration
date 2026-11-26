package com.bracits.pms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tenant isolation (V6). Tenant 1 (BRAC) owns the seed data; tenant 2 (DEMO) starts empty.
 * Seed ids: grade G1=1, designation CEO=1, employee E0001=1, org unit ORG=1, country Bangladesh=1, post ids from 1.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TenancyIntegrationTest {

    private static final String H = "X-Tenant-Id";

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void secondTenant() {
        jdbc.update("insert into tenant (code, name) values ('DEMO', 'Demo customer')");       // id 2
        jdbc.update("insert into tenant (code, name, status) values ('OLD', 'Old customer', 'INACTIVE')"); // id 3
    }

    @Test
    void noHeaderMeansDefaultTenant() throws Exception {
        mvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(8));
    }

    @Test
    void secondTenantSeesNoneOfTheFirstTenantsData() throws Exception {
        mvc.perform(get("/api/employees").header(H, "2")).andExpect(jsonPath("$.data.totalElements").value(0));
        mvc.perform(get("/api/grades").header(H, "2")).andExpect(jsonPath("$.data", hasSize(0)));
        mvc.perform(get("/api/designations").header(H, "2")).andExpect(jsonPath("$.data", hasSize(0)));
        mvc.perform(get("/api/locations/tree").header(H, "2")).andExpect(jsonPath("$.data", hasSize(0)));
        mvc.perform(get("/api/org-units/tree").header(H, "2")).andExpect(jsonPath("$.data", hasSize(0)));

        // looking up a tenant-1 row by id from tenant 2 is a plain 404, not a leak
        mvc.perform(get("/api/employees/1").header(H, "2")).andExpect(status().isNotFound());
        mvc.perform(get("/api/org-units/1").header(H, "2")).andExpect(status().isNotFound());
        mvc.perform(get("/api/org-units/1/posts").header(H, "2")).andExpect(status().isNotFound());
    }

    @Test
    void sharedReferenceDataIsVisibleToEveryTenant() throws Exception {
        mvc.perform(get("/api/countries").header(H, "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].iso2").value("BD"));
    }

    @Test
    void codesAreUniquePerTenantOnly() throws Exception {
        // G1 / rank 1 already exist in tenant 1; tenant 2 may use them too
        mvc.perform(post("/api/grades").header(H, "2").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"G1","name":"Grade One","rankNo":1}
                                """))
                .andExpect(status().isCreated());
        // ...but not twice in the same tenant
        mvc.perform(post("/api/grades").header(H, "2").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"G1","name":"Again","rankNo":2}
                                """))
                .andExpect(status().isConflict());

        mvc.perform(get("/api/grades").header(H, "2")).andExpect(jsonPath("$.data", hasSize(1)));
        mvc.perform(get("/api/grades")).andExpect(jsonPath("$.data", hasSize(6)));
    }

    @Test
    void cannotReferenceAnotherTenantsRows() throws Exception {
        // grade 1 belongs to tenant 1, so tenant 2 cannot build a designation on it
        mvc.perform(post("/api/designations").header(H, "2").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"CEO","name":"Chief Executive","gradeId":1}
                                """))
                .andExpect(status().isNotFound());

        // a child row (sanctioned post) of tenant 1 cannot be deleted from tenant 2
        mvc.perform(delete("/api/org-units/posts/1").header(H, "2")).andExpect(status().isNotFound());
        mvc.perform(get("/api/org-units/1/posts")).andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    void databaseRejectsCrossTenantLinksEvenWithoutJava() {
        // composite FK (tenant_id, designation_id) -> designation (tenant_id, id): designation 1 is tenant 1's
        assertThatThrownBy(() -> jdbc.update("""
                insert into employee (tenant_id, employee_code, full_name, email, join_date, designation_id, org_unit_id, location_id)
                values (2, 'X1', 'Cross Link', 'x@demo.test', current_date, 1, 1, 7)
                """)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void badOrUnknownTenantIsRejected() throws Exception {
        mvc.perform(get("/api/employees").header(H, "abc")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/employees").header(H, "99")).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
        mvc.perform(get("/api/employees").header(H, "3")).andExpect(status().isForbidden());   // inactive
    }
}
