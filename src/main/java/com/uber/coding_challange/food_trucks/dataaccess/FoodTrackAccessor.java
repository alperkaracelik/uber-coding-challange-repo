package com.uber.coding_challange.food_trucks.dataaccess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.uber.coding_challange.food_trucks.ctrl.GeodesicDistanceCalculator;
import com.uber.coding_challange.food_trucks.model.FoodTrack;
import com.uber.coding_challange.food_trucks.model.enums.DistanceUnitEnum;
import com.uber.coding_challange.food_trucks.model.enums.FoodTrackStatusEnum;

/**
 * Storage accessor class. Singleton pattern is used.
 * Allows to add/update/delete food tracks.
 * Maintains a food track status -> food track list map for faster query results.
 * 
 * @author alper.karacelik
 *
 */
public class FoodTrackAccessor 
{
	// Attributes --------------------------------------------------------
	private static HashMap<Long, FoodTrack> foodTruckMap;
	private static HashMap<FoodTrackStatusEnum, List<FoodTrack>> statusMap;
	// -------------------------------------------------------------------
	
	// SINGLETON Implementation ------------------------------------------
	private static FoodTrackAccessor INSTANCE = new FoodTrackAccessor();
	private FoodTrackAccessor() {initialize();}
	public static FoodTrackAccessor getInstance() {return INSTANCE;}
	// -------------------------------------------------------------------
	
	/**
	 * Initializes the mappings
	 */
	private static void initialize()
	{
		foodTruckMap = new HashMap<Long, FoodTrack>();
		statusMap = new HashMap<FoodTrackStatusEnum, List<FoodTrack>>();
	}
	
	/**
	 * Returns all the food tracks in the storage
	 * This method is synchronized on this class (Manipulation on storage elements is prevented.)
	 * 
	 * @return All the food tracks in the storage
	 */
	public List<FoodTrack> getAllFoodTracks()
	{
		synchronized (FoodTrackAccessor.class) 
		{
			return new ArrayList<FoodTrack>(foodTruckMap.values());
		}
	}
	
	/**
	 * Returns the food tracks that have the specified status.
	 * If the specified status is ALL, then all food tracks are returned.
	 * If the specified status is NO_STATEMENT, then an empty list is returned.
	 * This method is synchronized on this class (Manipulation on storage elements is prevented.)
	 * 
	 * @param status Food Track Status for querying
	 * @return The food tracks which have the specified status
	 */
	public List<FoodTrack>getFoodTracks(FoodTrackStatusEnum status)
	{
		synchronized (FoodTrackAccessor.class) 
		{
			if (status == FoodTrackStatusEnum.ALL)
			{
				return getAllFoodTracks();
			}
			else if (status == FoodTrackStatusEnum.NO_STATEMENT)
			{
				return new ArrayList<FoodTrack>();
			}
			else
			{
				return statusMap.get(status);
			}
		}
	}
	
	/**
	 * Returns the food tracks that reside in the specified circle.
	 * Center of the circle: ['latitude', 'longitude'], 
	 * Radius of the circle: 'radius', Unit of the radius: 'radiusUnit'
	 * This method is synchronized on this class (Manipulation on storage elements is prevented.)
	 * 
	 * @param foodTracks List that contains tracks that will be checked
	 * @param latitude Latitude of the center of the circle
	 * @param longitude Longitude of the center of the circle
	 * @param radius Radius of the circle
	 * @param radiusUnit Unit of the radius
	 * @return The food tracks that reside in the specified circle.
	 */
	public List<FoodTrack>getFoodTracks(
			List<FoodTrack> foodTracks, 
			double latitude, 
			double longitude, 
			double radius,
			DistanceUnitEnum radiusUnit)
	{
		synchronized (FoodTrackAccessor.class) 
		{
			// Initialize the result list
			List<FoodTrack> foodTracksInsideCircle = new ArrayList<FoodTrack>();
			
			// Traverse through the food tracks
			for (FoodTrack foodTrack:foodTracks)
			{
				// Calculate the distance between current food track and the center of the circle
				double distance = 
						GeodesicDistanceCalculator.getInstance().distance(
								foodTrack.getLatitude(), foodTrack.getLongitude(),
								latitude, longitude, radiusUnit);
				
				// If distance is smaller than the radius,
				if (distance < radius)
				{
					// Then it is in the circle, add it to the result list.
					foodTracksInsideCircle.add(foodTrack);
				}
			}
			
			// Return the resulting list.
			return foodTracksInsideCircle;
		}
	}
	
	/**
	 * Returns true if a food track with given id exist, false otherwise
	 * This method is synchronized on this class (Manipulation on storage elements is prevented.)
	 * 
	 * @param id Object id of a food track
	 * @return True if a food track with given id exist, false otherwise
	 */
	public boolean foodTrackExist(long id)
	{
		synchronized (FoodTrackAccessor.class) 
		{
			return foodTruckMap.containsKey(id);
		}
	}
	
