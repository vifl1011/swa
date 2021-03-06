package de.shop.kundenverwaltung.service;

import static java.util.logging.Level.FINER;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

import com.google.common.base.Strings;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.kundenverwaltung.domain.PasswordGroup;
import de.shop.util.File;
import de.shop.util.FileHelper.MimeType;
import de.shop.util.IdGroup;
import de.shop.util.Log;
import de.shop.util.Constants;
import de.shop.util.NoMimeTypeException;
import de.shop.util.Transactional;
import de.shop.util.ValidatorProvider;
import de.shop.util.FileHelper;

/**
 * Anwendungslogik fuer die KundeService
 */
@Produces(APPLICATION_JSON)
@Consumes
@RequestScoped
@Transactional
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
	private ValidatorProvider validatorProvider;
	
	@Inject
	private FileHelper fileHelper;
	
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
	public List<Kunde> findKundenByName(String nachname, FetchType fetch, Locale locale) {
		validateName(nachname, locale);
		List<Kunde> kunden;
		switch (fetch) {
			case NUR_KUNDE:
				kunden = em.createNamedQuery(Kunde.FIND_KUNDEN_BY_NAME, Kunde.class)
						   .setParameter(Kunde.PARAM_KUNDE_NAME, nachname)
                           .getResultList();
				break;
			
			case MIT_BESTELLUNGEN:
				kunden = em.createNamedQuery(Kunde.FIND_KUNDEN_BY_NAME_FETCH_BESTELLUNGEN,
						                     Kunde.class)
						   .setParameter(Kunde.PARAM_KUNDE_NAME, nachname)
                           .getResultList();
				break;

			default:
				kunden = em.createNamedQuery(Kunde.FIND_KUNDEN_BY_NAME, Kunde.class)
						   .setParameter(Kunde.PARAM_KUNDE_NAME, nachname)
                           .getResultList();
				break;
		}

		return kunden;
	}
	
	private void validateName(String name, Locale locale) {
		final Validator validator = validatorProvider.getValidator(locale);
		final Set<ConstraintViolation<Kunde>> violations = validator.validateValue(Kunde.class,
				                                                                           "name",
				                                                                           name,
				                                                                           Default.class);
		if (!violations.isEmpty())
			throw new InvalidNachnameException(name, violations);
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
	public List<Kunde> findKundenByIdPrefix(Long id) {
		if (id == null) {
			return Collections.emptyList();
		}
		
		final List<Kunde> kunden = em.createNamedQuery(Kunde.FIND_KUNDEN_BY_ID_PREFIX,
				                                               Kunde.class)
				                             .setParameter(Kunde.PARAM_KUNDE_ID_PREFIX, id.toString() + '%')
				                             .getResultList();
		return kunden;
	}
	
	@Log
	public Kunde findKundenByLogin(String login) {
		if (login == null) {
			return null;
		}
		try {
			final Kunde kunde = em.createNamedQuery(Kunde.FIND_KUNDE_BY_LOGIN,
					                                               Kunde.class)
					                             .setParameter(Kunde.PARAM_KUNDE_LOGIN, login)
					                             .getSingleResult();
			return kunde;
		} catch (NoResultException e) {
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
		final Validator validator = validatorProvider.getValidator(locale);
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
		final Validator validator = validatorProvider.getValidator(locale);
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
		final Validator validator = validatorProvider.getValidator(locale);
		
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
		kunde.setPasswortWdh(kunde.getPasswort());
		validateKunde(kunde, locale, Default.class, PasswordGroup.class);
		// Pruefung, ob die Email-Adresse schon existiert
		final Kunde eKunde = findKundeByEmail(kunde.getEmail(), locale);
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
		LOGGER.finest("updateKunde BEGIN");
		if (kunde == null) {
			return null;
		}
		// Werden alle Constraints beim Update gewahrt?
		validateKunde(kunde, locale, Default.class, PasswordGroup.class, IdGroup.class);
		//Detachen des Kunden fals vorhanden
		em.detach(kunde);
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
		em.detach(eKunde);
		LOGGER.finest("originalKunde:" + eKunde);
		LOGGER.finest("gešnderterKunde:" + kunde);
		em.merge(kunde);
		LOGGER.finest("updateKunde END");
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
		if (!(kunde.getBestellungen() == null) && !kunde.getBestellungen().isEmpty()) {
			throw new KundeDeleteBestellungException(kunde);
		}
		
		em.remove(kunde);
	}
	
	/**
	 * Ohne MIME Type fuer Upload bei RESTful WS
	 */
	public void setFile(Long kundeId, byte[] bytes, Locale locale) {
		final Kunde kunde = findKundeById(kundeId, FetchType.NUR_KUNDE, locale);
		if (kunde == null) {
			return;
		}
		final MimeType mimeType = fileHelper.getMimeType(bytes);
		setFile(kunde, bytes, mimeType);
	}
	
	/**
	 * Mit MIME-Type fuer Upload bei Webseiten
	 */
	public void setFile(Kunde kunde, byte[] bytes, String mimeTypeStr) {
		final MimeType mimeType = MimeType.get(mimeTypeStr);
		setFile(kunde, bytes, mimeType);
	}
	
	private void setFile(Kunde kunde, byte[] bytes, MimeType mimeType) {
		if (mimeType == null) {
			throw new NoMimeTypeException();
		}
		
		final String filename = fileHelper.getFilename(kunde.getClass(), kunde.getId(), mimeType);
		
		// Gibt es noch kein (Multimedia-) File
		File file = kunde.getFile();
		if (file == null) {
			file = new File(bytes, filename, mimeType);
			kunde.setFile(file);
			em.persist(file);
		}
		else {
			file.set(bytes, filename, mimeType);
			em.merge(file);
		}
	}
	
	public List<Long> findIdsByPrefix(String idPrefix) {
		if (Strings.isNullOrEmpty(idPrefix)) {
			return Collections.emptyList();
		}
		final List<Long> ids = em.createNamedQuery(Kunde.FIND_IDS_BY_PREFIX, Long.class)
				                 .setParameter(Kunde.PARAM_KUNDE_ID_PREFIX, idPrefix + '%')
				                 .getResultList();
		return ids;
	}
	
	public List<String> findNamenByPrefix(String namePrefix) {
		final List<String> namen = em.createNamedQuery(Kunde.FIND_NAMEN_BY_PREFIX, String.class)
				                         .setParameter(Kunde.PARAM_KUNDE_NAME_PREFIX, namePrefix + '%')
				                         .getResultList();
		return namen;
	}
	
}
