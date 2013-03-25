package de.shop.util;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.hibernate.validator.internal.engine.resolver.JPATraversableResolver;
import org.jboss.logging.Logger;


/*
 * Bei Verwendung von JSF werden die Attribute bereits in der Praesentationsschicht validiert.
 */
@ApplicationScoped
public class ValidatorProvider implements Serializable {
	private static final long serialVersionUID = 7886864531128694923L;
	
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private Config config;
	
	private transient HashMap<Locale, Validator> validators;
	private Locale defaultLocale;
	
	@PostConstruct
	private void init() {
		defaultLocale = config.getDefaultLocale();
		
		final List<Locale> locales = config.getLocales();
		final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
		final JPATraversableResolver jpaTraversableResolver = new JPATraversableResolver();  // Spez. Kap. 4.4.3

		validators = new HashMap<>(locales.size(), 1);  // Anzahl Elemente bei Fuellgrad 1.0
		for (Locale locale : locales) {
			final MessageInterpolator interpolator = validatorFactory.getMessageInterpolator();
			final MessageInterpolator localeInterpolator =
				                      new LocaleSpecificMessageInterpolator(interpolator,
                                                                            locale);
			final Validator validator = validatorFactory.usingContext()
                                                        .messageInterpolator(localeInterpolator)
                                                        .traversableResolver(jpaTraversableResolver)
                                                        .getValidator();
			validators.put(locale, validator);
		}

		if (validators.keySet() == null || validators.keySet().isEmpty()) {
			LOGGER.error("Es sind keine Sprachen eingetragen");
			return;
		}
		LOGGER.infof("Locales fuer die Fehlermeldungen bei Bean Validation: %s", validators.keySet());
	}
	
	/*
	 * JSF liefert Locale durch FacesContext.getCurrentInstance().getViewRoot().getLocale();
	 * JAX-RS liefert List<Locale> durch HttpHeaders.getAcceptableLanguages() mit absteigenden Prioritaeten.
	 */
	public Validator getValidator(Locale locale) {
		if (locale == null) {
			return validators.get(defaultLocale);
		}
		
		Validator validator = validators.get(locale);
		if (validator != null) {
			return validator;
		}
			
		if (!locale.getCountry().isEmpty()) {
			// z.B. de_DE
			locale = new Locale(locale.getLanguage());  // z.B. de_DE -> de
			validator = validators.get(locale);
			if (validator != null) {
				return validator;
			}
		}

		return validators.get(defaultLocale);
	}

	
	/**
	 * http://hibernate.org/~emmanuel/validation
	 * 
	 * ResourceBundle.getBundle(String, Locale, ClassLoader)
	 * ResourceBundleMessageInterpolator.loadBundle(ClassLoader, Locale, String) line: 206	
	 * ResourceBundleMessageInterpolator.getFileBasedResourceBundle(Locale) line: 177	
	 * ResourceBundleMessageInterpolator.findUserResourceBundle(Locale) line: 284	
	 * ResourceBundleMessageInterpolator.interpolateMessage(String, Map<String,Object>, Locale) line: 123	
	 * ResourceBundleMessageInterpolator.interpolate(String, MessageInterpolator$Context, Locale) line: 101	
	 * ValidatorProvider$LocaleSpecificMessageInterpolator.interpolate(String, MessageInterpolator$Context) line: 100	
	 */
	public static class LocaleSpecificMessageInterpolator implements MessageInterpolator {
		private final MessageInterpolator interpolator;
		private final Locale locale;

		public LocaleSpecificMessageInterpolator(MessageInterpolator interpolator, Locale locale) {
			this.locale = locale;
			this.interpolator = interpolator;
		}

		@Override
		public String interpolate(String message, Context context) {
			final String resultMessage = interpolator.interpolate(message, context, locale);
			return resultMessage;
		}

		@Override
		public String interpolate(String message, Context context, Locale loc) {
			return interpolator.interpolate(message, context, loc);
		}
	}
}
