package in.lubetk.allowance.command;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

import in.lubetk.allowance.CommandBase;
import in.lubetk.allowance.CommandResponse;
import in.lubetk.allowance.db.Family;
import in.lubetk.allowance.db.Parent;

public class CreateFamily extends CommandBase
{
	private String name;
	private String parentName;
	private String emailAddress;
	private String password;

	@Override
	public CommandResponse handleCommand(AmazonDynamoDB db)
	{
		DynamoDBMapper mapper = new DynamoDBMapper(db);
		Family family = new Family();
		family.setName(name);
		mapper.save(family);
		Parent parent = new Parent();
		parent.setName(parentName);
		parent.setEmailAddress(emailAddress);
		parent.setPassword(password);
		parent.setFamilyId(family.getFamilyId());
		mapper.save(parent);
		CreateFamilyResponse response = new CreateFamilyResponse();
		response.setFamilyId(family.getFamilyId());
		response.setParentId(parent.getParentId());
		
		return response;
	}
	
	public static class CreateFamilyResponse extends CommandResponse
	{
		private String familyId;
		private String parentId;
		public String getFamilyId()
		{
			return familyId;
		}
		public void setFamilyId(String familyId)
		{
			this.familyId = familyId;
		}
		public String getParentId()
		{
			return parentId;
		}
		public void setParentId(String parentId)
		{
			this.parentId = parentId;
		}
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getParentName()
	{
		return parentName;
	}

	public void setParentName(String parentName)
	{
		this.parentName = parentName;
	}

	public String getEmailAddress()
	{
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress)
	{
		this.emailAddress = emailAddress;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}
}
