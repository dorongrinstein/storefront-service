package com.sjsu.storefront.common;

@SuppressWarnings("serial")
public class ResourceNotFoundException extends Exception{

	public ResourceNotFoundException(String message) {
		super(message);
	}
}
