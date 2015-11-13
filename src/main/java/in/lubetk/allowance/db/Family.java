package in.lubetk.allowance.db;

import java.util.HashSet;
import java.util.Set;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAutoGeneratedKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "Families")
public class Family {
	private String familyId;
	private String name;
	private Set<String> kids;
	
	@DynamoDBHashKey
    @DynamoDBAutoGeneratedKey
	public String getFamilyId() 
	{
		return familyId;
	}
	public void setFamilyId(String familyId) 
	{
		this.familyId = familyId;
	}
	
	public String getName() 
	{
		return name;
	}
	public void setName(String name) 
	{
		this.name = name;
	}
	
	public Set<String> getKids()
	{
		return kids;
	}
	public void setKids(Set<String> kids)
	{
		this.kids = kids;
	}
	public void addKid(String kidId)
	{
		if ( kids == null )
			kids = new HashSet<String>();
		kids.add(kidId);
	}
}
