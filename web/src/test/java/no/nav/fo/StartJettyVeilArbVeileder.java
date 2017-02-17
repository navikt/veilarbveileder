package no.nav.fo;

import no.nav.fo.security.jwt.context.JettySubjectHandler;
import no.nav.sbl.dialogarena.common.jetty.Jetty;
import no.nav.sbl.dialogarena.test.SystemProperties;
import org.eclipse.jetty.jaas.JAASLoginService;

import static java.lang.System.setProperty;
import static no.nav.modig.lang.collections.FactoryUtils.gotKeypress;
import static no.nav.modig.lang.collections.RunnableUtils.first;
import static no.nav.modig.lang.collections.RunnableUtils.waitFor;
import static no.nav.modig.testcertificates.TestCertificates.setupKeyAndTrustStore;



public class StartJettyVeilArbVeileder {

    public static void main(String[] args) {
        SystemProperties.setFrom("jetty-veilarbveileder.properties");
        setupKeyAndTrustStore();
        setProperty("no.nav.modig.core.context.subjectHandlerImplementationClass", JettySubjectHandler.class.getName());

        JAASLoginService jaasLoginService = new JAASLoginService("JWT Realm");
        jaasLoginService.setLoginModuleName("jwtLogin");

        //Må ha https for csrf-token
        final Jetty jetty = Jetty.usingWar()
                .at("veilarbveileder")
                .sslPort(9590)
                .port(9591)
                .overrideWebXml()
                .withLoginService(jaasLoginService)
                .buildJetty();
        jetty.startAnd(first(waitFor(gotKeypress())).then(jetty.stop));
    }



}
