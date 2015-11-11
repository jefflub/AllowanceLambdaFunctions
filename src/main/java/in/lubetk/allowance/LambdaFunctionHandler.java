package in.lubetk.allowance;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class LambdaFunctionHandler implements RequestHandler<CommandBase, CommandResponse> {
	AmazonDynamoDB db;
	
	public LambdaFunctionHandler()
	{
		db = new AmazonDynamoDBClient();
	}
	
	public LambdaFunctionHandler(AmazonDynamoDB db)
	{
		this.db = db;
	}
	
	@Override
    public CommandResponse handleRequest(CommandBase input, Context context) {
        context.getLogger().log("Input: " + input);
        return input.handleCommand(db);
    }

}
