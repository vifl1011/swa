package de.shop.artikelverwaltung.service;

import de.shop.util.AbstractShopException;

public abstract class ProduktServiceException extends AbstractShopException {
	private static final long serialVersionUID = -2849585609393128387L;

	public ProduktServiceException(String msg) {
		super(msg);
	}
	
	public ProduktServiceException(String msg, Throwable t) {
		super(msg, t);
	}
}
