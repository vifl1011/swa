package de.shop.bestellverwaltung.service;

import de.shop.util.AbstractShopExceptionFactory;

public abstract class AbstractBestellServiceExceptionFactory extends AbstractShopExceptionFactory {
	private static final long serialVersionUID = -2849585609393128387L;

	public AbstractBestellServiceExceptionFactory(String msg) {
		super(msg);
	}
	
	public AbstractBestellServiceExceptionFactory(String msg, Throwable t) {
		super(msg, t);
	}
}
