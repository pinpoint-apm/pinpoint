package com.nhn.pinpoint.testweb.domain;

import java.util.Date;

public class Member {

	int id;

	String name;

	Date joined;

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the joined
	 */
	public Date getJoined() {
		return joined;
	}

	/**
	 * @param joined
	 *            the joined to set
	 */
	public void setJoined(Date joined) {
		this.joined = joined;
	}

}
