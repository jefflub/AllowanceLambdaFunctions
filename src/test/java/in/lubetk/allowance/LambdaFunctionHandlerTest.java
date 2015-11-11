package in.lubetk.allowance;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import in.lubetk.allowance.command.CreateFamily.CreateFamilyResponse;
import in.lubetk.allowance.db.DbUtils;
import in.lubetk.allowance.db.Family;
import junit.framework.TestCase;

public class LambdaFunctionHandlerTest {

    private static AmazonDynamoDB dynamoDB;

    @BeforeClass
    public static void createInput() throws IOException {
    	dynamoDB = new AmazonDynamoDBClient();
    	dynamoDB.setEndpoint("http://localhost:8000");
    	DbUtils.setupTables(dynamoDB);
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
    public void testCreateFamily() throws JsonGenerationException, JsonMappingException, IOException 
    {
        LambdaFunctionHandler handler = new LambdaFunctionHandler(dynamoDB);
        Context ctx = createContext();
		String json = "{ \"command\":\"CreateFamily\", \"name\":\"Lubetkin Horde\", \"parentName\":\"Dad\", \"emailAddress\":\"jefflub@example.com\", \"password\":\"foobar\" }";
		ByteArrayInputStream inputStream = new ByteArrayInputStream(json.getBytes());
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);
        handler.handleRequest(inputStream, outputStream, ctx);
        CreateFamilyResponse output = (new ObjectMapper()).readValue(outputStream.toByteArray(), CreateFamilyResponse.class);
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
}
