package de.shop.bestellverwaltung.service;

import de.shop.util.AbstractShopExceptionFactory;

public abstract class AbstractLieferungServiceExceptionFactory extends AbstractShopExceptionFactory {
	private static final long serialVersionUID = -2849585609393128387L;

	public AbstractLieferungServiceExceptionFactory(String msg) {
		super(msg);
	}
	
	public AbstractLieferungServiceExceptionFactory(String msg, Throwable t) {
		super(msg, t);
	}
}
