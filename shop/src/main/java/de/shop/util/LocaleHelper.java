package de.shop.util;

import java.util.List;
import java.util.Locale;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.HttpHeaders;

@ApplicationScoped
public class LocaleHelper {
	public Locale getLocale(HttpHeaders headers) {
		final List<Locale> locales = headers.getAcceptableLanguages();
		final Locale locale = locales.isEmpty() ? null : locales.get(0);
		return locale;
	}
}
