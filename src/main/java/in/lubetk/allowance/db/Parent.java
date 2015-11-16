package in.lubetk.allowance.db;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "Parents")
public class Parent
{
	private String name;
	private String cognitoIdentityId;
	private String password;
	private String familyId;
	private String emailAddress;
	
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	
	@DynamoDBHashKey
	public String getCognitoIdentityId()
	{
		return cognitoIdentityId;
	}
	public void setCognitoIdentityId(String cognitoIdentityId)
	{
		this.cognitoIdentityId = cognitoIdentityId;
	}
	
	public String getPassword()
	{
		return password;
	}
	public void setPassword(String password)
	{
		this.password = password;
	}
	
	public String getFamilyId()
	{
		return familyId;
	}
	public void setFamilyId(String familyId)
	{
		this.familyId = familyId;
	}
	public String getEmailAddress()
	{
		return emailAddress;
	}
	public void setEmailAddress(String emailAddress)
	{
		this.emailAddress = emailAddress;
	}
}
