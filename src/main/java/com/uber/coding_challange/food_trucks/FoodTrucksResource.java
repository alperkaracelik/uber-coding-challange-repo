package com.uber.coding_challange.food_trucks;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.uber.coding_challange.food_trucks.ctrl.FoodTruckClient;
import com.uber.coding_challange.food_trucks.dataaccess.FoodTruckAccessor;
import com.uber.coding_challange.food_trucks.model.FoodTruck;
import com.uber.coding_challange.food_trucks.model.enums.DistanceUnitEnum;
import com.uber.coding_challange.food_trucks.model.enums.FoodTruckStatusEnum;

/**
 * Root resource (exposed at "foodtrucks" path)
 */
@Path("foodtrucks")
/**
 * This class provides the entry point for the web service by providing two different GET methods.
 * 1) getFoodTrucks(): Returns all the food trucks.
 * 2) getFoodTrucksByQuery(): Returns the food trucks that provides the given query conditions.
 * 
 * @author alper.karacelik
 *
 */
public class FoodTrucksResource 
{	
	// Constructor
	public FoodTrucksResource()
	{
		FoodTruckClient.getInstance().initialize();
	}
	
    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "application/json" media type.
     *
     * @return Food Trucks in JSON format
     */
    @GET	
    @Produces(MediaType.APPLICATION_JSON)
    public List<FoodTruck> getFoodTrucks() 
    {
    	return FoodTruckAccessor.getInstance().getAllFoodTrucks();
    }
    
    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "application/json" media type.
     *
     * @return Food Trucks in JSON format
     */
    @GET
    @Path("/query")
    @Produces(MediaType.APPLICATION_JSON)
    public List<FoodTruck> getFoodTrucksByQuery(
    		@QueryParam("status") String statusStr,
            @QueryParam("latitude") String latitudeStr,
            @QueryParam("longitude") String longitudeStr,
            @QueryParam("radius") String radiusStr,
            @QueryParam("radius_unit") String radiusUnitStr)
    {
    	// Query Results
    	List<FoodTruck> queryResult = null;
    	
    	// Food truck status
		FoodTruckStatusEnum statusEnum = FoodTruckStatusEnum.ALL;
		
    	// If status is specified
    	if (statusStr != null)
    	{
    		// Obtain the status
    		statusEnum = FoodTruckStatusEnum.getFromStringValue(statusStr);
    	}
    	
    	// Update the query results by querying on 'status'
		queryResult = FoodTruckAccessor.getInstance().getFoodTrucks(statusEnum);
    	
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
        		queryResult = FoodTruckAccessor.getInstance().getFoodTrucks(queryResult, latitude, longitude, radius, radiusUnit);
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
