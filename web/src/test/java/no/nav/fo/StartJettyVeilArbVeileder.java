package no.nav.fo;

import no.nav.brukerdialog.security.context.JettySubjectHandler;
import no.nav.sbl.dialogarena.common.jetty.Jetty;
import no.nav.sbl.dialogarena.test.SystemProperties;
import org.apache.geronimo.components.jaspi.AuthConfigFactoryImpl;

import javax.security.auth.message.config.AuthConfigFactory;
import java.security.Security;

import static java.lang.System.setProperty;
import static no.nav.modig.lang.collections.FactoryUtils.gotKeypress;
import static no.nav.modig.lang.collections.RunnableUtils.first;
import static no.nav.modig.lang.collections.RunnableUtils.waitFor;

public class StartJettyVeilArbVeileder {

    public static void main(String[] args) {
        SystemProperties.setFrom("jetty-veilarbveileder.properties");
        System.setProperty("develop-local", "true");
        setProperty("no.nav.modig.core.context.subjectHandlerImplementationClass", JettySubjectHandler.class.getName());
        System.setProperty("org.apache.geronimo.jaspic.configurationFile", "src/test/resources/jaspiconf.xml");
        Security.setProperty(AuthConfigFactory.DEFAULT_FACTORY_SECURITY_PROPERTY, AuthConfigFactoryImpl.class.getCanonicalName());

        //Må ha https for csrf-token
        final Jetty jetty = Jetty.usingWar()
                .at("veilarbveileder")
                .sslPort(9590)
                .port(9591)
                .overrideWebXml()
                .configureForJaspic()
                .buildJetty();
        jetty.startAnd(first(waitFor(gotKeypress())).then(jetty.stop));
    }

}