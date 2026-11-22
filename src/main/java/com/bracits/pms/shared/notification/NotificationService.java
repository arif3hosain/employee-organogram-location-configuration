package com.bracits.pms.shared.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * POC stand-in for the product-owned notification module (guide §7.9). It only logs.
 * Called from {@code @TransactionalEventListener(phase = AFTER_COMMIT)} listeners, so mail is sent
 * only after the data change has committed. Deliberately NOT @Transactional.
 */
@Service
@Slf4j
public class NotificationService {

    public void send(String to, String subject, String body) {
        log.info("[MAIL] to={} subject='{}' body='{}'", to, subject, body);
    }
}
