package in.lubetk.allowance;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM,
include = JsonTypeInfo.As.PROPERTY,
property = "command")
@JsonTypeIdResolver(CommandTypeIdResolver.class)
public abstract class CommandBase
{
	String command;
	
	public String getCommand()
	{
		return command;
	}
	
	public void setCommand(String command)
	{
		this.command = command;
	}
	
	public abstract CommandResponse handleCommand(AmazonDynamoDB db);
}
