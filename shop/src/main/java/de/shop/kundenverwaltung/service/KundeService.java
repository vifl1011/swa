package de.shop.kundenverwaltung.service;

import static java.util.logging.Level.FINER;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.kundenverwaltung.domain.PasswordGroup;
import de.shop.util.IdGroup;
import de.shop.util.Log;
import de.shop.util.ValidationService;
import de.shop.util.Constants;

/**
 * Anwendungslogik fuer die KundeService
 */
@Log
public class KundeService implements Serializable {
	private static final long serialVersionUID = -5520738420154763865L;
	
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	public enum FetchType {
		NUR_KUNDE,
		MIT_BESTELLUNGEN,
		MIT_ADRESSE
	}
	
	@PersistenceContext
	private transient EntityManager em;
	
	@Inject
	private ValidationService validationService;
	
	@PostConstruct
	private void postConstruct() {
		LOGGER.log(FINER, "CDI-faehiges Bean {0} wurde erzeugt", this);
	}	
	
	@PreDestroy
	private void preDestroy() {
		LOGGER.log(FINER, "CDI-faehiges Bean {0} wird geloescht", this);
	}
	
	@Log
	public List<Kunde> findAllKunden(FetchType fetch) {
		List<Kunde> kunden = null;
		switch (fetch) {
			case NUR_KUNDE:
				kunden = em.createNamedQuery(Kunde.FIND_KUNDEN, Kunde.class)
	             .getResultList();
				break;
			case MIT_BESTELLUNGEN:
				kunden = em.createNamedQuery(Kunde.FIND_KUNDEN_FETCH_BESTELLUNGEN, Kunde.class)
				  .getResultList();
				break;
			case MIT_ADRESSE:
				kunden = em.createNamedQuery(Kunde.FIND_KUNDEN_FETCH_ADRESSE, Kunde.class)
				  .getResultList();
				break;
			default:
				break;
		}
		return kunden;
	}
	
	@Log
	public Kunde findKundeByEmail(String email, Locale locale) {
		validateEmail(email, locale);
		try {
			final Kunde kunde = em.createNamedQuery(Kunde.FIND_KUNDE_BY_EMAIL, Kunde.class)
					                      .setParameter(Kunde.PARAM_KUNDE_EMAIL, email)
					                      .getSingleResult();
			return kunde;
		}
		catch (NoResultException e) {
			return null;
		}
	}
	
	@Log
	public Kunde findKundeById(Long kundeId, FetchType fetch, Locale locale) {
		validateKundeId(kundeId, locale);
		Kunde kunde = null;
		switch(fetch) {
			case NUR_KUNDE:
				kunde = em.createNamedQuery(Kunde.FIND_KUNDE_BY_ID, Kunde.class)
								.setParameter(Kunde.PARAM_KUNDE_ID, kundeId)
								.getSingleResult();
				break;
			case MIT_BESTELLUNGEN:
				kunde = em.createNamedQuery(Kunde.FIND_KUNDE_BY_ID_FETCH_BESTELLUNGEN, Kunde.class)
											.setParameter(Kunde.PARAM_KUNDE_ID, kundeId)
											.getSingleResult();
				break;	
			case MIT_ADRESSE:
				kunde = em.createNamedQuery(Kunde.FIND_KUNDE_BY_ID_FETCH_ADRESSE, Kunde.class)
											.setParameter(Kunde.PARAM_KUNDE_ID, kundeId)
											.getSingleResult();				
				break;
			default:
				break;
		}
		return kunde;
	}
	
	@Log
	private void validateKundeId(Long kundeId, Locale locale) {
		//erstellen einer Validator Instanz in der Sprache locale
		final Validator validator = validationService.getValidator(locale);
		//
		
		final Set<ConstraintViolation<Kunde>> violations = validator.validateValue(Kunde.class,
				                                                                           "id",
				                                                                           kundeId,
				                                                                           IdGroup.class);
		if (!violations.isEmpty())
			throw new InvalidKundeIdException(kundeId, violations);
	}
	
	@Log
	private void validateEmail(String email, Locale locale) {
		//erstellen einer Validator Instanz in der Sprache locale
		final Validator validator = validationService.getValidator(locale);
		//
		final Set<ConstraintViolation<Kunde>> violations = validator.validateValue(Kunde.class,
				                                                                           "email",
				                                                                           email,
				                                                                           Default.class);
		if (!violations.isEmpty())
			throw new InvalidKundeEmailException(email, violations);
	}
	
	@Log
	private void validateKunde(Kunde kunde, Locale locale, Class<?>... groups) {
		// Werden alle Constraints beim Einfuegen gewahrt?
		final Validator validator = validationService.getValidator(locale);
		
		final Set<ConstraintViolation<Kunde>> violations = validator.validate(kunde, groups);
		if (!violations.isEmpty()) {
			throw new KundeValidationException(kunde, violations);
		}
	}
	
	@Log
	public Kunde createKunde(Kunde kunde, Locale locale) {
		if (kunde == null) {
			return null;
		}
		
		// Werden alle Constraints beim Einfuegen gewahrt?
		validateKunde(kunde, locale, Default.class, PasswordGroup.class);
		// Pruefung, ob die Email-Adresse schon existiert
		Kunde eKunde = findKundeByEmail(kunde.getEmail(), locale);
		if (eKunde == null) {
			LOGGER.finest("Email-Adresse existiert noch nicht");
		}
		else {
			throw new EmailExistsException(kunde.getEmail());
		}
		if (kunde.getAdresse() == null) {
			throw new KundeCreateAdresseException(kunde);
		}
		kunde.getAdresse().setKunde(kunde);
			
		kunde.setId(Constants.KEINE_ID);
		em.persist(kunde);
		return kunde;	
	}
	
	@Log
	public Kunde updateKunde(Kunde kunde, Locale locale) {
		if (kunde == null) {
			return null;
		}
		// Werden alle Constraints beim Update gewahrt?
		validateKunde(kunde, locale, Default.class, PasswordGroup.class, IdGroup.class);
		final Kunde eKunde = findKundeByEmail(kunde.getEmail(), locale);
		
		if (eKunde == null) {
			LOGGER.finest("Email-Adresse existiert noch nicht");
		}
		else if (eKunde.getId().longValue() == kunde.getId().longValue()) {
			LOGGER.finest("Email-Adresse existiert bereits, allerdings unter gleicher ID => kein Problem");
		}
		else {
			throw new EmailExistsException(kunde.getEmail());
		}

		em.merge(kunde);
		return kunde;
	}
	
	@Log
	public void deleteKunde(Kunde kunde) {
		if (kunde == null) {
			return;
		}
		// Bestellungen laden, damit sie anschl. ueberprueft werden koennen
		try {
			kunde = findKundeById(kunde.getId(), FetchType.MIT_BESTELLUNGEN, Locale.getDefault());
		}
		catch (InvalidKundeIdException e) {
			return;
		}
		if (kunde == null) {
			return;
		}
		// Gibt es Bestellungen?
		if (!kunde.getBestellungen().isEmpty()) {
			throw new KundeDeleteBestellungException(kunde);
		}
		
		em.remove(kunde);
	}

	
	
}