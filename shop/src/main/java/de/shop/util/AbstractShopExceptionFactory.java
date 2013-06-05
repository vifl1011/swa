package de.shop.util;

public abstract class AbstractShopExceptionFactory extends RuntimeException {
	private static final long serialVersionUID = -1030863258479949134L;

	public AbstractShopExceptionFactory(String msg) {
		super(msg);
	}

	public AbstractShopExceptionFactory(String msg, Throwable t) {
		super(msg, t);
	}
}
