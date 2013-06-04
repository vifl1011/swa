package de.shop.kundenverwaltung.service;
import javax.ejb.ApplicationException;

import de.shop.kundenverwaltung.domain.Kunde;

@ApplicationException(rollback = true)
public class KundeCreateAdresseException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private final Kunde kunde;
	public KundeCreateAdresseException(Kunde kunde) {
		super("Kunde " + kunde + "hat keine Adresse");
		this.kunde = kunde;
	}
	public Kunde getKunde() {
		return kunde;
	}
}
