package no.nav.veilarbveileder.service;

import no.nav.common.audit_log.cef.CefMessage;
import no.nav.common.audit_log.cef.CefMessageEvent;
import no.nav.common.audit_log.cef.CefMessageSeverity;
import no.nav.common.audit_log.log.AuditLogger;
import no.nav.common.audit_log.log.AuditLoggerImpl;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {
    private final AuditLogger auditLogger = new AuditLoggerImpl();

    public CefMessage cefMessage = CefMessage.builder()
            .applicationName("veilarbveileder")
            .event(CefMessageEvent.ACCESS)
            .name("Sporingslogg")
            .severity(CefMessageSeverity.INFO)
            .sourceUserId("Z12345")
            .destinationUserId("12345678900")
            .timeEnded(System.currentTimeMillis())
            .extension("msg", "NAV-ansatt har gjort oppslag på bruker")
            .build(); {
        auditLogger.log(cefMessage);
    }
}
