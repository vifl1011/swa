package de.shop.bestellverwaltung.service;

import de.shop.util.ShopException;

public abstract class AbstractBestellServiceExceptionFactory extends ShopException {
	private static final long serialVersionUID = -2849585609393128387L;

	public AbstractBestellServiceExceptionFactory(String msg) {
		super(msg);
	}
	
	public AbstractBestellServiceExceptionFactory(String msg, Throwable t) {
		super(msg, t);
	}
}
