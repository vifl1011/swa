package de.shop.auth.service.jboss;

import static de.shop.kundenverwaltung.domain.Kunde.FIND_LOGIN_BY_LOGIN_PREFIX;
import static de.shop.kundenverwaltung.domain.Kunde.PARAM_LOGIN_PREFIX;
import static de.shop.util.Constants.HASH_ALGORITHM;
import static de.shop.util.Constants.HASH_CHARSET;
import static de.shop.util.Constants.HASH_ENCODING;
import static de.shop.util.Constants.SECURITY_DOMAIN;
import static org.jboss.security.auth.spi.Util.createPasswordHash;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.security.SimpleGroup;

import com.google.common.base.Strings;

import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.kundenverwaltung.service.KundeService;
import de.shop.kundenverwaltung.service.KundeService.FetchType;
import de.shop.util.InternalError;
import de.shop.util.Log;


/**
 */
@ApplicationScoped
@Log
public class AuthService implements Serializable {
	private static final long serialVersionUID = -2736040689592627172L;
	
	private static final String LOCALHOST = "localhost";
	private static final int MANAGEMENT_PORT = 9999;

	public static final String ADMIN = "admin";
	public static final String MITARBEITER = "mitarbeiter";
	public static final String ABTEILUNGSLEITER = "abteilungsleiter";
	public static final String KUNDE = "kunde";
	public enum RolleType {
		ADMIN(0),
		MITARBEITER(1),
		ABTEILUNGSLEITER(2),
		KUNDE(3);
		
		private int value;
		
