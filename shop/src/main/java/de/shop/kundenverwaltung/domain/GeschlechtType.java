package de.shop.kundenverwaltung.domain;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlValue;

@XmlEnum
public enum GeschlechtType {
	@XmlValue
	MAENNLICH,
	WEIBLICH;
}
