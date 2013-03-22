package de.shop.kundenverwaltung.service;

import java.util.Collection;

import javax.ejb.ApplicationException;
import javax.validation.ConstraintViolation;

import de.shop.kundenverwaltung.domain.Kunde;


/**
 * Exception, die ausgel&ouml;st wird, wenn die Attributwerte eines Kunden nicht korrekt sind
 */
@ApplicationException(rollback = true)
public class KundeValidationException extends KundeServiceException {
	private static final long serialVersionUID = 4255133082483647701L;
	private final Kunde kunde;
	private final Collection<ConstraintViolation<Kunde>> violations;
	

	public KundeValidationException(Kunde kunde,
			                        Collection<ConstraintViolation<Kunde>> violations) {
		super("Ungueltiger Kunde: " + kunde + ", Violations: " + violations);
		this.kunde = kunde;
		this.violations = violations;
	}

	public Kunde getKunde() {
		return kunde;
	}

	public Collection<ConstraintViolation<Kunde>> getViolations() {
		return violations;
	}
}
