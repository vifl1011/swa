package de.shop.bestellverwaltung.service;

import de.shop.util.ShopException;

public abstract class BestellpositionServiceException extends ShopException {
	private static final long serialVersionUID = -2849585609393128387L;

	public BestellpositionServiceException(String msg) {
		super(msg);
	}
	
	public BestellpositionServiceException(String msg, Throwable t) {
		super(msg, t);
	}
}