package de.shop.artikelverwaltung.service;

import java.io.Serializable;

import static java.util.logging.Level.FINER;

import java.lang.invoke.MethodHandles;

import javax.annotation.PostConstruct;
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

import de.shop.artikelverwaltung.domain.Produkt;
import de.shop.util.Constants;
import de.shop.util.Log;
import de.shop.util.ValidatorProvider;

@Log
public class ProduktService implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3450368544721631368L;
	
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	@PersistenceContext
	private transient EntityManager em;
	
	@Inject
	private ValidatorProvider validatorProvider;
	
	@PostConstruct
	private void postConstruct() {
		LOGGER.log(FINER, "CDI-faehiges Bean {0} wurde erzeugt", this);
	}	
	
	@Log
	public List<Produkt> findProdukte() {
		final List<Produkt> produkte = em.createNamedQuery(Produkt.FIND_PRODUKTE, Produkt.class)
				                        .getResultList();
		return produkte;
	}
	
	@Log
	public Produkt findProduktById(Long id, Locale locale) {
		try {
			final Produkt produkt = em.createNamedQuery(Produkt.FIND_PRODUKT_BY_ID, Produkt.class)
					.setParameter(Produkt.PARAM_ID, id).getSingleResult();
			return produkt;
		}
		catch (NoResultException e) {
			return null;
		}
	}
	
	
	@Log
	public List<Produkt> findAlleProdukte() {
		List<Produkt> produkte = null;
		produkte = em.createNamedQuery(Produkt.FIND_PRODUKTE, Produkt.class)
	             .getResultList();
		return produkte;
	}
	
	@Log
	public Produkt findProduktByIdEm(Long id) {
		return em.find(Produkt.class, id);
	}
	
	
	@Log
	private void validateBezeichnung(String bez, Locale locale) {
		//erstellen einer Validator Instanz in der Sprache locale
		final Validator validator = validatorProvider.getValidator(locale);
		final Set<ConstraintViolation<Produkt>> violations = validator.validateValue(Produkt.class,
				                                                                           "bezeichnung",
				                                                                           bez,
				                                                                           Default.class);
		if (!violations.isEmpty())
			throw new InvalidProduktBezeichnungException(bez, violations);
	}
	
	@Log
	public Produkt createProdukt(Produkt produkt, Locale locale) {
		if (produkt == null) {
			return null;
		}
		// Validierung
		this.validateBezeichnung(produkt.getBezeichnung(), locale);
		
		produkt.setId(Constants.KEINE_ID);
		em.persist(produkt);
		return produkt;	
	}
	
	@Log
	public Produkt updateProdukt(Produkt produkt, Locale locale) {
		LOGGER.finest("updateProdukt BEGIN undso");
		if (produkt == null) {
			return null;
		}
				
		this.validateBezeichnung(produkt.getBezeichnung(), locale);
		
		em.detach(produkt);
		Produkt aProdukt = findProduktById(produkt.getId(), locale);
		em.detach(aProdukt);
		
		if (produkt.getBezeichnung().matches("[0-9]+")) {
			throw new InvalidProduktBezeichnungException(produkt.getBezeichnung());			
		}	
		em.merge(produkt);
		
		return produkt;
	}
	
	public List<Produkt> ladenhueter(int anzahl) {
		final List<Produkt> produkt = em.createNamedQuery(Produkt.FIND_LADENHUETER, Produkt.class)
				                        .setMaxResults(anzahl)
				                        .getResultList();
		return produkt;
	}
	
}