package de.shop.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Datum als String (z.B. 2000-01-31) uebertragen
 */
public class XmlDateAdapter extends XmlAdapter<String, Date> {
	private final DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()); 
	
	@Override
	public Date unmarshal(String date) throws ParseException {
		return formatter.parse(date);
	}
	
	@Override
	public String marshal(Date date)  {
		return formatter.format(date);
	}
}
