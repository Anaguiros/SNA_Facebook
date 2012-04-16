package dataSerialisation;

import java.io.IOException;
import java.io.StringWriter;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class DataJsonParser {

	public static String getJsonFromData(Data data){
		ObjectMapper mapper = new ObjectMapper();

		StringWriter writer = new StringWriter() ;
		try {
			mapper.writeValue(writer, data) ;
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return writer.toString() ;
	}
	
	public static Data getDataFromJson(String json){
		ObjectMapper mapper = new ObjectMapper();
		Data data = null ;
		
		try {
			data = mapper.readValue(json, Data.class) ;
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return data ; // return null si �a �choue
	}
}
