package de.shop.bestellverwaltung.service;

import de.shop.util.ShopException;

public abstract class BestellServiceException extends ShopException {
	private static final long serialVersionUID = -2849585609393128387L;

	public BestellServiceException(String msg) {
		super(msg);
	}
	
	public BestellServiceException(String msg, Throwable t) {
		super(msg, t);
	}
}
