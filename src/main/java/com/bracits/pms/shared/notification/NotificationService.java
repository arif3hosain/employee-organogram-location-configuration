package com.bracits.pms.shared.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * POC stand-in for the product-owned notification module (guide §7.9).
 * It only logs. Called from postCondition, i.e. after the transaction has committed.
 * Deliberately NOT @Transactional.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public void send(String to, String subject, String body) {
        log.info("[MAIL] to={} subject='{}' body='{}'", to, subject, body);
    }
}
