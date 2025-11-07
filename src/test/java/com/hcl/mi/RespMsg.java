package com.hcl.mi;

import lombok.Getter;

@Getter
public class RespMsg {

	private Object key;
	private Object value;
	private Integer statusCode;

	public RespMsg(Object key, Object value, Integer statusCode) {
		this.key = key;
		this.value = value;
		this.statusCode = statusCode;
	}
}
