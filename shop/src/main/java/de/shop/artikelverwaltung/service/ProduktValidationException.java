package de.shop.artikelverwaltung.service;

import java.util.Collection;

import javax.validation.ConstraintViolation;

import de.shop.artikelverwaltung.domain.Produkt;


/**
 * Exception, die ausgel&ouml;st wird, wenn die Attributwerte eines Artikels nicht korrekt sind
 */
public class ProduktValidationException extends AbstractProduktServiceException {
	private static final long serialVersionUID = 4255133082483647701L;
	private final Produkt produkt;
	private final Collection<ConstraintViolation<Produkt>> violations;

	public ProduktValidationException(Produkt produkt,
			                          Collection<ConstraintViolation<Produkt>> violations) {
		super("Ungueltiges Produkt: " + produkt + ", Violations: " + violations);
		this.produkt = produkt;
		this.violations = violations;
	}

	public Produkt getProdukt() {
		return produkt;
	}

	public Collection<ConstraintViolation<Produkt>> getViolations() {
		return violations;
	}
}
