/*
 *By: QC
 *Last modified: 3/16/2018 
 */
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.lang.*;

import javax.print.attribute.standard.RequestingUserName;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;

public class GeneralSearch {
	public static JSONObject search(String pageNumStr, String indexType, String keywords, String location,
			String orderType) {
		JSONParser parser = new JSONParser();
		System.out.println("keywords");
		System.out.println(keywords);
		JSONObject indexSearchResult = new JSONObject();
		JSONObject indexSearchRestaurants = new JSONObject();
		JSONObject respResult = new JSONObject();
		JSONObject indexSearchRestOrdered = new JSONObject();
		JSONObject recommends = new JSONObject();
		Integer pageNum = Integer.valueOf(pageNumStr);
		if (indexType.equals("Lucene")) {
			indexSearchResult = LuceneSearch.search(keywords);
		} else {
			indexSearchResult = HadoopSearch.search(keywords);
		}
		try {
			indexSearchRestaurants = (JSONObject) parser.parse(indexSearchResult.get("results").toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		////////////////////////////////////////////////////////
		// re-order
		indexSearchRestOrdered = reorder(orderType, indexSearchRestaurants, location, indexType);
		// set Page
		JSONObject respPageRest = new JSONObject();
		for (Integer i = 0; i < 10; i++) {
			Integer curRest = (pageNum - 1) * 10 + i;
			// respPageRest.put(curRest.toString(),
			// indexSearchRestOrdered.get(curRest.toString()));

			// change comment to Menu
			JSONObject restWithMenu = new JSONObject();
			String menu = "";
			try {
				restWithMenu = (JSONObject) parser.parse(indexSearchRestOrdered.get(curRest.toString()).toString());
				String comments = restWithMenu.get("Comment").toString();
				// System.out.println(comments);
				menu = getMenu(comments);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			restWithMenu.remove("Comment");
			restWithMenu.put("Menu", menu);
			respPageRest.put(curRest.toString(), restWithMenu);

		}
		// get recommend
		// recommends = recommend(respPageRest);
		for (Integer i = 0; i < 5; i++) {
			Integer recID = i *5+ (pageNum) * 10 + 20;
			// recommends.put(recID.toString(),
			// indexSearchRestOrdered.get(recID.toString()));
			// change comment to Menu
			JSONObject restWithMenu = new JSONObject();
			String menu = "";
			try {
				restWithMenu = (JSONObject) parser.parse(indexSearchRestOrdered.get(recID.toString()).toString());
				String comments = restWithMenu.get("Comment").toString();
				// System.out.println(comments);
				menu = getMenu(comments);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			restWithMenu.remove("Comment");
			restWithMenu.put("Menu", menu);
			recommends.put(recID.toString(), restWithMenu);
		}
		// set response
		respResult.put("numResults", indexSearchResult.get("size"));
		respResult.put("results", respPageRest);
		respResult.put("recommend", recommends);
		respResult.put("pageNum", pageNumStr);
		return respResult;
	}

	public static JSONObject reorder(String orderType, JSONObject oldResult, String location, String indexType) {
		System.out.println("reorder");
		JSONObject newResult = new JSONObject();
		ArrayList<Restaurant> rests = new ArrayList<Restaurant>();
		JSONParser parser = new JSONParser();
		// get longitude and latitude
		JSONObject locationJson = new JSONObject();
		Double locLatitude, locLongitude;
		try {
			locationJson = (JSONObject) parser.parse(getGeocoding(location));
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		locLatitude = Double.valueOf(locationJson.get("lat").toString());
		locLongitude = Double.valueOf(locationJson.get("lng").toString());

		for (Iterator iterator = oldResult.keySet().iterator(); iterator.hasNext();) {
			String id = iterator.next().toString();
			JSONObject rest = new JSONObject();
			// System.out.println(rest.toString());
			try {
				rest = (JSONObject) parser.parse(oldResult.get(id).toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
			// System.out.println(rest.toString());
			// System.out.println("id:______: "+id);

			Double restLat = Double.valueOf(rest.get("latitude").toString()),
					restLng = Double.valueOf(rest.get("longitude").toString());
			Double dist = distanceSimplify(locLatitude, locLongitude, restLat, restLng);
			//
			Restaurant restaurant = null;
			String restRate=rest.get("Rate").toString();
			if(restRate.length()<1) {
				restRate="0.0";
			}
			if (indexType.equals("Hadoop")) {

				restaurant = new Restaurant(id,restRate , dist.toString(),
						rest.get("Score").toString());
//				System.out.println("Rate: "+rest.get("Rate").toString());
			} else {// Lucene
//				System.out.println("dist: "+dist.toString());
				restaurant = new Restaurant(id, restRate, dist.toString(), "0.0");
//				System.out.println("Rate: "+rest.get("Rate").toString());
			}
			rests.add(restaurant);
		}
		if (orderType.equals("Distance")) {
			System.out.println("Ranking By Distance");
			Collections.sort(rests, new Comparator<Restaurant>() {
				public int compare(Restaurant rest1, Restaurant rest2) {
					Double rest1D = Double.parseDouble(rest1.Distance);
					Double rest2D = Double.parseDouble(rest2.Distance);
					return rest1D.compareTo(rest2D);
				}
			});
		} else if (orderType.equals("Rate")) {
			System.out.println("Ranking By Rate");
			Collections.sort(rests, new Comparator<Restaurant>() {
				public int compare(Restaurant rest1, Restaurant rest2) {
					System.out.println("rest1:"+rest1.Rate+" "+"rest2: "+rest2.Rate);
					Double rest1D = Double.parseDouble(rest1.Rate);
					Double rest2D = Double.parseDouble(rest2.Rate);
					int flag = rest1D.compareTo(rest2D);
					if (flag > 0) {
						return -1;
					} else if (flag == 0) {
						return 0;
					} else {
						return 1;
					}
				}
			});
		} else {
			System.out.println("Ranking By Default");
			if (indexType.equals("Hadoop")) {
				Collections.sort(rests, new Comparator<Restaurant>() {
					public int compare(Restaurant rest1, Restaurant rest2) {
						Double rest1D = Double.parseDouble(rest1.Score);
						Double rest2D = Double.parseDouble(rest2.Score);
						int flag = rest1D.compareTo(rest2D);
						if (flag > 0) {
							return -1;
						} else if (flag == 0) {
							return 0;
						} else {
							return 1;
						}
					}
				});
			}
//			else {// "Lucene"
//				
//				return oldResult;
//			}
		}
		Integer id = 0;
		for (Restaurant restaurant : rests) {
			JSONObject restJson = new JSONObject();
			try {
				restJson = (JSONObject) parser.parse(oldResult.get(restaurant.ID).toString());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			restJson.replace("Distance", restaurant.Distance);
			newResult.put(id.toString(), restJson);
			id++;
		}
		return newResult;
	}

	public static JSONObject recommend(JSONObject searchResult) {
		JSONObject recom = new JSONObject();
		// File recoFile=new File("recommend")
		/*
		 * Step 1: find relative id. 
		 * Step 2: find docs from data. 
		 * Since the data is not big enough and with limited computation resources, only a few association in72100 items. 
		 * Thus, this part was live blank.
		 */
		return recom;
	}

	public static String getGeocoding(String location) {
		GeoApiContext context = new GeoApiContext.Builder().apiKey("AIzaSyBskAVwUDjE9zQp36h-L333AkzCzq9c16U").build();
		GeocodingResult[] results = null;
		try {
			results = GeocodingApi.geocode(context, location).await();
		} catch (ApiException | InterruptedException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		System.out.println(gson.toJson(results[0].geometry.location));
		return gson.toJson(results[0].geometry.location).toString();
	}

	// distance calculation simplify
	public static Double distanceSimplify(Double lat1, Double lng1, Double lat2, Double lng2) {
		Double dx = lng1 - lng2;
		Double dy = lat1 - lat2;
		Double b = (lat1 + lat2) / 2.0;
		Double Lx = Math.toRadians(dx) * 6367000.0 * Math.cos(Math.toRadians(b));
		Double Ly = 6367000.0 * Math.toRadians(dy);
		return (Double) Math.sqrt(Lx * Lx + Ly * Ly);
	}

	public static String getMenu(String comments) {
		String menu = "";
		String[] commentWords = comments.split("\\W+");
		HashSet<String> menuWords = new HashSet<String>();
		for (String word : commentWords) {
			menuWords.add(word);
		}
		for (Iterator<String> iterator2 = menuWords.iterator(); iterator2.hasNext();) {
			menu += iterator2.next() + " ";
		}
		if (menu.equals("")) {
			menu += "Enjoy";
		}
		return menu;
	}
}
