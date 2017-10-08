package com.uber.coding_challange.food_trucks.ctrl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uber.coding_challange.food_trucks.dataaccess.FoodTrackAccessor;
import com.uber.coding_challange.food_trucks.model.FoodTrack;

/**
 * The client class. Singleton pattern is used. 
 * Responsible for establishing a connection with DataSF API and obtaining the food track data. 
 * It is initialized only once (with lazy initialization); 
 * therefore, the connection is established only once.
 * 
 * @author alper.karacelik
 *
 */
public class FoodTrackClient 
{
	// Constants ---------------------------------------------------------
	private static final String DATASF_URL = "https://data.sfgov.org/resource/6a9r-agq8.json";
	// -------------------------------------------------------------------
	
	// Attributes --------------------------------------------------------
	private static boolean initialized = false;
	// -------------------------------------------------------------------
	
	// SINGLETON Implementation ------------------------------------------
	private static FoodTrackClient INSTANCE = new FoodTrackClient();
	private FoodTrackClient() {}
	public static FoodTrackClient getInstance() {return INSTANCE;}
	// -------------------------------------------------------------------
	
	/**
	 * Uses the public API provided by DataSF, 
	 * Obtains all the food track data
	 * Pushes the obtained data to the Food Track Accessor (the storage handler)
	 * Uses lazy initialization. 
	 * This method is called after the first request to the web service.
	 * Other calls will simply be ignored.
	 * No two different threads can access this method at the same time.
	 */
	public void initialize()
	{
		synchronized (FoodTrackAccessor.class) 
		{
			// Continue if not already initialized.
			if (! initialized)
			{
				try 
				{
					URL url = new URL(DATASF_URL);
					ObjectMapper jsonMapper = new ObjectMapper();
					
					// Obtain the food tracks
					List<FoodTrack> foodTrackList = 
							jsonMapper.readValue(url, new TypeReference<List<FoodTrack>>(){});
					
					// Add the received food tracks to our storage
					for (FoodTrack foodTrack : foodTrackList)
					{
						FoodTrackAccessor.getInstance().addFoodTrack(foodTrack);
					}
					
					// set initialized flag true
					initialized = true;
				} 
				catch (MalformedURLException e) 
				{
					e.printStackTrace();
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				}
			}
		}
	}
}
