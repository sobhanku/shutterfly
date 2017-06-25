package com.sfly.app;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.joda.time.DateTime;
import org.joda.time.Weeks;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sfly.model.SflyEvent;
import com.sfly.utils.SflyUtils;

/**
 * @author Sobhan Kumar Padhi
 *
 */

public class SflyIngestionService {

	private final String SFLY_EVENT_ITEM_KEY = "key";
	private final String SFLY_EVENT_ITEM_TYPE = "type";
	private final String SFLY_EVENT_ITEM_VERB = "verb";
	private final String SFLY_EVENT_ITEM_LAST_NAME = "last_name";
	private final String SFLY_EVENT_ITEM_ADDR_CITY = "addr_city";
	private final String SFLY_EVENT_ITEM_ADDR_STATE = "addr_state";
	private final String SFLY_EVENT_ITEM_EVENT_TIME = "event_time";
	private final String SFLY_EVENT_ITEM_CUSTOMER_ID = "customer_id";
	private final String SFLY_EVENT_ITEM_TOTAL_AMOUNT = "total_amount";

	private final String SFLY_EVENT_TYPE_ORDER = "ORDER";
	private final String SFLY_EVENT_TYPE_CUSTOMER = "CUSTOMER";
	private final String SFLY_EVENT_TYPE_SITE_VISIT = "SITE_VISIT";

	private HashSet<Date> gDateList = new HashSet<Date>();


	/**
	 * Ingests single JSON event (e) into the data structure (D).
	 * 
	 * @param event
	 * @param dataMap
	 */
	public void ingest(JSONObject event, HashMap<String, SflyEvent> dataMap) {

		try {
			insertEvents(event, dataMap);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}				
	}

	/**
	 * Calculates the Customer Lifetime Value (LTV) for all customers who have order history and displays the top X customers based on 
	 * individual start week and global max week. For instance customer A has orders for week 1 and week 2 and and customer B has orders for week 2 and week 3,
	 * then the customer A weeks-count will be from Week 1 to Week 3 and for customer B it will be week 2 to week 3, i.e. 3 weeks for customer A and
	 * 2 weeks for customer B.  
	 * 
	 * @param top
	 * @param dataMap
	 * @return
	 */
	@SuppressWarnings("unused")
	public String topXSimpleLTVCustomers(int top, HashMap<String, SflyEvent> dataMap)
	{

		Map<String,Double> customerLTV = new TreeMap<String, Double>();

		Date genDate = Collections.max(gDateList);
		DateTime maxDate = new DateTime(genDate);

		for (Map.Entry<String, SflyEvent> entry : dataMap.entrySet())
		{
			String custID = entry.getKey();
			SflyEvent data = entry.getValue();
			Date userDate = null;
			Set<Date> weekDates = data.weekStartDates;
			if(weekDates != null)
				userDate = Collections.min(data.weekStartDates);

			DateTime minDate = new DateTime(userDate);
			int countWeeks = Weeks.weeksBetween(minDate, maxDate).getWeeks();

			if(custID.equals("0mdbgya6dxz0"))
				System.out.println(minDate + " " + maxDate);
			if(countWeeks < 1 && data.weekStartDates != null)
			{
				countWeeks = data.weekStartDates.size();
			}
			if(countWeeks > 0)
			{
				double visitsPerWeek = (double)data.totalVisits/countWeeks; //Unused in the revenue calculation, but calculated as required.
				double expPerVisit = data.totalExpenditure/data.totalVisits; //Unused in the revenue calculation, but calculated as required.

				double avgCustomerValue = data.totalExpenditure / countWeeks;
				double cltv = 52 * avgCustomerValue * 10;

				customerLTV.put(custID, cltv);
			}
		}

		Map<String,Double> sortedCLTV = SflyUtils.sortByValue(customerLTV);

		StringBuffer buf = new StringBuffer();
		int count = 1;
		for(Map.Entry<String, Double> entry : sortedCLTV.entrySet())
		{	
			if(count > top)
				break;
			String custID = entry.getKey();
			double sortedCltv = entry.getValue();
			buf.append(custID + "," + sortedCltv);
			buf.append("\n");
			System.out.println(custID + ", " + sortedCltv);
			count++;
		}


		String ltvOutput = buf.substring(0, buf.length()-1).toString();
		return ltvOutput;
	}



