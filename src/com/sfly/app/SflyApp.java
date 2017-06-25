package com.sfly.app;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sfly.model.SflyEvent;
import com.sfly.utils.SflyUtils;

/**
 * @author Sobhan Kumar Padhi
 *
 */

public class SflyApp {

	public static void main(String[] args) {

		HashMap<String, SflyEvent> dataMap = null;
		boolean enableSerialization = false;
		int top = 5;
		String serializedFile = "./output/persistDB.dat";
		String outputFile = "./output/output.txt";
		String path = "./input";

		try
		{
			ArrayList<String> files = (ArrayList<String>) SflyUtils.getFilesInFolder(path);
			SflyIngestionService ingestor = new SflyIngestionService();

			if(enableSerialization)
			{
				dataMap = SflyUtils.deserialize(serializedFile);				
				dataMap = dataMap == null?new HashMap<String, SflyEvent>():dataMap;

				ingestor.setgDateList(dataMap);
			}
			else
				dataMap = new HashMap<String, SflyEvent>();


			for(String file: files)
			{

				StringBuffer strBuf = SflyUtils.readFile(path + "/" + file);

				JSONParser parser = new JSONParser();
				JSONArray jsonData = (JSONArray) parser.parse(strBuf.toString());

				for(Object event: jsonData)
				{
					//System.out.println(((JSONObject)event).toString());
					ingestor.ingest((JSONObject)event, dataMap);
				}
			}	
			if(enableSerialization)
				SflyUtils.serialize(dataMap, serializedFile);

			String ltvOutput = ingestor.topXSimpleLTVCustomers(top, dataMap);
			//ingestor.topXSimpleLTVCustomersAlt(top, dataMap); //LTV Calculation with Alternative solution
			SflyUtils.writeFile(ltvOutput, outputFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
