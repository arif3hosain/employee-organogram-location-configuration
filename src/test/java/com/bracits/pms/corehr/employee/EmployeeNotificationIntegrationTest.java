package com.bracits.pms.corehr.employee;

import com.bracits.pms.shared.notification.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Mail goes out through @TransactionalEventListener(AFTER_COMMIT): only when the change committed. */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class EmployeeNotificationIntegrationTest {

    private static final String NEW_EMPLOYEE = """
            {"employeeCode":"E0100","fullName":"New Person","email":"new.person@example.com",
             "joinDate":"2025-01-01","designationId":6,"orgUnitId":8,"locationId":8,"supervisorId":7}
            """;

    @Autowired
    MockMvc mvc;

    @MockitoBean
    NotificationService notificationService;

    @Test
    void createSendsWelcomeAndSupervisorMailAfterCommit() throws Exception {
        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(NEW_EMPLOYEE))
                .andExpect(status().isCreated());

        verify(notificationService).send(eq("new.person@example.com"), anyString(), anyString());
        verify(notificationService).send(eq("supervisor:7"), eq("New team member"), anyString());
    }

    @Test
    void invalidInputSendsNoMail() throws Exception {
        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON)
                        .content(NEW_EMPLOYEE.replace("new.person@example.com", "bad-email")))
                .andExpect(status().isBadRequest());

        verify(notificationService, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    void rolledBackCreateSendsNoMail() throws Exception {
        // E0001 already exists -> BusinessException inside the transaction -> rollback, no event delivered
        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON)
                        .content(NEW_EMPLOYEE.replace("E0100", "E0001")))
                .andExpect(status().isConflict());

        verify(notificationService, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    void deleteSendsMailToHrAfterCommit() throws Exception {
        mvc.perform(delete("/api/employees/8")).andExpect(status().isOk());
        verify(notificationService).send(eq("hr-admin"), eq("Employee removed"), anyString());
    }

    @Test
    void updateRequiresVersion() throws Exception {
        mvc.perform(put("/api/employees/8").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"employeeCode":"E0005","fullName":"Farhana Akter","email":"farhana.akter@example.com",
                                 "joinDate":"2022-01-10","designationId":5,"orgUnitId":8,"locationId":8,"supervisorId":7}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Version is required for update (send the version you loaded)"))
                .andExpect(jsonPath("$.data.version").exists());
    }
}
