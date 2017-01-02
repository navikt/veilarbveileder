package no.nav.fo.internal;

import no.nav.sbl.dialogarena.common.web.selftest.SelfTestBaseServlet;
import no.nav.sbl.dialogarena.types.Pingable;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.util.Collection;

public class SelftestServlet extends SelfTestBaseServlet {

    @Override
    protected String getApplicationName() {
        return "veilarbportefolje";
    }

    @Override
    protected Collection<? extends Pingable> getPingables() {
        return WebApplicationContextUtils.getWebApplicationContext(getServletContext()).getBeansOfType(Pingable.class).values();
    }
}