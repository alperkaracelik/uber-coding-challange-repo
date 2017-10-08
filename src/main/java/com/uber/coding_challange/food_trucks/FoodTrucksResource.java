package com.uber.coding_challange.food_trucks;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.uber.coding_challange.food_trucks.ctrl.FoodTrackClient;
import com.uber.coding_challange.food_trucks.dataaccess.FoodTrackAccessor;
import com.uber.coding_challange.food_trucks.model.FoodTrack;
import com.uber.coding_challange.food_trucks.model.enums.DistanceUnitEnum;
import com.uber.coding_challange.food_trucks.model.enums.FoodTrackStatusEnum;

/**
 * Root resource (exposed at "foodtracks" path)
 */
@Path("foodtracks")
/**
 * This class provides the entry point for the web service by providing two different GET methods.
 * 1) getFoodTracks(): Returns all the food tracks.
 * 2) getFoodTracksByQuery(): Returns the food tracks that provides the given query conditions.
 * 
 * @author alper.karacelik
 *
 */
public class FoodTrucksResource 
{	
	// Constructor
	public FoodTrucksResource()
	{
		FoodTrackClient.getInstance().initialize();
	}
	
    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "application/json" media type.
     *
     * @return Food Tracks in JSON format
     */
    @GET	
    @Produces(MediaType.APPLICATION_JSON)
    public List<FoodTrack> getFoodTracks() 
    {
    	return FoodTrackAccessor.getInstance().getAllFoodTracks();
    }
    
    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "application/json" media type.
     *
     * @return Food Tracks in JSON format
     */
    @GET
    @Path("/query")
    @Produces(MediaType.APPLICATION_JSON)
    public List<FoodTrack> getFoodTracksByQuery(
    		@QueryParam("status") String statusStr,
            @QueryParam("latitude") String latitudeStr,
            @QueryParam("longitude") String longitudeStr,
            @QueryParam("radius") String radiusStr,
            @QueryParam("radius_unit") String radiusUnitStr)
    {
    	// Query Results
    	List<FoodTrack> queryResult = null;
    	
    	// Food track status
		FoodTrackStatusEnum statusEnum = FoodTrackStatusEnum.ALL;
		
    	// If status is specified
    	if (statusStr != null)
    	{
    		// Obtain the status
    		statusEnum = FoodTrackStatusEnum.getFromStringValue(statusStr);
    	}
    	
    	// Update the query results by querying on 'status'
		queryResult = FoodTrackAccessor.getInstance().getFoodTracks(statusEnum);
    	
    	// If latitude and longitude is specified
    	if (latitudeStr != null && longitudeStr != null && radiusStr != null && radiusUnitStr != null)
    	{
    		try 
    		{
    			// Obtain the latitude, longitude, radius and radius unit
        		double latitude = Double.parseDouble(latitudeStr);
        		double longitude = Double.parseDouble(longitudeStr);
        		double radius = Double.parseDouble(radiusStr);
        		DistanceUnitEnum radiusUnit = DistanceUnitEnum.getFromStringValue(radiusUnitStr); 
        		
        		// Update the query results 
        		queryResult = FoodTrackAccessor.getInstance().getFoodTracks(queryResult, latitude, longitude, radius, radiusUnit);
			} 
    		catch (Exception e) 
    		{
				e.printStackTrace();
			}
    	}
    	
    	// Return the query result
    	return queryResult;
    }
}
