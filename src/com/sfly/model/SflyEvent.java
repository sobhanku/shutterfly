package com.sfly.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

/**
 * @author Sobhan Kumar Padhi
 *
 */

public class SflyEvent implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5127710435954824272L;

	public int totalVisits;
	public double totalExpenditure;
	public Set<Date> weekStartDates;
	public HashMap<String, String> event;


	public int getTotalVisits() {
		return totalVisits;
	}


	public void setTotalVisits(int totalVisits) {
		this.totalVisits = totalVisits;
	}


	public double getTotalExpenditure() {
		return totalExpenditure;
	}


	public void setTotalExpenditure(double totalExpenditure) {
		this.totalExpenditure = totalExpenditure;
	}


	public Set<Date> getWeekStartDates() {
		return weekStartDates;
	}


	public void setWeekStartDates(Set<Date> weekStartDates) {
		this.weekStartDates = weekStartDates;
	}


	public HashMap<String, String> getEvent() {
		return event;
	}


	public void setEvent(HashMap<String, String> event) {
		this.event = event;
	}


	@Override
	public String toString() {
		return "SflyEvent [totalVisits=" + totalVisits + ", totalExpenditure=" + totalExpenditure + ", weekStartDates="
				+ weekStartDates + ", event=" + event + "]";
	}	
}
