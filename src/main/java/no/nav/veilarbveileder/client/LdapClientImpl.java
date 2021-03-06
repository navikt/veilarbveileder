package no.nav.veilarbveileder.client;

import no.nav.common.health.HealthCheckResult;
import no.nav.common.types.identer.NavIdent;
import no.nav.veilarbveileder.config.CacheConfig;
import org.springframework.cache.annotation.Cacheable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static no.nav.common.utils.EnvironmentUtils.getRequiredProperty;

public class LdapClientImpl implements LdapClient {

    private static final String LDAP_BASEDN_PROPERTY_NAME = "LDAP_BASEDN";
    private static final String LDAP_URL = "LDAP_URL";
    private static final String LDAP_USERNAME = "LDAP_USERNAME";
    private static final String LDAP_PASSWORD = "LDAP_PASSWORD";

    @Cacheable(CacheConfig.VEILEDER_ROLLE_CACHE_NAME)
    public boolean veilederHarRolle(NavIdent ident, String rolle) {
        NamingEnumeration<SearchResult> result = sokLDAP(ident);
        return getRoller(result).contains(rolle);
    }

    @Override
    public HealthCheckResult checkHealth() {
        if(veilederHarRolle(NavIdent.of("dummy"), "dummy")){
            return HealthCheckResult.unhealthy("Veileder skal ikke ha dummy rolle");
        }

        return HealthCheckResult.healthy();
    }

    private NamingEnumeration<SearchResult> sokLDAP(NavIdent navIdent) {
        String searchbase = "OU=Users,OU=NAV,OU=BusinessUnits," + getRequiredProperty(LDAP_BASEDN_PROPERTY_NAME);
        SearchControls searchCtrl = new SearchControls();
        searchCtrl.setSearchScope(SearchControls.SUBTREE_SCOPE);

        try {
            return createLdapContext().search(searchbase, String.format("(&(objectClass=user)(CN=%s))", navIdent.get()), searchCtrl);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> getRoller(NamingEnumeration<SearchResult> result) {
        try {
            if (result.hasMore()) {
                SearchResult searchResult = result.next();
                NamingEnumeration<?> attributes = searchResult.getAttributes().get("memberof").getAll();
                List<String> rawRoles = getRawRoles(attributes);
                return extractCommonName(rawRoles);
            } else {
                return emptyList();
            }
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> getRawRoles(NamingEnumeration<?> attributes) throws NamingException {
        List<String> rawRoles = new ArrayList<>();
        while (attributes.hasMore()) {
            rawRoles.add((String) attributes.next());
        }
        return rawRoles;
    }

    private static LdapContext createLdapContext() {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, getRequiredProperty(LDAP_URL));
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, getRequiredProperty(LDAP_USERNAME));
        env.put(Context.SECURITY_CREDENTIALS, getRequiredProperty(LDAP_PASSWORD));

        try {
            return new InitialLdapContext(env, null);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<String> extractCommonName(List<String> rawRoles) {
        return rawRoles
                .stream()
                .map(rolleStr -> {
                    if (!rolleStr.startsWith("CN=")) {
                        throw new IllegalArgumentException("Feil format på AD-rolle: " + rolleStr);
                    }
                    return rolleStr.split(",")[0].split("CN=")[1];
                })
                .collect(Collectors.toList());
    }
}
