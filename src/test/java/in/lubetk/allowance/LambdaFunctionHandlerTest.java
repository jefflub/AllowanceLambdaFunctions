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

import in.lubetk.allowance.command.CreateFamily.CreateFamilyResponse;
import in.lubetk.allowance.command.StatusReport.StatusReportResponse;
import in.lubetk.allowance.db.DbUtils;
import junit.framework.TestCase;

public class LambdaFunctionHandlerTest {

    private static AmazonDynamoDB dynamoDB;
    private static LambdaFunctionHandler handler;

    @BeforeClass
    public static void createInput() throws IOException {
    	dynamoDB = new AmazonDynamoDBClient();
    	dynamoDB.setEndpoint("http://localhost:8000");
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
    public void testCreateFamily() throws JsonGenerationException, JsonMappingException, IOException 
    {
		String json = "{ \"command\":\"CreateFamily\", \"name\":\"Lubetkin Horde\", \"parentName\":\"Dad\", \"emailAddress\":\"jefflub@example.com\", \"password\":\"foobar\" }";
        CreateFamilyResponse output = (CreateFamilyResponse)runCommand(json, CreateFamilyResponse.class);
        TestCase.assertNotNull(output.getFamilyId());
        TestCase.assertNotNull(output.getParentId());
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
