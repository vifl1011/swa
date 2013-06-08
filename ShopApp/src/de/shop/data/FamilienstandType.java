package de.shop.data;

public enum FamilienstandType {
	LEDIG(0),
	VERHEIRATET(1),
	GESCHIEDEN(2),
	VERWITWET(3);
	
	private int value;
	
	private FamilienstandType(int value) {
		this.value = value;
	}
	
	public static FamilienstandType valueOf(int value) {
		switch (value) {
			case 0:
				return LEDIG;
			case 1:
				return VERHEIRATET;
			case 2:
				return GESCHIEDEN;
			case 3:
				return VERWITWET;
			default:
				return null;
		}
	}
	
	public int value() {
		return value;
	}
}
