package de.shop.kundenverwaltung.service;

import java.util.Collection;

import javax.ejb.ApplicationException;
import javax.validation.ConstraintViolation;

import de.shop.kundenverwaltung.domain.Kunde;

@ApplicationException(rollback = true)
public class InvalidKundeEmailException extends AbstractKundeServiceExceptionFactory {
	private static final long serialVersionUID = -8973151010781329074L;
	
	private final String email;
	private final Collection<ConstraintViolation<Kunde>> violations;
	
	public InvalidKundeEmailException(String email, Collection<ConstraintViolation<Kunde>> violations) {
		super("Ungueltige EMail: " + email + ", Violations: " + violations);
		this.email = email;
		this.violations = violations;
	}

	public String getEmail() {
		return email;
	}

	public Collection<ConstraintViolation<Kunde>> getViolations() {
		return violations;
	}
}