	/**
	 * @param event
	 * @param dataMap
	 * @throws ParseException
	 */
	private void insertEvents(JSONObject event, HashMap<String, SflyEvent> dataMap) throws ParseException {

		String type = event.get(SFLY_EVENT_ITEM_TYPE).toString();
		String id = event.get(SFLY_EVENT_ITEM_KEY).toString();
		String eventDate = event.get(SFLY_EVENT_ITEM_EVENT_TIME).toString();
		String customerID = type.equals(SFLY_EVENT_TYPE_CUSTOMER)?event.get(SFLY_EVENT_ITEM_KEY).toString():event.get(SFLY_EVENT_ITEM_CUSTOMER_ID).toString();
		String dataKey = type + ":" + id;

		if(!dataMap.containsKey(customerID))
		{

			SflyEvent eventObj = new SflyEvent();

			HashMap<String, String> eventData = new HashMap<String, String>();

			if(type.equals(SFLY_EVENT_TYPE_CUSTOMER))
			{
				eventData = new HashMap<String, String>();
				eventData.put(dataKey, event.toString());
				eventObj.event = eventData;

				dataMap.put(customerID, eventObj);	
			}
			else
			{
				if(type.equals(SFLY_EVENT_TYPE_ORDER))
				{
					Set<Date> dateList = new HashSet<Date>();	

					double totalAmount = SflyUtils.getTotalAmountInDouble(event.get(SFLY_EVENT_ITEM_TOTAL_AMOUNT).toString());
					eventObj.totalExpenditure = totalAmount;
					eventObj.totalVisits = 0;
					dateList.add(SflyUtils.getStartDateOfWeek(eventDate));
					eventObj.weekStartDates = dateList;

					if(this.gDateList != null)
						this.gDateList.addAll(dateList);
					else
						this.gDateList = (HashSet<Date>)dateList;
				}
				else if(type.equals(SFLY_EVENT_TYPE_SITE_VISIT))
				{
					eventObj.totalExpenditure = 0;
					eventObj.totalVisits = 1;

				}
				else
				{
					eventObj.totalVisits = 0;
					eventObj.totalExpenditure = 0;
				}
				eventData = new HashMap<String, String>();
				eventData.put(dataKey, event.toString());

				eventObj.event = eventData;				
				dataMap.put(customerID, eventObj);
			}
		}
		else
		{
			SflyEvent eventObj = dataMap.get(customerID);
			HashMap<String, String> eventData = eventObj.event;


			if(type.equals(SFLY_EVENT_TYPE_CUSTOMER))
			{
				if(!eventData.containsKey(dataKey))
				{
					eventData.put(dataKey, event.toString());
					eventObj.event = eventData;
					dataMap.put(customerID, eventObj);	
				}
				else
				{
					String oldEvent = eventData.get(dataKey);
					JSONObject oldJsonEvent  = (JSONObject) new JSONParser().parse(oldEvent);
					Date OldDate = SflyUtils.convertDateFromString(oldJsonEvent.get(SFLY_EVENT_ITEM_EVENT_TIME).toString(), null);
					Date currDate = SflyUtils.convertDateFromString(event.get(SFLY_EVENT_ITEM_EVENT_TIME).toString(), null);

					if(currDate.after(OldDate))
					{
						String updatedDetails = updateCustomerDetails(oldJsonEvent, event);

						eventData.put(dataKey, updatedDetails);					
					}
					else
					{
						eventData.put("CUSTOMERbackup:" + id, event.toString());
					}

					eventObj.event = eventData;
					dataMap.put(customerID, eventObj);
				}
			}
			else
			{
				if(type.equals(SFLY_EVENT_TYPE_ORDER))
				{
					Set<Date> dateList = eventObj.weekStartDates==null?new HashSet<Date>():eventObj.weekStartDates;
					dateList.add(SflyUtils.getStartDateOfWeek(eventDate));
					eventObj.weekStartDates = dateList;

					if(this.gDateList != null)
						this.gDateList.addAll(dateList);
					else
						this.gDateList = (HashSet<Date>)dateList;

					double totalAmount = SflyUtils.getTotalAmountInDouble(event.get(SFLY_EVENT_ITEM_TOTAL_AMOUNT).toString());

					if(eventData.containsKey(dataKey))
					{
						String oldEvent = eventData.get(dataKey);
						JSONObject oldJsonOrder  = (JSONObject) new JSONParser().parse(oldEvent);


						Date OldDate = SflyUtils.convertDateFromString(oldJsonOrder.get(SFLY_EVENT_ITEM_EVENT_TIME).toString(), null);
						Date currDate = SflyUtils.convertDateFromString(event.get(SFLY_EVENT_ITEM_EVENT_TIME).toString(), null);

						if(currDate.after(OldDate))
						{
							double oldTotalAmount = SflyUtils.getTotalAmountInDouble(oldJsonOrder.get(SFLY_EVENT_ITEM_TOTAL_AMOUNT).toString());
							double diffAmount = totalAmount - oldTotalAmount;
							eventObj.totalExpenditure += diffAmount;

							String orderUpdate = updateOrderDetails(oldJsonOrder, event);
							eventData.put(dataKey, orderUpdate);																 
						}
						else
						{
							eventData.put("ORDERbackup:" + id, event.toString());
						}
					}
					else
					{		
						eventData.put(dataKey, event.toString());
						eventObj.totalExpenditure += totalAmount;						
					}

					eventObj.event = eventData;						
					dataMap.put(customerID, eventObj);
				}
				else
				{
					if(type.equals(SFLY_EVENT_TYPE_SITE_VISIT))
					{
						if(eventData.containsKey(dataKey))
						{
							JSONObject oldEvent  = (JSONObject) new JSONParser().parse(eventData.get(dataKey));
							Date OldDate = SflyUtils.convertDateFromString(oldEvent.get(SFLY_EVENT_ITEM_EVENT_TIME).toString(), null);
							Date currDate = SflyUtils.convertDateFromString(event.get(SFLY_EVENT_ITEM_EVENT_TIME).toString(), null);

							if(currDate.after(OldDate))
								eventObj.totalVisits += 1;
						}
						else
							eventObj.totalVisits += 1;

					}
					eventData.put(dataKey, event.toString());
					eventObj.event = eventData;
					dataMap.put(customerID, eventObj);		
				}
			}
		}
	}


