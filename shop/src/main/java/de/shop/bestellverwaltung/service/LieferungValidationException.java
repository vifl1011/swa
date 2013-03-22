package de.shop.bestellverwaltung.service;

import java.util.Collection;

import javax.ejb.ApplicationException;
import javax.validation.ConstraintViolation;

import de.shop.bestellverwaltung.domain.Lieferung;

@ApplicationException(rollback = true)
public class LieferungValidationException extends LieferungServiceException {
	private static final long serialVersionUID = 4255133082483647701L;
	private final Lieferung lieferung;
	private final Collection<ConstraintViolation<Lieferung>> violations;
	

	public LieferungValidationException(Lieferung lieferung,
			                        Collection<ConstraintViolation<Lieferung>> violations)
	{
		super("Ungueltige Bestellung: " + lieferung + ", Violations: " + violations);
		this.lieferung = lieferung;
		this.violations = violations;
	}

	public Lieferung getBestellung() {
		return this.lieferung;
	}

	public Collection<ConstraintViolation<Lieferung>> getViolations() {
		return violations;
	}
}