package de.shop.artikelverwaltung.service;

import java.util.Collection;

import javax.ejb.ApplicationException;
import javax.validation.ConstraintViolation;

import de.shop.artikelverwaltung.domain.Produkt;

@ApplicationException(rollback = true)
public class InvalidProduktIdException extends AbstractProduktServiceException {
	private static final long serialVersionUID = 3197321473062978823L;
	
	private final Long produktId;
	private final Collection<ConstraintViolation<Produkt>> violations;
	
	public InvalidProduktIdException(Long produktId, Collection<ConstraintViolation<Produkt>> violations) {
		super("Ungueltige Produkt-ID: " + produktId + ", Violations: " + violations);
		this.produktId = produktId;
		this.violations = violations;
	}

	public Long getproduktId() {
		return produktId;
	}

	public Collection<ConstraintViolation<Produkt>> getViolations() {
		return violations;
	}
}