	/**
	 * Alternate Solution - Calculates the Customer Lifetime Value (LTV) for all customers who have order history and displays the top X customers. 
	 * The number of weeks is based on the individual customers. For instance a customer has orders placed in Week 1, 2 and 5. Then the count of weeks 
	 * will be 3 weeks for him.
	 * 
	 * @param top
	 * @param dataMap
	 * @return
	 */
	@SuppressWarnings("unused")
	public String topXSimpleLTVCustomersAlt(int top, HashMap<String, SflyEvent> dataMap){

		Map<String,Double> customerLTV = new TreeMap<String, Double>();

		for (Map.Entry<String, SflyEvent> entry : dataMap.entrySet())
		{
			String custID = entry.getKey();
			SflyEvent data = entry.getValue();

			int countWeeks = data.weekStartDates!=null?data.weekStartDates.size():data.totalVisits;	

			if(countWeeks > 0)
			{
				double visitsPerWeek = data.totalVisits/countWeeks; //Unused in the revenue calculation, but calculated as required.
				double expPerVisit = data.totalExpenditure/data.totalVisits; //Unused in the revenue calculation, but calculated as required.

				double avgCustomerValue = data.totalExpenditure / countWeeks;

				double cltv = 52 * avgCustomerValue * 10;
				customerLTV.put(custID, cltv);
			}
		}

		Map<String,Double> sortedCLTV = SflyUtils.sortByValue(customerLTV);

		StringBuffer buf = new StringBuffer();
		int count = 1;
		System.out.println("");
		for(Map.Entry<String, Double> entry : sortedCLTV.entrySet())
		{	
			String custID = entry.getKey();
			double custLTV = entry.getValue();
			if(count > top)
				break;

			buf.append(custID + "," + custLTV);
			buf.append("\n");
			System.out.println(custID + ", " + custLTV);
			count++;
		}
		String ltvOutput = buf.substring(0, buf.length()-1).toString();
		return ltvOutput;
	}


	/**
	 * Fills the dateList with the week's first dates of all orders placed. This is needed ONLY when the serialization is enabled.
	 * 
	 * @param dataMap
	 */
	public void setgDateList(HashMap<String, SflyEvent> dataMap) {

		HashSet<Date> dateList = new HashSet<Date>();
		for (Map.Entry<String, SflyEvent> entry : dataMap.entrySet())
		{
			SflyEvent data = entry.getValue();

			Set<Date> weekDates = data.weekStartDates; 
			if(weekDates != null)
			{
				dateList.addAll(weekDates);
			}
		}

		this.gDateList = dateList;
	}

