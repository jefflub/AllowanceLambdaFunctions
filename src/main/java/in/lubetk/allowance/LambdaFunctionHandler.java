package in.lubetk.allowance;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LambdaFunctionHandler implements RequestStreamHandler {
	AmazonDynamoDB db;
	ObjectMapper mapper;
	
	public LambdaFunctionHandler()
	{
		db = new AmazonDynamoDBClient(new EnvironmentVariableCredentialsProvider());
		db.setRegion(Region.getRegion(Regions.US_WEST_2));
		mapper = new ObjectMapper();
	}
	
	public LambdaFunctionHandler(AmazonDynamoDB db)
	{
		this();
		this.db = db;
	}
	
	@Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws JsonGenerationException, JsonMappingException, IOException 
	{
		CommandBase command = mapper.readValue(input, CommandBase.class);
        CommandResponse response = command.handleCommand(db);
        mapper.writeValue(output, response);
    }

}
