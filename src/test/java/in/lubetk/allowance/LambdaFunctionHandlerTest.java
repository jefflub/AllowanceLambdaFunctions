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
import in.lubetk.allowance.command.SpendMoney.SpendMoneyResponse;
import in.lubetk.allowance.command.StatusReport.StatusReportResponse;
import in.lubetk.allowance.command.ViewFamily.ViewFamilyResponse;
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
		String json = "{ \"command\":\"CreateFamily\", \"name\":\"Lubetkin Horde\", \"parentName\":\"Dad\", \"cognitoIdentityId\":\"12345\", \"password\":\"foobar\" }";
        CreateFamilyResponse output = (CreateFamilyResponse)runCommand(json, CreateFamilyResponse.class);
        TestCase.assertNotNull(output.getFamilyId());
        
        json = "{\"command\":\"AddKidToFamily\", \"cognitoIdentityId\":\"12345\", \"name\":\"New Kid\",\"emailAddress\":\"foo@bar.com\",\"allowance\":15}";
        AddKidToFamilyResponse response = (AddKidToFamilyResponse)runCommand(json, AddKidToFamilyResponse.class);
        TestCase.assertNotNull(response.getKidId());
        
        json = "{\"command\":\"AddKidToFamily\", \"cognitoIdentityId\":\"12345\", \"name\":\"Bad Kid\",\"emailAddress\":\"baddude@bar.com\"," 
        					+ "\"allowance\":10, \"buckets\":{\"Booze\":33, \"Chicks\":67}}";
        AddKidToFamilyResponse addKidResponse = (AddKidToFamilyResponse)runCommand(json, AddKidToFamilyResponse.class);
        TestCase.assertNotNull(addKidResponse.getKidId());
        
        json = String.format("{\"command\":\"AddMoneyForKid\", \"cognitoIdentityId\":\"12345\", \"kidId\":\"%s\", \"amount\":10000, \"note\":\"Default allocations\"}",  response.getKidId());
        AddMoneyForKidResponse addMoneyResponse = (AddMoneyForKidResponse)runCommand(json, AddMoneyForKidResponse.class);
        TestCase.assertNotNull(addMoneyResponse.getBucketInfo());
        TestCase.assertEquals(addMoneyResponse.getBucketInfo().length, 3);
        TestCase.assertEquals(5000, getBucketForName(addMoneyResponse.getBucketInfo(), "Spending").getCurrentTotal());
        TestCase.assertEquals(2500, getBucketForName(addMoneyResponse.getBucketInfo(), "Saving").getCurrentTotal());
        TestCase.assertEquals(2500, getBucketForName(addMoneyResponse.getBucketInfo(), "Charity").getCurrentTotal());

        json = String.format("{\"command\":\"AddMoneyForKid\", \"cognitoIdentityId\":\"12345\", \"kidId\":\"%s\", " 
        					  + "\"amount\":10000, \"note\":\"Custom allocations\", "
        					  + "\"allocations\":{" +
        					  "\"%s\":33, \"%s\":33, \"%s\":34}}", response.getKidId(),
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
        
        json = String.format("{\"command\":\"SpendMoney\", \"cognitoIdentityId\":\"12345\", \"bucketId\":\"%s\", \"amount\":2000, \"note\":\"Bought thing\"}",
				getBucketForName(addMoneyResponse.getBucketInfo(), "Spending").getBucketId());
        SpendMoneyResponse spendResponse = (SpendMoneyResponse)runCommand(json, SpendMoneyResponse.class);
        TestCase.assertNotNull(spendResponse.getBucketInfo());
        TestCase.assertEquals(5000 + 3300 - 2000, spendResponse.getBucketInfo().getCurrentTotal());

        json = String.format("{\"command\":\"AddMoneyForKid\", \"cognitoIdentityId\":\"12345\", \"kidId\":\"%s\", \"amount\":20000, \"note\":\"Default allocations\"}",  addKidResponse.getKidId());
        addMoneyResponse = (AddMoneyForKidResponse)runCommand(json, AddMoneyForKidResponse.class);
        TestCase.assertNotNull(addMoneyResponse.getBucketInfo());
        TestCase.assertEquals(addMoneyResponse.getBucketInfo().length, 2);
        TestCase.assertEquals(6600, getBucketForName(addMoneyResponse.getBucketInfo(), "Booze").getCurrentTotal());
        TestCase.assertEquals(13400, getBucketForName(addMoneyResponse.getBucketInfo(), "Chicks").getCurrentTotal());
        
        json = "{\"command\":\"ViewFamily\", \"cognitoIdentityId\":\"12345\"}";
        ViewFamilyResponse vfResponse = (ViewFamilyResponse)runCommand(json, ViewFamilyResponse.class);
        TestCase.assertNotNull(vfResponse);
        TestCase.assertEquals(2, vfResponse.getKids().length);
        TestCase.assertEquals(5, vfResponse.getBuckets().size());
        for ( String bid : vfResponse.getBuckets().keySet() )
        {
        	Bucket b = vfResponse.getBuckets().get(bid);
        	switch (b.getName())
        	{
        	case "Spending":
        		TestCase.assertEquals(5000 + 3300 - 2000, b.getCurrentTotal());
        		break;
        	case "Saving":
        		TestCase.assertEquals(2500 + 3300, b.getCurrentTotal());
        		break;
        	case "Charity":
        		TestCase.assertEquals(2500 + 3400, b.getCurrentTotal());
        		break;
        	case "Booze":
        		TestCase.assertEquals(6600, b.getCurrentTotal());
        		break;
        	case "Chicks":
        		TestCase.assertEquals(13400, b.getCurrentTotal());
        		break;
        	default:
        		TestCase.fail("Unknown bucket returned from ViewFamily");
        	}
        }
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
    	System.err.println("Request: " + json);
        Context ctx = createContext();
		ByteArrayInputStream inputStream = new ByteArrayInputStream(json.getBytes());
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);
        handler.handleRequest(inputStream, outputStream, ctx);
        String outputJson = outputStream.toString();
        System.err.println("Response: " + outputJson);
        return (CommandResponse)(new ObjectMapper()).readValue(outputJson, responseClass);
    }
}
