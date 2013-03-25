package de.shop.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;


@ApplicationScoped
public class Config implements Serializable {
	private static final long serialVersionUID = 3916523726340426731L;
	
	// In META-INF\ejb-jar.xml kann der Wert gesetzt bzw. ueberschrieben werden
	@Resource(name = "absenderMail")
	private String absenderMail;
	
	@Resource(name = "absenderName")
	private String absenderName;
	
	@Resource(name = "empfaengerMail")
	private String empfaengerMail;
	
	@Resource(name = "empfaengerName")
	private String empfaengerName;

	@Resource(name = "locales")
	private String localesStr;
	
	private List<Locale> locales;
	private Locale defaultLocale = Locale.GERMAN;

	@PostConstruct
	private void init() {
		if (Strings.isNullOrEmpty(localesStr)) {
			locales = Lists.newArrayList(defaultLocale);
			return;
		}
		
	    final Iterable<String> tokens = Splitter.on(',')
	    		                                .trimResults()
	    		                                .omitEmptyStrings()
	    		                                .split(localesStr);
	    locales = new ArrayList<>();
	    for (String token : tokens) {
	    	locales.add(new Locale(token));
	    }
	    
	    if (locales != null && !locales.isEmpty()) {
	    	defaultLocale = locales.get(0);
		}
	}    

	public List<Locale> getLocales() {
		return locales;
	}

	public void setLocales(List<Locale> locales) {
		this.locales = locales;
	}

	public Locale getDefaultLocale() {
		return defaultLocale;
	}

	public void setDefaultLocale(Locale defaultLocale) {
		this.defaultLocale = defaultLocale;
	}

	public String getAbsenderMail() {
		return absenderMail;
	}

	public void setAbsenderMail(String absenderMail) {
		this.absenderMail = absenderMail;
	}

	public String getAbsenderName() {
		return absenderName;
	}

	public void setAbsendeNamer(String absenderName) {
		this.absenderName = absenderName;
	}

	public String getEmpfaengerMail() {
		return empfaengerMail;
	}

	public void setEmpfaengerMail(String empfaengerMail) {
		this.empfaengerMail = empfaengerMail;
	}

	public String getEmpfaengerName() {
		return empfaengerName;
	}

	public void setEmpfaengerName(String empfaengerName) {
		this.empfaengerName = empfaengerName;
	}

	@Override
	public String toString() {
		return "Config [locales=" + locales + ", defaultLocale=" + defaultLocale + ", absenderMail=" + absenderMail
				+ ", absenderName=" + absenderName + ", empfaengerMail="
				+ empfaengerMail + ", empfaengerName=" + empfaengerName + "]";
	}
}
