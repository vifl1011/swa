package de.shop.auth.controller;

import javax.enterprise.context.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import de.shop.auth.service.jboss.AuthService.RolleType;

@FacesConverter("RollenConverter")
@RequestScoped
public class RollenConverter implements Converter {
	@Override
	public Object getAsObject(FacesContext ctx, UIComponent comp, String str) {
		final RolleType rolle = RolleType.valueOf(str);
		return rolle;
	}

	@Override
	public String getAsString(FacesContext ctx, UIComponent comp, Object obj) {
		if (obj == null)
			return "";
		
		final RolleType rolle = (RolleType) obj;
		return rolle.name();
	}
}
