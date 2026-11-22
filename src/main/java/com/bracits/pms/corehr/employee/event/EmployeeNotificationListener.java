package com.bracits.pms.corehr.employee.event;

import com.bracits.pms.corehr.employee.api.EmployeeView;
import com.bracits.pms.shared.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Sends employee mail only after the service transaction has committed
 * ({@code AFTER_COMMIT}): a rolled-back create or delete never sends anything.
 */
@Component
@RequiredArgsConstructor
public class EmployeeNotificationListener {

    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCreated(EmployeeCreatedEvent event) {
        EmployeeView e = event.employee();
        notificationService.send(e.email(), "Welcome to " + e.orgUnitName(),
                "Dear " + e.fullName() + ", your employee code is " + e.employeeCode() + ".");
        if (e.supervisorId() != null) {
            notificationService.send("supervisor:" + e.supervisorId(), "New team member",
                    e.fullName() + " (" + e.designationName() + ") now reports to you.");
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDeleted(EmployeeDeletedEvent event) {
        EmployeeView e = event.employee();
        notificationService.send("hr-admin", "Employee removed",
                e.employeeCode() + " " + e.fullName() + " was deleted.");
    }
}