	/**
	 * Updates old event data with the updated customer event details.
	 * 
	 * @param oldJsonEvent
	 * @param event
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private String updateCustomerDetails(JSONObject oldJsonEvent, JSONObject event)
	{

		if(event.containsKey(SFLY_EVENT_ITEM_ADDR_STATE) && !event.get(SFLY_EVENT_ITEM_ADDR_STATE).equals(oldJsonEvent.get(SFLY_EVENT_ITEM_ADDR_STATE)))
		{
			oldJsonEvent.put(SFLY_EVENT_ITEM_ADDR_STATE, event.get(SFLY_EVENT_ITEM_ADDR_STATE));
		}

		if(event.containsKey(SFLY_EVENT_ITEM_LAST_NAME) && !event.get(SFLY_EVENT_ITEM_LAST_NAME).equals(oldJsonEvent.get(SFLY_EVENT_ITEM_LAST_NAME)))
		{
			oldJsonEvent.put(SFLY_EVENT_ITEM_LAST_NAME, event.get(SFLY_EVENT_ITEM_LAST_NAME));
		}

		if(event.containsKey(SFLY_EVENT_ITEM_ADDR_CITY) && !event.get(SFLY_EVENT_ITEM_ADDR_CITY).equals(oldJsonEvent.get(SFLY_EVENT_ITEM_ADDR_CITY)))
		{
			oldJsonEvent.put(SFLY_EVENT_ITEM_ADDR_CITY, event.get(SFLY_EVENT_ITEM_ADDR_CITY));
		}

		if(event.containsKey(SFLY_EVENT_ITEM_VERB) && !event.get(SFLY_EVENT_ITEM_VERB).equals(oldJsonEvent.get(SFLY_EVENT_ITEM_VERB)))
		{
			oldJsonEvent.put(SFLY_EVENT_ITEM_VERB, event.get(SFLY_EVENT_ITEM_VERB));
		}

		oldJsonEvent.put(SFLY_EVENT_ITEM_EVENT_TIME, event.get(SFLY_EVENT_ITEM_EVENT_TIME));
		return oldJsonEvent.toString();
	}

	/**
	 * Updates old event data with the updated order event details.
	 * 
	 * @param oldJsonEvent
	 * @param event
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private String updateOrderDetails(JSONObject oldJsonEvent, JSONObject event)
	{

		if(event.containsKey(SFLY_EVENT_ITEM_TOTAL_AMOUNT) && !event.get(SFLY_EVENT_ITEM_TOTAL_AMOUNT).equals(oldJsonEvent.get(SFLY_EVENT_ITEM_TOTAL_AMOUNT)))
		{
			oldJsonEvent.put(SFLY_EVENT_ITEM_TOTAL_AMOUNT, event.get(SFLY_EVENT_ITEM_TOTAL_AMOUNT));
		}

		if(event.containsKey(SFLY_EVENT_ITEM_CUSTOMER_ID) && !event.get(SFLY_EVENT_ITEM_CUSTOMER_ID).equals(oldJsonEvent.get(SFLY_EVENT_ITEM_CUSTOMER_ID)))
		{
			oldJsonEvent.put(SFLY_EVENT_ITEM_CUSTOMER_ID, event.get(SFLY_EVENT_ITEM_CUSTOMER_ID));
		}

		if(event.containsKey(SFLY_EVENT_ITEM_VERB) && !event.get(SFLY_EVENT_ITEM_VERB).equals(oldJsonEvent.get(SFLY_EVENT_ITEM_VERB)))
		{
			oldJsonEvent.put(SFLY_EVENT_ITEM_VERB, event.get(SFLY_EVENT_ITEM_VERB));
		}

		oldJsonEvent.put(SFLY_EVENT_ITEM_EVENT_TIME, event.get(SFLY_EVENT_ITEM_EVENT_TIME));
		return oldJsonEvent.toString();
	}

	public HashSet<Date> getgDateList() {
		return gDateList;
	}

	public void addToDateList(Date date) {
		this.gDateList.add(date);
	}
}