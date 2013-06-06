package de.shop.bestellverwaltung.service;

import static java.util.logging.Level.FINER;
import static java.util.logging.Level.FINEST;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;

import de.shop.util.IdGroup;
import de.shop.util.NotFoundException;
import de.shop.util.Transactional;
import de.shop.util.ValidatorProvider;
import de.shop.artikelverwaltung.domain.Produkt;
import de.shop.artikelverwaltung.service.ProduktService;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.bestellverwaltung.domain.Bestellposition;
import de.shop.bestellverwaltung.domain.Lieferung;
import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.util.Log;

@Transactional
public class BestellService implements Serializable {

	public enum FetchType {
		NUR_BESTELLUNG, MIT_KUNDE
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	private static final String EXC_NO_SHIPMENT_AVAILABLE = "keine Lieferung vorhanden mit ID ";
	private static final String EXC_NO_PRODUCT_AVAILABLE = "kein Produkt vorhanden mit ID ";
	private static final String EXC_NO_ORDER_AVAILABLE = "keine Bestellung vorhanden mit ID ";
	private static final String EXC_ORDER_ITEMS_NULL_OR_EMPTY = "Bestellung nicht erstellt => bestellpositionen sind null oder leer";

	@PersistenceContext
	private transient EntityManager em;

	@Inject
	private ValidatorProvider validatorProvider;
	
	@Inject
	private ProduktService ps;

	@PostConstruct
	private void postConstruct() {
		LOGGER.log(FINER, "CDI-faehiges Bean {0} wurde erzeugt", this);
	}

	@PreDestroy
	private void preDestroy() {
		LOGGER.log(FINER, "CDI-faehiges Bean {0} wird geloescht", this);
	}
	
