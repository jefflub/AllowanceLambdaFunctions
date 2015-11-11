package in.lubetk.allowance;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;

import in.lubetk.allowance.command.CreateFamily;
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
}