	/**
	 * Returns the food track with the given id
	 * This method is synchronized on this class (Manipulation on storage elements is prevented.)
	 * 
	 * @param id Object id of a food track
	 * @return The food track with the given id
	 */
	public FoodTrack getFoodTrack(long id)
	{
		synchronized (FoodTrackAccessor.class) 
		{
			return foodTruckMap.get(id);
		}
	}
	
	/**
	 * Adds the given food track to the storage
	 * Also populates the status map
	 * This method is synchronized on this class (Manipulation on storage elements is prevented.)
	 * 
	 * @param foodTrack new food track
	 */
	public void addFoodTrack(FoodTrack foodTrack)
	{
		synchronized (FoodTrackAccessor.class) 
		{
			foodTruckMap.put(foodTrack.getObjectid(), foodTrack);
			addToStatusMap(foodTrack);
		}
	}

	/**
	 * Updates the given food track. 
	 * If there no food track with the object id of the given food track, then does nothing.
	 * Also update the status map if the status is changed.
	 * This method is synchronized on this class (Manipulation on storage elements is prevented.)
	 * 
	 * @param foodTrack updated food track
	 */
	public void updateFoodTrack(FoodTrack foodTrack)
	{
		synchronized (FoodTrackAccessor.class) 
		{
			if (foodTruckMap.containsKey(foodTrack.getObjectid()))
			{
				// Update the status map first.
				updateStatusChange(foodTrack);
				foodTruckMap.put(foodTrack.getObjectid(), foodTrack);
			}
		}
	}
	
	/**
	 * Removes and returns the food track with the given id.
	 * Also updates the status map
	 * This method is synchronized on this class (Manipulation on storage elements is prevented.)
	 * 
	 * @param id
	 * @return The removed food track
	 */
	public FoodTrack removeFoodTrack(long id)
	{
		synchronized (FoodTrackAccessor.class) 
		{
			removeFromStatusMap(foodTruckMap.get(id));
			return foodTruckMap.remove(id);
		}
	}
	
	/**
	 * Adds the given food track to the related list in the status map.
	 * Example: If the status of the given food track is 'REQUESTED';
	 * then, the given track is added to the list of the 'REQUESTED' map item.
	 * This method is synchronized on this class (Manipulation on storage elements is prevented.)
	 * 
	 * @param foodTrack new food track
	 */
	private void addToStatusMap(FoodTrack foodTrack)
	{
		synchronized (FoodTrackAccessor.class) 
		{
			FoodTrackStatusEnum status = foodTrack.getStatusEnum();
			List<FoodTrack> foodTracks;
			
			// If an existing entry will be updated.
			if (statusMap.containsKey(status))
			{
				foodTracks = statusMap.get(status);
			}
			// If a new entry is created.
			else
			{
				foodTracks = new ArrayList<FoodTrack>();
				statusMap.put(status, foodTracks);
			}
			
			foodTracks.add(foodTrack);
		}
	}
	
	/**
	 * Updates the status map if the status of the given food track is changed.
	 * This method is synchronized on this class (Manipulation on storage elements is prevented.)
	 * 
	 * @param updatedFoodTrack Updated food track
	 */
	private void updateStatusChange(FoodTrack updatedFoodTrack)
	{
		synchronized (FoodTrackAccessor.class) 
		{
			// Continue only if the given food track already exist
			if (foodTruckMap.containsKey(updatedFoodTrack.getObjectid()))
			{
				FoodTrack existingFoodTrack = foodTruckMap.get(updatedFoodTrack.getObjectid());
				
				// Check if the status is changed.
				if (existingFoodTrack.getStatusEnum() != updatedFoodTrack.getStatusEnum())
				{
					// Remove the food track from the map of previous status
					removeFromStatusMap(existingFoodTrack);
					// Add the food track to the map of current status
					addToStatusMap(updatedFoodTrack);
				}
			}
		}
	}
	
	/**
	 * Updates the status map by removing the given food track from the related status map item.
	 * Example: If the status of the given food track is 'REQUESTED';
	 * then, the given track is from the list of the 'REQUESTED' map item.
	 * This method is synchronized on this class (Manipulation on storage elements is prevented.)
	 * 
	 * @param foodTrack Removed food track
	 */
	private void removeFromStatusMap(FoodTrack foodTrack)
	{
		synchronized (FoodTrackAccessor.class) 
		{
			// Continue only if the given food track already exist
			if (foodTruckMap.containsKey(foodTrack.getObjectid()))
			{
				if (statusMap.containsKey(foodTrack.getStatusEnum()))
				{
					statusMap.get(foodTrack.getStatusEnum()).remove(foodTrack);
				}
			}
		}
	}
}
