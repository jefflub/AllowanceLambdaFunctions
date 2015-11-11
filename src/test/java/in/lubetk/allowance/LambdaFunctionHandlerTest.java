package in.lubetk.allowance;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.CreateTableResult;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.lambda.runtime.Context;

import in.lubetk.allowance.command.CreateFamily;
import in.lubetk.allowance.command.CreateFamily.CreateFamilyResponse;
import in.lubetk.allowance.db.Bucket;
import in.lubetk.allowance.db.Family;
import in.lubetk.allowance.db.Kid;
import in.lubetk.allowance.db.Parent;
import in.lubetk.allowance.db.Transaction;
import junit.framework.TestCase;

public class LambdaFunctionHandlerTest {

    private static AmazonDynamoDB dynamoDB;

    @BeforeClass
    public static void createInput() throws IOException {
    	setupDynamoDBTables();
    }
    
    @AfterClass
    public static void cleanupTables()
    {
    	dynamoDB.deleteTable("Families");
    	dynamoDB.deleteTable("Buckets");
    	dynamoDB.deleteTable("Kids");
    	dynamoDB.deleteTable("Parents");
    	dynamoDB.deleteTable("Transactions");
    }

    private Context createContext() {
        TestContext ctx = new TestContext();

        return ctx;
    }

    @Test
    public void testCreateFamily() 
    {
        LambdaFunctionHandler handler = new LambdaFunctionHandler(dynamoDB);
        Context ctx = createContext();
        CreateFamily command = new CreateFamily();
        command.setName("Lubetkin Family");
        command.setParentName("Dad");
        command.setEmailAddress("jefflub@example.com");
        command.setPassword("foobar");
        CreateFamilyResponse output = (CreateFamilyResponse)handler.handleRequest(command, ctx);
        TestCase.assertNotNull(output.getFamilyId());
        TestCase.assertNotNull(output.getParentId());
    }
    
    @Test
    public void testTables()
    {
    	DynamoDBMapper mapper = new DynamoDBMapper(dynamoDB);
    	
    	Family family = new Family();
    	family.setName("Lubetkin");
    	mapper.save(family);
    	System.err.println("FamilyID=" + family.getFamilyId());
    }
    
    private static void setupDynamoDBTables()
    {
    	dynamoDB = new AmazonDynamoDBClient();
    	dynamoDB.setEndpoint("http://localhost:8000");
    	
    	setupTable(Family.class);
    	setupTable(Bucket.class);
    	setupTable(Kid.class);
    	setupTable(Parent.class);
    	setupTable(Transaction.class);
    }
    
    private static void setupTable( Class<?> tableClass )
    {
    	DynamoDBMapper mapper = new DynamoDBMapper(dynamoDB);
    	CreateTableRequest req = mapper.generateCreateTableRequest(tableClass);
    	req.setProvisionedThroughput(new ProvisionedThroughput(5L, 5L));
    	if ( req.getGlobalSecondaryIndexes() != null )
    	{
	    	for ( GlobalSecondaryIndex i : req.getGlobalSecondaryIndexes() )
	    	{
	    		i.setProvisionedThroughput(new ProvisionedThroughput(5L, 5L));
	    	}
    	}
    	System.err.println(req.toString());
    	CreateTableResult result = dynamoDB.createTable(req);
    	System.err.println(result.toString());
    }
}
