package de.shop.artikelverwaltung.service;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class BezeichnungExistsException extends ProduktServiceException {
	private static final long serialVersionUID = -8746809017779123646L;
	private final String bezeichnung;
	
	public BezeichnungExistsException(String bezeichnung) {
		super("Die Bezeichnung " + bezeichnung + " existiert bereits");
		this.bezeichnung = bezeichnung;
	}

	public String getBezeichnung() {
		return bezeichnung;
	}
}
