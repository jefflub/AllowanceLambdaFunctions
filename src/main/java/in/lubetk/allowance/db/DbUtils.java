package in.lubetk.allowance.db;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.CreateTableResult;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.Projection;
import com.amazonaws.services.dynamodbv2.model.ProjectionType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;

public class DbUtils
{
	public static void setupTables(AmazonDynamoDB dynamoDB)
	{
    	setupTable(Family.class, dynamoDB);
    	setupTable(Bucket.class, dynamoDB);
    	setupTable(Kid.class, dynamoDB);
    	setupTable(Parent.class, dynamoDB);
    	setupTable(Transaction.class, dynamoDB);
	}
	
	private static void setupTable(Class<?> tableClass, AmazonDynamoDB dynamoDB)
    {
    	DynamoDBMapper mapper = new DynamoDBMapper(dynamoDB);
    	CreateTableRequest req = mapper.generateCreateTableRequest(tableClass);
    	req.setProvisionedThroughput(new ProvisionedThroughput(1L, 1L));
    	if ( req.getGlobalSecondaryIndexes() != null )
    	{
	    	for ( GlobalSecondaryIndex i : req.getGlobalSecondaryIndexes() )
	    	{
	    		i.setProvisionedThroughput(new ProvisionedThroughput(1L, 1L));
	    		i.setProjection((new Projection()).withProjectionType(ProjectionType.ALL));
	    	}
    	}
    	System.err.println(req.toString());
    	CreateTableResult result = dynamoDB.createTable(req);
    	System.err.println(result.toString());
    }
	
	public static void deleteTables(AmazonDynamoDB dynamoDB)
	{
    	dynamoDB.deleteTable("Families");
    	dynamoDB.deleteTable("Buckets");
    	dynamoDB.deleteTable("Kids");
    	dynamoDB.deleteTable("Parents");
    	dynamoDB.deleteTable("Transactions");		
	}
}
