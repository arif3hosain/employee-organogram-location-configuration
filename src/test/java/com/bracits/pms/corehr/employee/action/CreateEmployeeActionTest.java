package com.bracits.pms.corehr.employee.action;

import com.bracits.pms.corehr.employee.api.EmployeeForm;
import com.bracits.pms.corehr.employee.api.EmployeeView;
import com.bracits.pms.corehr.employee.service.EmployeeService;
import com.bracits.pms.corehr.location.service.LocationService;
import com.bracits.pms.corehr.organogram.service.DesignationService;
import com.bracits.pms.corehr.organogram.service.OrgUnitService;
import com.bracits.pms.shared.action.Result;
import com.bracits.pms.shared.domain.RecordStatus;
import com.bracits.pms.shared.notification.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Action unit test as described in guide §3.4 "Testing each layer":
 * invalid input -> failure and the service is never called; valid input -> one service call + mail.
 * Lives in the action package so it can build the package-private EmployeeFormRules.
 */
class CreateEmployeeActionTest {

    private EmployeeService service;
    private NotificationService notifications;
    private CreateEmployeeAction action;

    @BeforeEach
    void setUp() {
        service = mock(EmployeeService.class);
        notifications = mock(NotificationService.class);
        DesignationService designations = mock(DesignationService.class);
        OrgUnitService orgUnits = mock(OrgUnitService.class);
        LocationService locations = mock(LocationService.class);
        when(designations.exists(anyLong())).thenReturn(true);
        when(orgUnits.exists(anyLong())).thenReturn(true);
        when(locations.exists(anyLong())).thenReturn(true);
        action = new CreateEmployeeAction(service, new EmployeeFormRules(designations, orgUnits, locations), notifications);
    }

    private static EmployeeForm form(String email) {
        return new EmployeeForm("E9001", "Test Person", email, "+8801711111111", LocalDate.of(2024, 1, 1),
                1L, 2L, 3L, null, null, null);
    }

    @Test
    void invalidEmailFailsAndServiceIsNeverCalled() {
        Result<EmployeeView> result = action.run(form("not-an-email"));

        assertThat(result.success()).isFalse();
        assertThat(result.message()).isEqualTo("Email is not valid");
        verify(service, never()).create(any());
        verify(notifications, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    void duplicateCodeFailsBeforeExecute() {
        when(service.codeExists(eq("E9001"), any())).thenReturn(true);

        Result<EmployeeView> result = action.run(form("test@example.com"));

        assertThat(result.success()).isFalse();
        verify(service, never()).create(any());
    }

    @Test
    void validInputCallsServiceOnceThenSendsMail() {
        var saved = new EmployeeView(10L, "E9001", "Test Person", "test@example.com", null, LocalDate.of(2024, 1, 1),
                1L, "Officer", 6L, "G6", "Grade 6 - Staff", 2L, "ERP Unit", 3L, "Head Office", null, null, RecordStatus.ACTIVE, 0L);
        when(service.create(any())).thenReturn(saved);

        Result<EmployeeView> result = action.run(form("test@example.com"));

        assertThat(result.success()).isTrue();
        assertThat(result.data().id()).isEqualTo(10L);
        verify(service, times(1)).create(any());
        verify(notifications, times(1)).send(eq("test@example.com"), anyString(), anyString());
    }
}
