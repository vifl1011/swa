package de.shop.artikelverwaltung.service;

import java.util.Collection;

import javax.ejb.ApplicationException;
import javax.validation.ConstraintViolation;

import de.shop.artikelverwaltung.domain.Produkt;
import de.shop.util.AbstractShopExceptionFactory;

@ApplicationException(rollback = true)
public class InvalidProduktBezeichnungException extends AbstractShopExceptionFactory {

	private static final long serialVersionUID = -1807312604250425008L;
	private final String bez;
	private final Collection<ConstraintViolation<Produkt>> violations;

	public InvalidProduktBezeichnungException(String bez, Collection<ConstraintViolation<Produkt>> violations) {
		super("Ungueltige Bezeichnung: " + bez + ", Violations: " + violations);
		this.bez = bez;
		this.violations = violations;
	}
	
	public InvalidProduktBezeichnungException(String bez) {
		super("Ungueltige Bezeichnung: " + bez);
		this.bez = bez;
		this.violations = null;
	}

	public String getBez() {
		return bez;
	}

	public Collection<ConstraintViolation<Produkt>> getViolations() {
		return violations;
	}
}