		RolleType(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

	
	@Inject
	private Logger logger;
	
	@PersistenceContext
	private transient EntityManager em;
	
	@Inject
	private KundeService ks;
	
	@PostConstruct
	private void postConstruct() {
		logger.debugf("CDI-faehiges Bean %s wurde erzeugt", this);
	}
	
	@PreDestroy
	private void preDestroy() {
		logger.debugf("CDI-faehiges Bean %s wurde geloescht", this);
	}


	/**
	 * In Anlehnung an org.jboss.test.PasswordHasher von Scott Stark
	 */
	public String verschluesseln(String password) {
		if (password == null) {
			return null;
		}
		
		// Alternativ:
		// org.jboss.crypto.CryptoUtil.createPasswordHash
		final String passwordHash = createPasswordHash(HASH_ALGORITHM, HASH_ENCODING, HASH_CHARSET,
				                                       null, password);
		return passwordHash;
	}
	
	/**
	 */
	public boolean validatePassword(Kunde kunde, String passwort) {
		if (kunde == null) {
			return false;
		}
		
		final String verschluesselt = verschluesseln(passwort);
		final boolean result = verschluesselt.equals(kunde.getPasswort());
		return result;
	}
	
	/**
	 */
	public void addRollen(Long kundeId, Collection<RolleType> rollen) {
		if (rollen == null || rollen.isEmpty()) {
			return;
		}

		ks.findKundeById(kundeId, FetchType.NUR_KUNDE, null)
		  .getRollen()
		  .addAll(rollen);
		flushSecurityCache(kundeId.toString());
	}

	/**
	 */
	public void removeRollen(Long kundeId, Collection<RolleType> rollen) {
		if (rollen == null || rollen.isEmpty()) {
			return;
		}

		ks.findKundeById(kundeId, FetchType.NUR_KUNDE, null)
		  .getRollen()
		  .removeAll(rollen);
		flushSecurityCache(kundeId.toString());
	}
	
	/**
	 * siehe http://community.jboss.org/thread/169263
	 * siehe https://docs.jboss.org/author/display/AS7/Management+Clients
	 * siehe https://github.com/jbossas/jboss-as/blob/master/controller-client/
	 * 					src/main/java/org/jboss/as/controller/client/ModelControllerClient.java
	 * siehe http://community.jboss.org/wiki/FormatOfADetypedOperationRequest
	 * siehe http://community.jboss.org/wiki/DetypedDescriptionOfTheAS7ManagementModel
	 * 
	 * Gleicher Ablauf mit CLI (= command line interface):
	 * cd %JBOSS_HOME%\bin
	 * jboss-admin.bat
	 *    connect
	 *    /subsystem=security/security-domain=shop:flush-cache(principal=myUserName)
	 */
	private void flushSecurityCache(String username) {
		ModelControllerClient client;
		try {
			client = ModelControllerClient.Factory.create(LOCALHOST, MANAGEMENT_PORT);
		}
		catch (UnknownHostException e) {
			// Kann nicht passieren: sonst waere "localhost" nicht bekannt
			throw new IllegalStateException(e);
		}
		
		try {
			final ModelNode address = new ModelNode();
			address.add("subsystem", "security");
			address.add("security-domain", SECURITY_DOMAIN);

			final ModelNode operation = new ModelNode();
			operation.get("address").set(address);
			operation.get("operation").set("flush-cache");
			operation.get("principal").set(username);

			try {
				final ModelNode result = client.execute(operation);
				final String resultString = result.get("outcome").asString();
				if (!"success".equals(resultString)) {
					throw new IllegalStateException("FEHLER bei der Operation \"flush-cache\" fuer den Security-Cache: "
							                        + resultString);
				}
			}
			catch (IOException e) {
				throw new IllegalStateException("FEHLER bei der Operation \"flush-cache\" fuer den Security-Cache", e);
			}

		}
		finally {
			if (client != null) {
				try {
					client.close();
				}
				catch (IOException e) {
					throw new IllegalStateException("FEHLER bei der Methode close() fuer den Management-Client", e);
				}
			}
		}
	}
	
	/**
	 */
	public List<RolleType> getEigeneRollen() {		
		final List<RolleType> rollen = new LinkedList<>();
		
		// Authentifiziertes Subject ermitteln
		Subject subject = null;
		try {
			subject = (Subject) PolicyContext.getContext("javax.security.auth.Subject.container");
		}
		catch (PolicyContextException e) {
			final InternalError error = new InternalError(e);
			logger.error(error.getMessage(), error);
			throw error;
		}
		if (subject == null) {
			return null;
		}

		// Gruppe "Roles" ermitteln
		final Set<Principal> principals = subject.getPrincipals(Principal.class);
		for (Principal p : principals) {
			if (!(p instanceof SimpleGroup)) {
				continue;
			}

			final SimpleGroup sg = (SimpleGroup) p;
			if (!"Roles".equals(sg.getName())) {
				continue;
			}
			
			// Rollen ermitteln
			final Enumeration<Principal> members = sg.members();
			while (members.hasMoreElements()) {
				final String rolle = members.nextElement().toString();
				if (rolle != null) {
					rollen.add(RolleType.valueOf(rolle.toUpperCase(Locale.getDefault())));
				}
			}
		}
		return rollen;
	}

	/**
	 */
	public List<String> findUsernameListByUsernamePrefix(String usernamePrefix) {
		final List<String> usernameList = em.createNamedQuery(FIND_LOGIN_BY_LOGIN_PREFIX, String.class)
				                            .setParameter(PARAM_LOGIN_PREFIX, usernamePrefix + '%')
				                            .getResultList();
		return usernameList;
	}
	

	public static void main(String[] args) throws IOException {
		for (;;) {
			System.out.print("Password (Abbruch durch <Return>): ");
			final BufferedReader reader = new BufferedReader(
					                          new InputStreamReader(System.in, Charset.defaultCharset()));
			final String password = reader.readLine();
			if (Strings.isNullOrEmpty(password)) {
				break;
			}
			final String passwordHash = createPasswordHash(HASH_ALGORITHM, HASH_ENCODING, HASH_CHARSET, null, password);
			System.out.println("Verschluesselt: " + passwordHash + System.getProperty("line.separator"));
		}
		
		System.out.println("FERTIG");
	}
}
