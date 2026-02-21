import com.mongodb.MongoClient;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;
import com.mongodb.AggregationOutput;
import java.util.*;

public class MongoDBDataStoreUtilities
{
	static DBCollection storeComplaint;

	private static String envOrDefault(String key, String defaultValue)
	{
		String value = System.getenv(key);
		if (value == null || value.trim().isEmpty())
		{
			return defaultValue;
		}
		return value.trim();
	}

	private static int envIntOrDefault(String key, int defaultValue)
	{
		String value = System.getenv(key);
		if (value == null || value.trim().isEmpty())
		{
			return defaultValue;
		}
		try
		{
			return Integer.parseInt(value.trim());
		}
		catch (NumberFormatException e)
		{
			return defaultValue;
		}
	}

	public static DBCollection getConnection()
	{
		String host = envOrDefault("LEGACY_MONGO_HOST", "localhost");
		int port = envIntOrDefault("LEGACY_MONGO_PORT", 27017);
		String database = envOrDefault("LEGACY_MONGO_DB", "complaint");
		String collection = envOrDefault("LEGACY_MONGO_COLLECTION", "storedComplaints");

		MongoClient mongo;
		mongo = new MongoClient(host, port);

		DB db = mongo.getDB(database);
		storeComplaint= db.getCollection(collection);	
		return storeComplaint; 
	}


	public static String insertComplaint(String complaint_no,String actno,String compalaint_date,String subject, String description, String status, String closed)
	{
		try
		{		
			getConnection();
			BasicDBObject doc = new BasicDBObject("title", "storeComplaint").
			append("complaint_no", complaint_no).
			append("actno", actno).
			append("compalaint_date", compalaint_date).
			append("subject", subject).
			append("description", description).
			append("status", status).
			append("closed", closed);
			storeComplaint.insert(doc);
			return "Successfull";
		}
		catch(Exception e)
		{
			return "UnSuccessfull";
		}	
		
	}
//create table complaint (COMPLAINT_NO INT, ACTNO varchar(40), COMPLAINT_DATE DATE, SUBJECT varchar(40), DESCRIPTION varchar(40), STATUS varchar(40),	 CLOSED varchar(40)); 
	public static HashMap<String, ArrayList<Complaint_POJO>> selectComplaint()
	{	
		HashMap<String, ArrayList<Complaint_POJO>> complaints=null;

		try
		{

			getConnection();
			DBCursor cursor = storeComplaint.find();
			complaints=new HashMap<String, ArrayList<Complaint_POJO>>();
			while (cursor.hasNext())
			{
				BasicDBObject obj = (BasicDBObject) cursor.next();				

				if(!complaints.containsKey(obj.getString("complaint_no")))
				{	
					ArrayList<Complaint_POJO> arr = new ArrayList<Complaint_POJO>();
					complaints.put(obj.getString("complaint_no"), arr);
				}
				ArrayList<Complaint_POJO> listReview = complaints.get(obj.getString("complaint_no"));		
				Complaint_POJO complaint =new Complaint_POJO(obj.getString("complaint_no"),obj.getString("actno"),obj.getString("compalaint_date"),obj.getString("subject"),
					obj.getString("description"),obj.getString("status"),obj.getString("closed"));
			//add to Complaint hashmap
				listReview.add(complaint);

			}
			return complaints;
		}
		catch(Exception e)
		{
			complaints=null;
			return complaints;	
		}	


	}

	



	
}	
