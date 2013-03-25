package de.shop.util;


public class JsonFile {
	private byte[] bytes;

	public JsonFile() {
		super();
	}
	
	public JsonFile(byte[] bytes) {
		super();
		this.bytes = bytes;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}

	@Override
	public String toString() {
		return "JsonFile [size=" + bytes.length + "]";
	}
}
