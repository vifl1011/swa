package de.shop.bestellverwaltung.service;

import java.util.Collection;

import javax.ejb.ApplicationException;
import javax.validation.ConstraintViolation;

import de.shop.bestellverwaltung.domain.Bestellposition;

@ApplicationException(rollback = true)
public class BestellpositionValidationException extends BestellpositionServiceException {
	private static final long serialVersionUID = 4255133082483647701L;
	private final Bestellposition bestellposition;
	private final Collection<ConstraintViolation<Bestellposition>> violations;
	

	public BestellpositionValidationException(Bestellposition bestellposition,
			                        Collection<ConstraintViolation<Bestellposition>> violations)
	{
		super("Ungueltige Bestellung: " + bestellposition + ", Violations: " + violations);
		this.bestellposition = bestellposition;
		this.violations = violations;
	}

	public Bestellposition getBestellposition() {
		return this.bestellposition;
	}

	public Collection<ConstraintViolation<Bestellposition>> getViolations() {
		return violations;
	}
}

