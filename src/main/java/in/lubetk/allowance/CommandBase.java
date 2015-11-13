package in.lubetk.allowance;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

import in.lubetk.allowance.db.Family;
import in.lubetk.allowance.db.Parent;

@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM,
include = JsonTypeInfo.As.PROPERTY,
property = "command")
@JsonTypeIdResolver(CommandTypeIdResolver.class)
public abstract class CommandBase
{
	String command;
	String sessionToken;
	AmazonDynamoDB db;
	DynamoDBMapper mapper;
	
	@JsonIgnore
	public AmazonDynamoDB getAmazonDynamoDB()
	{
		return db;
	}
	
	@JsonIgnore
	public DynamoDBMapper getMapper()
	{
		return mapper;
	}
	
	public String getCommand()
	{
		return command;
	}
	
	public void setCommand(String command)
	{
		this.command = command;
	}
	
	public String getSessionToken()
	{
		return sessionToken;
	}
	
	public void setSessionToken(String sessionToken)
	{
		this.sessionToken = sessionToken;
	}
	
	public Parent getSessionParent()
	{
		return mapper.load(Parent.class, sessionToken);
	}
	
	public Family getSessionFamily()
	{
		Parent parent = getSessionParent();
		return mapper.load(Family.class, parent.getFamilyId());
	}
	
	public final CommandResponse handleCommand(AmazonDynamoDB db)
	{
		this.db = db;
		this.mapper = new DynamoDBMapper(db);
		return handleCommandInternal();
	}
	
	public abstract CommandResponse handleCommandInternal();
}
