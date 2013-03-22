/**
 * 
 */
package de.shop.kundenverwaltung.service;


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.shop.kundenverwaltung.domain.Adresse;
import de.shop.kundenverwaltung.domain.GeschlechtType;
import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.kundenverwaltung.service.KundeService.FetchType;
import de.shop.util.AbstractTest;
import de.shop.util.Log;


/**
 * @author Patrik
 *
 */
@RunWith(Arquillian.class)
@FixMethodOrder(NAME_ASCENDING)
public class KundeServiceTest extends AbstractTest {

	@Inject
	private KundeService ks;
	
	private static final String NACHNAME_NEU = "Test";
	private static final String VORNAME_NEU = "Theo";
	private static final String LOGIN_NEU = "Theo21";
	private static final String PASSWORT_NEU = "Theo21";
	private static final String EMAIL_NEU = "theo@test.de";
	private static final float RABATT_NEU = 0.5f;
	private static final String PLZ_NEU = "11111";
	private static final String ORT_NEU = "Testort";
	private static final String STRASSE_NEU = "Testweg";
	private static final String HAUSNR_NEU = "1";
	
	private static final String EMAIL_AKT = "theosneuemail@test.de";
	
	private static final Long ID_BESTAND = Long.valueOf("201");
	private static final String EMAIL_BESTAND = "Joebstl.Emi@web-ka.de";
	
	
	private Kunde createBspKunde() {
		Kunde kunde = new Kunde(EMAIL_NEU, GeschlechtType.MAENNLICH, LOGIN_NEU, NACHNAME_NEU, VORNAME_NEU, 
								PASSWORT_NEU, RABATT_NEU);
		
		Adresse adresse = new Adresse(ORT_NEU, PLZ_NEU, STRASSE_NEU, HAUSNR_NEU, kunde);
		kunde.setAdresse(adresse);
		return kunde;
	
	}
	
	@Test
	public void validate() {
		assertThat(true, is(true));
	}
	
	@Test  // innerhalb eines laufenden JBoss
	public void findAllKunden() {
		// Given
		List<Kunde> kunden = null;

		// When
		kunden = ks.findAllKunden(KundeService.FetchType.NUR_KUNDE);
		// Then
		assertThat(kunden.size() > 0, is(true));
	}

	@Test  // innerhalb eines laufenden JBoss
	public void findKundeById() {
		// Given
		Long kundeId = ID_BESTAND;
		Locale locale = Locale.GERMAN;
		// When
		Kunde kunde = ks.findKundeById(ID_BESTAND, FetchType.NUR_KUNDE, locale);
		// Then
		assertThat(kunde.getId(), is(kundeId));
	}
	
	@Test  // innerhalb eines laufenden JBoss
	public void findKundeByEmail() throws RollbackException, 
				HeuristicMixedException, HeuristicRollbackException, SystemException, NotSupportedException {
		// Given
		String email = EMAIL_BESTAND.trim(); 
		final UserTransaction trans = getUserTransaction();
		trans.commit();
		// When
		trans.begin();
		Kunde kunde = ks.findKundeByEmail(email, Locale.GERMAN);
		trans.commit();
		// Then
		assertThat(kunde.getEmail(), is(email));
	}
	
	@Log
	@Test
	public void aCreate() throws RollbackException, 
					HeuristicMixedException, HeuristicRollbackException, SystemException, NotSupportedException {
		// Given
		Integer orgAnzahl = ks.findAllKunden(KundeService.FetchType.NUR_KUNDE).size();
		final UserTransaction trans = getUserTransaction();
		trans.commit();
		
		Kunde kunde = createBspKunde();
		Locale locale = Locale.GERMAN;
		// When
		trans.begin();
		Kunde ckunde = ks.createKunde(kunde, locale);	
		trans.commit();
		
		trans.begin();
		Integer neueAnzahl = ks.findAllKunden(KundeService.FetchType.NUR_KUNDE).size();
		trans.commit();
		// Then
		assertThat(ckunde.getEmail(),   is(EMAIL_NEU));
		assertThat(ckunde.getErzeugt() != null, is(true));
		assertThat(ckunde.getAktualisiert() != null, is(true));
		assertThat(orgAnzahl + 1 == neueAnzahl, is(true));

	}
	
	@Test
	public void bUpdate() {
		// Given
		Locale locale = Locale.GERMAN;
		Kunde kunde = ks.findKundeByEmail(EMAIL_NEU, locale);
		kunde.setEmail(EMAIL_AKT);
		
		// When		
		ks.updateKunde(kunde, locale);
		Kunde updkunde = ks.findKundeByEmail(EMAIL_AKT, locale);
		
		// Then
		assertThat(updkunde.getEmail(), is(EMAIL_AKT));
	}


	@Test
	public void cDelete() {
		// Given
		Locale locale = Locale.GERMAN;
		Kunde kunde = ks.findKundeByEmail(EMAIL_AKT, locale);
		
		// When		
		ks.deleteKunde(kunde);
		Kunde leererKunde = ks.findKundeByEmail(EMAIL_AKT, locale);
		
		// Then
		assertThat(leererKunde, is(nullValue()));
	}
	
	@Log
	@Test
	public void createDuplicateKunde() throws RollbackException, 
					HeuristicMixedException, HeuristicRollbackException, SystemException, NotSupportedException {
		// Given
		final UserTransaction trans = getUserTransaction();
		trans.commit();
		
		Kunde kunde = createBspKunde();
		Locale locale = Locale.GERMAN;
		// When
		trans.begin();
		Kunde cKunde = ks.createKunde(kunde, locale);	
		trans.commit();
		

		// Then
		thrown.expect(EmailExistsException.class);
		trans.begin();
		ks.createKunde(kunde, LOCALE);
		
		
		assertThat(cKunde.getEmail(),   is(EMAIL_NEU));
		assertThat(cKunde.getErzeugt() != null, is(true));
		assertThat(cKunde.getAktualisiert() != null, is(true));
	}
}