	public void resetConstraintsByBestellposition(Bestellposition bestellposition, Locale locale) {
		//Extrahiere Lieferung ID
		
		final String lieferungUriStr = bestellposition.getLieferungUri().toString();
		int startPos = lieferungUriStr.lastIndexOf('/') + 1;
		final String lieferungIdStr = lieferungUriStr.substring(startPos);
				
		Long lieferungId = null;
		
		try {
			lieferungId = Long.valueOf(lieferungIdStr);
		} catch (NotFoundException e) {
			throw new NotFoundException(EXC_NO_SHIPMENT_AVAILABLE + lieferungIdStr, e);
		}
				
		final Lieferung lieferung = findLieferungById(lieferungId, locale);
		
		if (lieferung == null) {
			throw new NotFoundException(EXC_NO_SHIPMENT_AVAILABLE + lieferungId);
		}
				
		bestellposition.setLieferung(lieferung);
		
		//Extrahiere Produkt ID
		
		final String produktUriStr = bestellposition.getProduktUri().toString();
		startPos = produktUriStr.lastIndexOf('/') + 1;
		final String produktIdStr = produktUriStr.substring(startPos);
		
		Long produktId = null;
		
		try {
			produktId = Long.valueOf(produktIdStr);
		} catch (NotFoundException e) {
			throw new NotFoundException(EXC_NO_PRODUCT_AVAILABLE + produktIdStr, e);
		}
		
		final Produkt produkt = ps.findProduktById(produktId, locale);
		
		if (produkt == null) {
			throw new NotFoundException(EXC_NO_PRODUCT_AVAILABLE + produktId);
		}
		
		bestellposition.setProdukt(produkt);
		
		//Extrahiere Bestellung ID
		
		final String bestellungUriStr = bestellposition.getBestellungUri().toString();
		startPos = bestellungUriStr.lastIndexOf('/') + 1;
		final String bestellungIdStr = bestellungUriStr.substring(startPos);
		
		Long bestellungId = null;
		
		try {
			bestellungId = Long.valueOf(bestellungIdStr);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		
		final Bestellung bestellung = findBestellungById(bestellungId, locale);
		
		if (bestellung == null) {
			throw new NotFoundException(EXC_NO_ORDER_AVAILABLE + bestellungId);
		}
		
		bestellposition.setBestellung(bestellung);
	}

	/*
	 * Sucht alle Bestellungen und gibt diese in Form einer Liste zurück
	 */
	@Log
	public List<Bestellung> findBestellungen(Locale locale) {
		List<Bestellung> result = null;
		result = em.createNamedQuery(Bestellung.FIND_BESTELLUNGEN, Bestellung.class).getResultList();

		return result;
	}

	/*
	 * Liefert eine Bestellung mit der entsprechenden ID zurück
	 */
	public Bestellung findBestellungById(Long id, Locale locale) {
		final List<Bestellung> result = em.createNamedQuery(Bestellung.FIND_BESTELLUNG_BY_ID, Bestellung.class)
				.setParameter(Bestellung.PARAM_BESTELLUNG_ID, id).getResultList();

		if (result.isEmpty())
			return null;
		else
			return result.get(0);
	}
	
	/*
	 * Sucht eine bestimmte Bestellposition
	 */
	@Log
	public Bestellposition findBestellpositionById(Long id, Locale locale) {
		return em.createNamedQuery(Bestellposition.FIND_BESTELLPOSITION_BY_ID, Bestellposition.class)
				.setParameter(Bestellposition.PARAM_ID, id).getSingleResult();
	}

	/*
	 * Sucht eine bestimmte lieferung
	 */
	@Log
	public Lieferung findLieferungById(Long id, Locale locale) {
		return em.createNamedQuery(Lieferung.FIND_LIEFERUNG_BY_ID, Lieferung.class)
				.setParameter(Lieferung.PARAM_LIEFERUNG_ID, id).getSingleResult();
	}

	/*
	 * gibt alle Lieferungen zurück
	 */
	@Log
	public List<Lieferung> findLieferungen(Locale locale) {
		final List<Lieferung> result = em.createNamedQuery(Lieferung.FIND_LIEFERUNGEN, Lieferung.class).getResultList();

		if (result.isEmpty())
			return null;
		else
			return result;
	}

	/*
	 * Sucht alle Bestellungen eines Kunden und gibt diese in einer Liste
	 * zurück. Die Anzahl der Listeneinträge entspricht hierbei der IDX für die
	 * nächste Bestellung
	 */
	@Log
	public List<Bestellung> findBestellungenByKunde(Kunde kunde, FetchType fetch) {
		List<Bestellung> result = null;
		result = em.createNamedQuery(Bestellung.FIND_BESTELLUNGEN_BY_KUNDE_ID, Bestellung.class)
				.setParameter(Bestellung.PARAM_BESTELLUNG_KUNDE_ID, kunde.getId()).getResultList();

		return result;
	}

	/*
	 * sucht eine Bestellung nach einem Datum
	 */
	@Log
	public List<Bestellung> findBestellungByDate(Date date, FetchType fetch) {
		List<Bestellung> result = null;
		switch (fetch) {
		case NUR_BESTELLUNG:
			result = em.createNamedQuery(Bestellung.FIND_BESTELLUNGEN_BY_DATE, Bestellung.class)
					.setParameter(Bestellung.PARAM_BESTELLUNG_DATE, date).getResultList();
			break;
		case MIT_KUNDE:
			break;
		default:
			break;	
		}

		return result;
	}

	public Bestellposition findBestellpositionOfBestellungByProduktId(
			Bestellung bestellung, Produkt produkt, Locale locale) {
		Bestellposition result = null;

		result = em.createNamedQuery(Bestellposition.FIND_BESTELLPOSITIONEN_BY_BESTELLUNG_BY_PRODUKT,
						Bestellposition.class)
				.setParameter(Bestellposition.PARAM_BESTELLUNG_ID, bestellung.getId())
				.setParameter(Bestellposition.PARAM_PRODUKT_ID, produkt.getId()).getSingleResult();

		return result;
	}

	/*
	 * Sucht alle Bestellpositionen die zu einer Bestellung gehören und gibt sie
	 * in einer Liste zurück. Die Anzahl der Listeneinträge entspricht der
	 * höchsten idx dieser Bestellpositionen + 1 weshalb die Anzahl der
	 * Listeneinträge als neue IDX verwendet wird
	 */
	@Log
	public List<Bestellposition> findBestellpositionenByBestellung(Bestellung bestellung) {
		List<Bestellposition> result = null;

		result = em.createNamedQuery(Bestellposition.FIND_BESTELLPOSITIONEN_BY_BESTELLUNG_ID,
						Bestellposition.class)
				.setParameter(Bestellposition.PARAM_BESTELLUNG_ID, bestellung.getId()).getResultList();

		return result;
	}

	@Log
	public Lieferung createLieferung(Lieferung lieferung, Locale locale) {
		em.persist(lieferung);

		return lieferung;
	}
	
	@Log
	public Lieferung updateLieferung(Lieferung lieferung, Locale locale) {
		if (lieferung == null) {
			LOGGER.finest("Lieferung ist null => Update nicht möglich");
			return null;
		}
			
		if (lieferung.getId() == null) {
			LOGGER.finest("ID der Lieferung ist null => Update nicht möglich, noch nicht in der Datenbank vorhanden");
			return null;
		}
		
		em.detach(lieferung);
		final Lieferung tmp = findLieferungById(lieferung.getId(), locale);
		
		if (tmp == null) {
			final String msg = "Keine solche Lieferung vorhanden";
			throw new NotFoundException(msg);
		}
		
		LOGGER.log(FINEST, "Lieferung vorher: %s", tmp);
		em.detach(tmp);
		LOGGER.log(FINEST, "Lieferung nachher: %s", lieferung);
		
		em.merge(lieferung);
		LOGGER.finest("update der Lieferung erfolgreich");
		
		return lieferung;
	}
	
	//Erstellt eine Bestellposition
	//Eine Bestellposition benötigt immer ein Produkt, eine Lieferung und eine dazugehörige Bestellung
	@Log
	public Bestellposition createBestellposition(Bestellposition bestellposition, Locale locale) {
		final Produkt produkt = bestellposition.getProdukt();
		bestellposition.setEinzelpreis(produkt.getPreis());
		em.persist(bestellposition);

		return bestellposition;
	}
	
	//Updated eine Bestellposition
	@Log
	public Bestellposition updateBestellposition(Bestellposition bestellposition, Locale locale)
	{
		if (bestellposition == null) {
			LOGGER.log(FINEST, "Bestellposition ist null => Update nicht möglich");
			return null;
		}
			
		if (bestellposition.getId() == null) {
			LOGGER.log(FINEST, "ID der Bestellposition null => Update nicht möglich, " 
													+ "noch nicht in der Datenbank vorhanden");
			return null;
		}
		
		final Bestellposition tmp = findBestellpositionById(bestellposition.getId(), locale);
		
		if (tmp == null) {
			final String msg = "Bestellposition nicht gefunden";
			throw new NotFoundException(msg);
		}
		
		//Extrahiere Lieferung ID
		final String lieferungUriStr = bestellposition.getLieferungUri().toString();
		int startPos = lieferungUriStr.lastIndexOf('/') + 1;
		final String lieferungIdStr = lieferungUriStr.substring(startPos);
		
		Long lieferungId = null;
		
		try {
			lieferungId = Long.valueOf(lieferungIdStr);
		} catch (NotFoundException e) {
			throw new NotFoundException(EXC_NO_SHIPMENT_AVAILABLE + lieferungIdStr, e);
		}
		
		final Lieferung lieferung = findLieferungById(lieferungId, locale);
		
		if (lieferung == null) {
			throw new NotFoundException(EXC_NO_SHIPMENT_AVAILABLE + lieferungId);
		}
		
		//	Extrahiere Produkt ID
		
		final String produktUriStr = bestellposition.getProduktUri().toString();
		startPos = produktUriStr.lastIndexOf('/') + 1;
		final String produktIdStr = produktUriStr.substring(startPos);
		
		Long produktId = null;
		
		try {
			produktId = Long.valueOf(produktIdStr);
		} catch (NotFoundException e) {
			throw new NotFoundException(EXC_NO_PRODUCT_AVAILABLE + produktIdStr, e);
		}
		
		final Produkt produkt = ps.findProduktById(produktId, locale);
		
		if (produkt == null) {
			throw new NotFoundException(EXC_NO_PRODUCT_AVAILABLE + produktId);
		}
		
		//	Extrahiere Bestellung ID
		
		final String bestellungUriStr = bestellposition.getBestellungUri().toString();
		startPos = bestellungUriStr.lastIndexOf('/') + 1;
		final String bestellungIdStr = bestellungUriStr.substring(startPos);
		
		Long bestellungId = null;
		
		try {
			bestellungId = Long.valueOf(bestellungIdStr);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		
		final Bestellung bestellung = findBestellungById(bestellungId, locale);
		
		if (bestellung == null) {
			throw new NotFoundException(EXC_NO_ORDER_AVAILABLE + bestellungId);
		}
		
		
		bestellposition.setLieferung(lieferung);
		bestellposition.setBestellung(bestellung);
		bestellposition.setProdukt(produkt);
		
//		LOGGER.log(FINEST, "Bestellposition vorher: %s", tmp);
//		tmp.setValues(bestellposition);
//		LOGGER.log(FINEST, "Bestellposition nachher: %s", tmp);
		
		//TODO Preis wird noch nicht gesetzt, benötigt wird NamedQuery zum finden des Produkts einer Bestellposition
		validateBestellposition(bestellposition, locale, Default.class, IdGroup.class);
		em.merge(bestellposition);
		LOGGER.finest("update der Bestellposition erfolgreich");
		
		return bestellposition;
	}
	
	//Legt eine Bestellung mit einer oder gegebenenfalls mehreren Bestellpositionen an
	@Log
	public Bestellung createBestellung(Bestellung bestellung, Kunde kunde, Locale locale) {
		if (bestellung == null) {
			LOGGER.finest("Konnte Bestellung nicht erstellen => Bestellung ist null");
			throw new IllegalArgumentException("Konnte Bestellung nicht erstellen => Bestellung ist null");
		}
		if (kunde == null) {
			LOGGER.finest("Konnte Bestellung nicht erstellen => Kunde ist null");
			throw new IllegalArgumentException("Konnte Bestellung nicht erstellen => Kunde ist null");
		}
		
		bestellung.setKunde(kunde);
		kunde.addBestellung(bestellung);
		
		final List<Bestellposition> bestellpositionen = bestellung.getBestellpositionen();
		
		if (bestellpositionen == null || bestellpositionen.isEmpty()) {
			LOGGER.finest(EXC_ORDER_ITEMS_NULL_OR_EMPTY);
			throw new IllegalArgumentException(EXC_ORDER_ITEMS_NULL_OR_EMPTY);
		}
		
		float p = (float) 0;
		Produkt produkt = null;
		for (Bestellposition bp : bestellpositionen) {
			produkt = bp.getProdukt();
			
			if (produkt == null) {
				throw new NotFoundException("Produkt nicht auffindbar");
			}
			bp.setEinzelpreis(produkt.getPreis());
			p += bp.getEinzelpreis();
			bp.setBestellung(bestellung);
		}
		bestellung.setGesamtpreis(p);		
		//bestellung = em.find(Bestellung.class, bestellung.getId());
		em.persist(bestellung);
		LOGGER.finest("Bestellung wurde erfolgreich erstellt mit ID " + bestellung.getId());
		
		return bestellung;
	}
	
	
	
	//Erstellt eine Bestellung mit genau einer Bestellposition
	@Log
	public Bestellung createBestellung(
			Kunde kunde, Produkt produkt, int menge, Lieferung lieferung, Locale locale) throws Exception {
		// Bestellung anlegen
		final Bestellung bestellung = new Bestellung();
		bestellung.setKunde(kunde);
		kunde.addBestellung(bestellung);

		// Bestellposition anlegen
		final Bestellposition bestellposition = new Bestellposition(bestellung, lieferung, produkt, menge);

		// bestellung anpassen
		bestellung.setGesamtpreis(bestellposition.getEinzelpreis());

		validateBestellung(bestellung, locale, Default.class);

		em.persist(bestellung);

		return bestellung;
	}

	public Bestellung updateBestellung(Bestellung bestellung, Locale locale) {
		if (bestellung == null) {
			LOGGER.finest("Bestellung ist null => Update nicht möglich");
			return null;
		}
			
		if (bestellung.getId() == null) {
			LOGGER.finest("ID der Bestellung ist null => Update nicht möglich, noch nicht in der Datenbank vorhanden");
			return null;
		}
		
		final Bestellung orgBestellung = findBestellungById(bestellung.getId(), locale);
		
		if (orgBestellung == null) {
			final String msg = "Bestellung nicht gefunden";
			throw new NotFoundException(msg);
		}
		
		final List<Bestellposition> bestellpositionen = findBestellpositionenByBestellung(bestellung);
		
		if (bestellpositionen == null)
			throw new NotFoundException("keine Bestellpositionen vorhanden!");
		
		for (Bestellposition bp : bestellpositionen) {
			if (bp == null)
				continue;
			
			//resetConstraintsByBestellposition(bp, locale);
			bp.setBestellung(bestellung);
		}
		
		bestellung.setBestellpositionen(bestellpositionen);
		
		//final List<Bestellposition> bestellpositionen = bestellung.getBestellpositionen();
		
		if (bestellpositionen == null || bestellpositionen.isEmpty()) {
			LOGGER.finest(EXC_ORDER_ITEMS_NULL_OR_EMPTY);
			throw new IllegalArgumentException(EXC_ORDER_ITEMS_NULL_OR_EMPTY);
		}
		
		float p = (float) 0;

		for (Bestellposition bp : bestellpositionen) {
			p += bp.getEinzelpreis();
			bp.setBestellung(bestellung);
		}
		bestellung.setGesamtpreis(p);
		
		em.merge(bestellung);
		
		LOGGER.finest("update der Bestellung erfolgreich");
		
		return bestellung;
	}

	// Validation Methods....
	public void validateBestellung(Bestellung bestellung, Locale locale, Class<?>... groups) {
		final Validator validator = validatorProvider.getValidator(locale);

		final Set<ConstraintViolation<Bestellung>> violations = validator.validate(bestellung, groups);
		if (!violations.isEmpty()) {
			throw new BestellungValidationException(bestellung, violations);
		}
	}
	
	public void validateLieferung(Lieferung lieferung, Locale locale, Class<?>... groups) {
		final Validator validator = validatorProvider.getValidator(locale);

		final Set<ConstraintViolation<Lieferung>> violations = validator.validate(lieferung, groups);
		if (!violations.isEmpty()) {
			throw new LieferungValidationException(lieferung, violations);
		}
	}
	
	public void validateBestellposition(Bestellposition bestellposition, Locale locale, Class<?>... groups) {
		final Validator validator = validatorProvider.getValidator(locale);

		final Set<ConstraintViolation<Bestellposition>> violations = validator.validate(bestellposition, groups);
		if (!violations.isEmpty()) {
			throw new BestellpositionValidationException(bestellposition, violations);
		}
	}
}
