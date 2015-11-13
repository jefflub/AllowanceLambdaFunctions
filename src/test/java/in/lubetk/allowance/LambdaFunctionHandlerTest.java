package in.lubetk.allowance;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import in.lubetk.allowance.command.AddKidToFamily.AddKidToFamilyResponse;
import in.lubetk.allowance.command.AddMoneyForKid.AddMoneyForKidResponse;
import in.lubetk.allowance.command.CreateFamily.CreateFamilyResponse;
import in.lubetk.allowance.command.StatusReport.StatusReportResponse;
import in.lubetk.allowance.db.Bucket;
import in.lubetk.allowance.db.DbUtils;
import junit.framework.TestCase;

public class LambdaFunctionHandlerTest {

    private static AmazonDynamoDB dynamoDB;
    private static LambdaFunctionHandler handler;

    @BeforeClass
    public static void createInput() throws IOException {
    	dynamoDB = new AmazonDynamoDBClient();
    	dynamoDB.setEndpoint("http://localhost:8000");
    	try
    	{
    		DbUtils.deleteTables(dynamoDB);
    	}
    	catch( RuntimeException ex )
    	{
    		// Ignore cleanup failure
    	}
    	DbUtils.setupTables(dynamoDB);
    	handler = new LambdaFunctionHandler(dynamoDB);
    }
    
    @AfterClass
    public static void cleanupTables()
    {
    	DbUtils.deleteTables(dynamoDB);
    }

    private Context createContext() {
        TestContext ctx = new TestContext();

        return ctx;
    }

    @Test
    public void testGoldenPath() throws JsonGenerationException, JsonMappingException, IOException 
    {
		String json = "{ \"command\":\"CreateFamily\", \"name\":\"Lubetkin Horde\", \"parentName\":\"Dad\", \"emailAddress\":\"jefflub@example.com\", \"password\":\"foobar\" }";
        CreateFamilyResponse output = (CreateFamilyResponse)runCommand(json, CreateFamilyResponse.class);
        TestCase.assertNotNull(output.getFamilyId());
        TestCase.assertNotNull(output.getParentId());
        TestCase.assertEquals(output.getParentId(), output.getSessionToken());
        
        json = "{\"command\":\"AddKidToFamily\",\"sessionToken\":\"" + output.getSessionToken() + "\",\"name\":\"New Kid\",\"emailAddress\":\"foo@bar.com\",\"allowance\":15}";
        AddKidToFamilyResponse response = (AddKidToFamilyResponse)runCommand(json, AddKidToFamilyResponse.class);
        TestCase.assertNotNull(response.getKidId());
        TestCase.assertEquals(response.getSessionToken(), output.getSessionToken());
        
        json = String.format("{\"command\":\"AddMoneyForKid\",\"sessionToken\":\"%s\", \"kidId\":\"%s\", \"amount\":10000, \"note\":\"Default allocations\"}", response.getSessionToken(), response.getKidId());
        AddMoneyForKidResponse addMoneyResponse = (AddMoneyForKidResponse)runCommand(json, AddMoneyForKidResponse.class);
        TestCase.assertNotNull(addMoneyResponse.getBucketInfo());
        TestCase.assertEquals(addMoneyResponse.getBucketInfo().length, 3);
        TestCase.assertEquals(5000, getBucketForName(addMoneyResponse.getBucketInfo(), "Spending").getCurrentTotal());
        TestCase.assertEquals(2500, getBucketForName(addMoneyResponse.getBucketInfo(), "Saving").getCurrentTotal());
        TestCase.assertEquals(2500, getBucketForName(addMoneyResponse.getBucketInfo(), "Charity").getCurrentTotal());

        json = String.format("{\"command\":\"AddMoneyForKid\",\"sessionToken\":\"%s\", \"kidId\":\"%s\", " 
        					  + "\"amount\":10000, \"note\":\"Custom allocations\", "
        					  + "\"allocationOverride\":{" +
        					  "\"%s\":33, \"%s\":33, \"%s\":34}}", response.getSessionToken(), response.getKidId(),
        					  getBucketForName(addMoneyResponse.getBucketInfo(), "Spending").getBucketId(),
        					  getBucketForName(addMoneyResponse.getBucketInfo(), "Saving").getBucketId(),
        					  getBucketForName(addMoneyResponse.getBucketInfo(), "Charity").getBucketId()
        					  );
        addMoneyResponse = (AddMoneyForKidResponse)runCommand(json, AddMoneyForKidResponse.class);
        TestCase.assertNotNull(addMoneyResponse.getBucketInfo());
        TestCase.assertEquals(addMoneyResponse.getBucketInfo().length, 3);
        TestCase.assertEquals(5000 + 3300, getBucketForName(addMoneyResponse.getBucketInfo(), "Spending").getCurrentTotal());
        TestCase.assertEquals(2500 + 3300, getBucketForName(addMoneyResponse.getBucketInfo(), "Saving").getCurrentTotal());
        TestCase.assertEquals(2500 + 3400, getBucketForName(addMoneyResponse.getBucketInfo(), "Charity").getCurrentTotal());
        
    }
    
    private Bucket getBucketForName( Bucket[] buckets, String name )
    {
    	for ( Bucket b : buckets )
    	{
    		if ( b.getName().equals(name))
    		{
    			return b;
    		}
    	}
    	return null;
    }
    
    @Test
    public void testStatusReport() throws JsonGenerationException, JsonMappingException, IOException
    {
    	String json = "{ \"command\":\"StatusReport\" }";
    	StatusReportResponse output = (StatusReportResponse)runCommand(json, StatusReportResponse.class);
    	TestCase.assertNotNull(output);
    	System.err.println( output.getMessage() );
    }
    
    private CommandResponse runCommand(String json, Class<?> responseClass) throws JsonGenerationException, JsonMappingException, IOException
    {
        Context ctx = createContext();
		ByteArrayInputStream inputStream = new ByteArrayInputStream(json.getBytes());
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);
        handler.handleRequest(inputStream, outputStream, ctx);
        return (CommandResponse)(new ObjectMapper()).readValue(outputStream.toByteArray(), responseClass);
    }
}
