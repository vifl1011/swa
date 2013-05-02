package de.shop.util;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import static javax.faces.application.FacesMessage.SEVERITY_WARN;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;

import org.jboss.logging.Logger;

@RequestScoped
@Log
public class Messages implements Serializable {
	private static final long serialVersionUID = -2209093106110666329L;
	
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());

	private static final String PREFIX_PACKAGES = "de.shop.";
	private static final String SUFFIX = ".controller.messages";

	public enum MessagesType implements Serializable {
		SHOP(PREFIX_PACKAGES + "messages"),
		KUNDENVERWALTUNG(PREFIX_PACKAGES + "kundenverwaltung" + SUFFIX),
		BESTELLVERWALTUNG(PREFIX_PACKAGES + "bestellverwaltung" + SUFFIX),
		ARTIKELVERWALTUNG(PREFIX_PACKAGES + "artikelverwaltung" + SUFFIX),
		AUTH(PREFIX_PACKAGES + "auth" + SUFFIX);
		
		private String value;
		
		MessagesType(String value) {
			this.value = value;
		}
	}
	
	@Inject
	private Config config;
	
	@Inject
	private transient FacesContext ctx;
	
	@Inject
	@Client
	private Locale locale;
	
	// z.B. "de" als Schluessel auch fuer de_DE
	private transient Map<MessagesType, Map<String, ResourceBundle>> bundles;

	@PostConstruct
	private void postConstruct() {
		bundles = new HashMap<>();
		
		final MessagesType[] messagesValues = MessagesType.values();
		final List<Locale> locales = config.getLocales();
		
		for (MessagesType messagesType : messagesValues) {
			final Map<String, ResourceBundle> bundleMap = new HashMap<>();
			bundles.put(messagesType, bundleMap);

			for (Locale lc : locales) {
				final ResourceBundle bundle = ResourceBundle.getBundle(messagesType.value, lc);
				bundleMap.put(lc.getLanguage(), bundle);				
			}
		}

		LOGGER.debugf("Messages wurde erzeugt: %s", this);
	}
	
	/**
	 * Fuer Fehlermeldungen an der Web-Oberflaeche, die durch Exceptions verursacht werden
	 */
	public void error(MessagesType messagesType, String msgKey, String idUiKomponente, Object... args) {
		createMsg(messagesType, msgKey, idUiKomponente, SEVERITY_ERROR, args);
	}
	
	public void warn(MessagesType messagesType, String msgKey, String idUiKomponente, Object... args) {
		createMsg(messagesType, msgKey, idUiKomponente, SEVERITY_WARN, args);
	}
	
	public void info(MessagesType messagesType, String msgKey, String idUiKomponente, Object... args) {
		createMsg(messagesType, msgKey, idUiKomponente, SEVERITY_INFO, args);
	}
	
	private void createMsg(MessagesType messagesType,
			               String msgKey,
			               String idUiKomponente,
			               Severity severity,
			               Object... args) {
		final Map<String, ResourceBundle> bundleMap = bundles.get(messagesType);
		ResourceBundle bundle = bundleMap.get(locale.getLanguage());
		if (bundle == null) {
			// Keine Texte zu aktuellen Sprache gefunden: Default-Sprache verwenden
			bundle = bundleMap.get(config.getDefaultLocale().getLanguage());
		}
		final String msgPattern = bundle.getString(msgKey);
		final MessageFormat formatter = new MessageFormat(msgPattern);
		final String msg = formatter.format(args);
		
		final FacesMessage facesMsg = new FacesMessage(severity, msg, null);
		ctx.addMessage(idUiKomponente, facesMsg);
	}


	/**
	 * Fuer Fehlermeldungen an der Web-Oberflaeche, falls die Meldung durch Bean Validation
	 * bereits gegeben ist, z.B. bei Validierung in Suchmasken oder bei @assertTrue
	 */
	public <T> void error(Collection<ConstraintViolation<T>> violations, String idUiKomponente) {
		createMsg(violations, idUiKomponente, SEVERITY_ERROR);
	}

	public <T> void warn(Collection<ConstraintViolation<T>> violations, String idUiKomponente) {
		createMsg(violations, idUiKomponente, SEVERITY_WARN);
	}

	private <T> void createMsg(Collection<ConstraintViolation<T>> violations,
			                   String idUiKomponente,
			                   Severity severity) {
		for (ConstraintViolation<T> v: violations) {
			LOGGER.trace(v);
			final FacesMessage facesMsg = new FacesMessage(severity, v.getMessage(), null);
			ctx.addMessage(idUiKomponente, facesMsg);
		}
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("Messages [ bundles= {");
		for (MessagesType mt : bundles.keySet()) {
			sb.append(mt);
			sb.append(" : {");
			final Map<String, ResourceBundle> bundleMap = bundles.get(mt);
			for (String localeStr : bundleMap.keySet()) {
				sb.append(localeStr);
				sb.append(", ");
			}
			final int length = sb.length();
			sb.delete(length - 2, length - 1);
			sb.append("}, ");
		}
		
		final int length = sb.length();
		sb.delete(length - 2, length - 1);
		sb.append('}');
		
		return sb.toString();
	}
}
