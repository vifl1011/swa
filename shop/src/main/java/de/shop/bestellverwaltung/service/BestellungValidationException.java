package de.shop.bestellverwaltung.service;

import java.util.Collection;

import javax.ejb.ApplicationException;
import javax.validation.ConstraintViolation;

import de.shop.bestellverwaltung.domain.Bestellung;

@ApplicationException(rollback = true)
public class BestellungValidationException extends BestellServiceException {
	private static final long serialVersionUID = 4255133082483647701L;
	private final Bestellung bestellung;
	private final Collection<ConstraintViolation<Bestellung>> violations;
	

	public BestellungValidationException(Bestellung bestellung,
			                        Collection<ConstraintViolation<Bestellung>> violations)
	{
		super("Ungueltige Bestellung: " + bestellung + ", Violations: " + violations);
		this.bestellung = bestellung;
		this.violations = violations;
	}

	public Bestellung getBestellung() {
		return this.bestellung;
	}

	public Collection<ConstraintViolation<Bestellung>> getViolations() {
		return violations;
	}
}
