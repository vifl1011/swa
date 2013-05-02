package de.shop.util;

import java.io.Serializable;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;


@Named("sprache")
@SessionScoped
@Log
public class SpracheListener implements Serializable {
	private static final long serialVersionUID = 1986565724093259408L;
	
	@Produces
	@Named
	@Client
	private Locale locale;
	
	@Inject
	private transient FacesContext ctx;
	
	@Inject
	private transient ExternalContext externalCtx;
	
	@PostConstruct
	private void init() {
		locale = externalCtx.getRequestLocale();
	}

	public void change(String localeStr) {
		final Locale newLocale = new Locale(localeStr);
		if (newLocale.equals(locale)) {
			return;
		}
		
		locale = newLocale;
		ctx.getViewRoot().setLocale(locale);
		ctx.renderResponse();
	}
	
//    @Produces
//    @Faces
//    public Locale getLocale() {
//    	final UIViewRoot viewRoot = ctx.getViewRoot();
//        return viewRoot != null
//        	   ? viewRoot.getLocale()
//        	   : ctx.getApplication().getViewHandler().calculateLocale(ctx);
//    }
	
	@Override
	public String toString() {
		return "SpracheController [locale=" + locale + "]";
	}
}
