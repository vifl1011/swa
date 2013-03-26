/**
 * 
 */
package de.shop.kundenverwaltung.domain;


import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Set;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.shop.util.AbstractResourceTest;
import java.util.List;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

/**
 * @author Patrik
 *
 */
@RunWith(Arquillian.class)
public class KundeTest extends AbstractResourceTest {
	
	private static final String NACHNAME_NEU = "Test";
	private static final String VORNAME_NEU = "Theo";
	private static final String LOGIN_NEU = "TheosLogin";
	private static final String PASSWORT_NEU = "TheosPassword";
	private static final String EMAIL_NEU = "theo@test.de";
	private static final float RABATT_NEU = 0.5f;
	private static final String PLZ_NEU = "11111";
	private static final String ORT_NEU = "Testort";
	private static final String STRASSE_NEU = "Testweg";
	private static final String HAUSNR_NEU = "1";
	
	private static final Long ID_BESTAND = Long.valueOf("201");
	
	
	@Test
	public void validate() {
		assertThat(true, is(true));
	}
	
	
//	TODO vorerst wegen fehlendem EntityManager auskommentiert
//	@Test  // innerhalb eines laufenden JBoss
//	public void findKundeById() {
//		// Given
//		Long kundeId = ID_BESTAND;
//		// When
//		Kunde kunde = getEntityManager().find(Kunde.class, kundeId);
//		// Then
//		assertThat(kunde.getId(), is(kundeId));
//	}
	
	
//	TODO vorerst wegen fehlendem EntityManager auskommentiert
//	@Test  // innerhalb eines laufenden JBoss
//	public void findKundeByEmail() {
//		// Given
//		String email = "Weigel.Tobias@web-ka.de";
//		// When
//		final Kunde kunde = getEntityManager().createNamedQuery(Kunde.FIND_KUNDE_BY_EMAIL,
//                                                                Kunde.class)
//                                                      .setParameter(Kunde.PARAM_KUNDE_EMAIL, email)
//				                                      .getSingleResult();
//		// Then
//		assertThat(kunde.getEmail(), is(email));
//	}
	
	
//	TODO vorerst wegen fehlendem EntityManager auskommentiert
//	@Test
//	public void createKunde() {
//		// Given
//		
//		Kunde kunde = new Kunde(EMAIL_NEU, GeschlechtType.MAENNLICH, LOGIN_NEU, NACHNAME_NEU, VORNAME_NEU, 
//								PASSWORT_NEU, RABATT_NEU);
//		
//		Adresse adresse = new Adresse(ORT_NEU, PLZ_NEU, STRASSE_NEU, HAUSNR_NEU, kunde);
//		kunde.setAdresse(adresse);		
//		
//		// When
//		try {
//			getEntityManager().persist(kunde);         // abspeichern einschl. Adresse
//		}
//		catch (ConstraintViolationException e) {
//			// Es gibt Verletzungen bzgl. Bean Validation: auf der Console ausgeben
//			final Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
//			for (ConstraintViolation<?> v : violations) {
//				System.err.println("!!! FEHLERMELDUNG>>> " + v.getMessage());
//				System.err.println("!!! ATTRIBUT>>> " + v.getPropertyPath());
//				System.err.println("!!! ATTRIBUTWERT>>> " + v.getInvalidValue());
//			}
//			
//			throw new RuntimeException(e);
//		}
//		
//		// Then
//		
//		// Den abgespeicherten Kunden ueber eine Named Query ermitteln
//		final List<Kunde> kunden = getEntityManager().createNamedQuery(Kunde.FIND_KUNDEN_BY_NACHNAME,
//                                                                       Kunde.class)
//                                                             .setParameter(Kunde.PARAM_KUNDE_NACHNAME,
//                                                                    	   NACHNAME_NEU)
//				                                             .getResultList();
//		
//		// Ueberpruefung des ausgelesenen Objekts
//		assertThat(kunden.size(), is(1));
//		kunde = (Kunde) kunden.get(0);
//		assertThat(kunde.getId().longValue() > 0, is(true));
//		assertThat(kunde.getName(), is(NACHNAME_NEU));
//	}
}
