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

    public void auditLogWithMessageAndDestinationUserId(String logMessage, String destinationUserId, String innloggetBrukerToken) {
        auditLogger.log(CefMessage.builder()
                .timeEnded(System.currentTimeMillis())
                .applicationName("veilarbveileder")
                .sourceUserId(innloggetBrukerToken)
                .event(CefMessageEvent.ACCESS)
                .severity(CefMessageSeverity.INFO)
                .name("veilarbveileder-audit-log")
                .destinationUserId(destinationUserId)
                .extension("msg", logMessage)
                .build());
    }
}
