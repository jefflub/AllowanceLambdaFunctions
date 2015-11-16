package in.lubetk.allowance.command;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

import in.lubetk.allowance.CommandBase;
import in.lubetk.allowance.CommandResponse;
import in.lubetk.allowance.db.Bucket;
import in.lubetk.allowance.db.Family;
import in.lubetk.allowance.db.Kid;

public class AddKidToFamily extends CommandBase
{
	private static final Map<String, Integer> DEFAULT_BUCKETS;
	private String name;
	private String emailAddress;
	private int allowance;
	private Map<String, Integer> buckets;
	
	static
	{
		DEFAULT_BUCKETS = new HashMap<>();
		DEFAULT_BUCKETS.put("Spending", 50);
		DEFAULT_BUCKETS.put("Saving", 25);
		DEFAULT_BUCKETS.put("Charity", 25);
	}
	
	@Override
	public CommandResponse handleCommandInternal()
	{
		DynamoDBMapper mapper = getMapper();

		if ( buckets == null || buckets.size() == 0 )
		{
			buckets = DEFAULT_BUCKETS;
		}
		
		int allocationSum = 0;
		for ( Integer i : buckets.values() )
		{
			allocationSum += i;
		}
		if ( allocationSum != 100 )
			throw new RuntimeException( "AddKidToFamily: Bucket allocations must sum to 100." );
		
		Kid kid = new Kid();
		kid.setName(name);
		kid.setEmailAddress(emailAddress);
		kid.setAllowance(allowance);
		mapper.save(kid);
		
		Family family = getSessionFamily();
		family.addKid(kid.getKidId());
		mapper.save(family);

		for (Entry<String, Integer> e : buckets.entrySet())
		{
			Bucket bucket = new Bucket();
			bucket.setName(e.getKey());
			bucket.setCurrentTotal(0);
			bucket.setDefaultAllocation(e.getValue());
			mapper.save(bucket);
			kid.addBucket(bucket.getBucketId());
		}
		
		// Save kid
		mapper.save(kid);
		AddKidToFamilyResponse response = new AddKidToFamilyResponse();
		response.setKidId(kid.getKidId());
		response.setViewToken(kid.getViewToken());
		return response;
	}
	
	public static class AddKidToFamilyResponse extends CommandResponse
	{
		private String kidId;
		private String viewToken;
		
		public void setKidId(String kidId)
		{
			this.kidId = kidId;
		}
		
		public String getKidId()
		{
			return kidId;
		}

		public String getViewToken()
		{
			return viewToken;
		}

		public void setViewToken(String viewToken)
		{
			this.viewToken = viewToken;
		}
	}

	public String getEmailAddress()
	{
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress)
	{
		this.emailAddress = emailAddress;
	}

	public int getAllowance()
	{
		return allowance;
	}

	public void setAllowance(int allowance)
	{
		this.allowance = allowance;
	}

	public Map<String, Integer> getBuckets()
	{
		return buckets;
	}

	public void setBuckets(Map<String, Integer> buckets)
	{
		this.buckets = buckets;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

}
