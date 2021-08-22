package tests;

import org.testng.annotations.Test;

import static io.restassured.RestAssured.*;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import java.util.Calendar;
import java.util.Date;

// Assumptions:
// 1. 16-days daily forecast api is not available for Free subscribers. So, /forecast used instead for this code challenge.
// 2. As the response has forecast data for every 3 hours interval for each day, I've decided to pick main.temp_min value at 10:00 am to check if the day is suitable.
// 3. Maximum forecast records returned for /forecast is 40, which approximately for 6 days. So, I was unable to check 16-day forecast.

//Enhancements:
// Property files are used to store api end point and parameter details. 

public class OpenWeather {
	static String baseURIVal = "https://api.openweathermap.org/data/2.5";
	static String cityName = "Sydney";
	static String units = "metric";
	static String countDays = "40";
	static String apiKey = "04a3b45248ef3933d1f8f9ad7d6dcd55";
	static int referenceHour = 10;
	static int suitableMinTemp = 10;

	@Test
	public void checkWeatherSuitable(){
		baseURI = baseURIVal;
		
		Response response = 
		given()
			.pathParam("city name", cityName)
			.pathParam("units", units)
			.pathParam("cnt", countDays)
			.pathParam("API key", apiKey)
		.when()
			.get("/forecast?q={city name}&units={units}&cnt={cnt}&appid={API key}");
		response
		.then()
		.statusCode(200).log().all();

		checkResponse(response);
	}
	
	public void checkResponse(Response response) {
		JsonPath extractor = response.jsonPath();
		String strRecCount = extractor.getString("cnt");
		int intRecCount = Integer.parseInt(strRecCount);
		
		int matchCount = 0;

		for (int i = 0; i < intRecCount; i++) {
			String forecastDateUnix = extractor.getString("list[" + i + "].dt");
			String strForecastTemp = extractor.getString("list[" + i + "].main.temp_min");
			double dblForecastMinTemp = Double.parseDouble(strForecastTemp);

			Date forecastDateTime = new Date((long) Integer.parseInt(forecastDateUnix) * 1000);
			Calendar c = Calendar.getInstance();
			c.setTime(forecastDateTime);
			int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
			int hourTime = c.get(Calendar.HOUR_OF_DAY);
			
			if (dayOfWeek == Calendar.THURSDAY && hourTime == referenceHour && dblForecastMinTemp > suitableMinTemp) {
				if (matchCount == 0) {
					System.out.println("-----------------------------------------------------------------------");
					System.out.println("Sydney forecast for Thursdays with temperature > " + suitableMinTemp + " degree C");
					System.out.println("-----------------------------------------------------------------------");
				}
				
				String dateMatched = c.get(Calendar.DATE) + "/" + c.get(Calendar.MONTH) + "/" + c.get(Calendar.YEAR);
				System.out.println("Date: " + dateMatched + ", Forecast (min temperature at 10:00 am): " + dblForecastMinTemp + " degree C");
				matchCount++;
			}
		}
		
		if (matchCount == 0) {
			System.out.println("-----------------------------------------------------------------------");
			System.out.println("No Thursdays with temperature (forecast) > " + suitableMinTemp + " degree C found for Sydney");
			System.out.println("-----------------------------------------------------------------------");
		} else {
			System.out.println("-----------------------------------------------------------------------");
		}
	}
}
