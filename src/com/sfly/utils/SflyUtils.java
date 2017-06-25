package com.sfly.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.sfly.model.SflyEvent;

/**
 * @author Sobhan Kumar Padhi
 *
 */
public final class SflyUtils {

	public static Date getStartDateOfWeek(String eventDate)
	{
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Date weekStart = null;
		try {
			Date currDate = df.parse(eventDate);
			Calendar cal = Calendar.getInstance();			
			cal.setTime(currDate);

			int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - cal.getFirstDayOfWeek();
			cal.add(Calendar.DAY_OF_MONTH, -dayOfWeek);
			weekStart = cal.getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return weekStart;
	}


	public static Date convertDateFromString(String eventDate, String format)
	{
		SimpleDateFormat df = null;
		Date currDate = null;

		if(format == null)
			//df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
			df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		else
			df = new SimpleDateFormat(format);

		try {
			currDate = df.parse(eventDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return currDate;
	}

	public static String convertStringFromDate(Date eventDate, String format)
	{
		SimpleDateFormat df = null;
		String currDate = null;

		if(format == null)
			df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		else
			df = new SimpleDateFormat(format);

		currDate = df.format(eventDate);
		return currDate;
	}


	public static double getTotalAmountInDouble(String strTotalAmount)
	{
		String totalAmount = strTotalAmount.split("USD")[0].trim();
		return Double.parseDouble(totalAmount);
	}


	public static StringBuffer readFile(String path) throws IOException
	{
		FileInputStream fs = new FileInputStream(path);

		BufferedReader buf= new BufferedReader(new InputStreamReader(fs));
		StringBuffer strBuf = new StringBuffer();
		String line;
		while((line = buf.readLine()) != null)
		{
			strBuf.append(line);
		}
		buf.close();

		return strBuf;
	}

	public static void writeFile(String data, String path)
	{
		FileOutputStream fs;
		try {
			fs = new FileOutputStream(path);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fs));
			bw.write(data);
			bw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static List<String> getFilesInFolder(String path) throws IOException
	{
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();
		List<String> fileList = new ArrayList<String>();

		if(listOfFiles != null)
		{
			for (int i = 0; i < listOfFiles.length; i++) {
				File file = listOfFiles[i];
				String fileName = file.getName();
				if (file.isFile() && fileName.endsWith(".txt")) {
					fileList.add(fileName);
				}
			}
		}
		return fileList;
	}


	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue( Map<K, V> dataMap )
	{
		List<Map.Entry<K, V>> mapDataList = new LinkedList<Map.Entry<K, V>>( dataMap.entrySet() );
		Collections.sort( mapDataList, new Comparator<Map.Entry<K, V>>()
		{
			public int compare( Map.Entry<K, V> obj1, Map.Entry<K, V> obj2 )
			{
				return (obj2.getValue()).compareTo( obj1.getValue() );
			}
		} );

		Map<K, V> sortedMap = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : mapDataList)
		{
			sortedMap.put( entry.getKey(), entry.getValue() );
		}
		return sortedMap;
	}


	public static void  serialize(HashMap<String, SflyEvent> dataMap, String filename){
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try {
			fos = new FileOutputStream(filename);
			out = new ObjectOutputStream(fos);
			out.writeObject(dataMap);
			out.close();
			//System.out.println("Serialization complete");
		} catch (IOException ex) {
			ex.printStackTrace();
		}


	}

	@SuppressWarnings("unchecked")
	public static HashMap<String, SflyEvent>  deserialize(String filename){
		HashMap<String, SflyEvent>  eventMap= null;
		FileInputStream fis = null;
		ObjectInputStream in = null;

		try {

			File f = new File(filename);
			if(f.exists()) { 
				fis = new FileInputStream(filename);
				in = new ObjectInputStream(fis);
				eventMap = (HashMap<String, SflyEvent>) in.readObject();
				in.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		return eventMap;
	}
}