package de.shop.kundenverwaltung.service;

import de.shop.util.AbstractShopException;

public abstract class AbstractKundeServiceExceptionFactory extends AbstractShopException {
	private static final long serialVersionUID = -2849585609393128387L;

	public AbstractKundeServiceExceptionFactory(String msg) {
		super(msg);
	}
	
	public AbstractKundeServiceExceptionFactory(String msg, Throwable t) {
		super(msg, t);
	}
}
