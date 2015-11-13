package in.lubetk.allowance.command;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

import in.lubetk.allowance.CommandBase;
import in.lubetk.allowance.CommandResponse;
import in.lubetk.allowance.db.Bucket;
import in.lubetk.allowance.db.Family;
import in.lubetk.allowance.db.Kid;

public class AddKidToFamily extends CommandBase
{
	private static final String[] DEFAULT_BUCKETS = { "Spending", "Saving", "Charity" };
	private static final int[] DEFAULT_ALLOCATIONS = { 50, 25, 25 };
	private String name;
	private String emailAddress;
	private int allowance;
	private String[] buckets;
	private int[] bucketAllocations;
	
	@Override
	public CommandResponse handleCommandInternal()
	{
		DynamoDBMapper mapper = getMapper();

		if ( buckets == null || buckets.length == 0 )
		{
			buckets = DEFAULT_BUCKETS;
			bucketAllocations = DEFAULT_ALLOCATIONS;
		}
		
		if ( buckets.length != bucketAllocations.length )
			throw new RuntimeException( "AddKidToFamily: Must have same number of buckets and default allocations" );
		
		int allocationSum = 0;
		for ( int i : bucketAllocations )
		{
			allocationSum += i;
		}
		if ( allocationSum != 100 )
			throw new RuntimeException( "AddKidToFamily: Bucket allocations must sum to 100." );
		
		// Add the kid
		Kid kid = new Kid();
		kid.setName(name);
		kid.setEmailAddress(emailAddress);
		kid.setAllowance(allowance);
		mapper.save(kid);
		
		// Add the kid ID to family
		Family family = getSessionFamily();
		family.addKid(kid.getKidId());
		mapper.save(family);
		// For each bucket
		for (int i = 0; i < buckets.length; i++)
		{
		//    Add bucket
			Bucket bucket = new Bucket();
			bucket.setName(buckets[i]);
			bucket.setCurrentTotal(0);
			mapper.save(bucket);
		//    Add bucket ID to kid
			kid.addBucket(bucket.getBucketId(), bucketAllocations[i]);
		}
		// Save kid
		mapper.save(kid);
		AddKidToFamilyResponse response = new AddKidToFamilyResponse();
		response.setKidId(kid.getKidId());
		response.setSessionToken(getSessionToken());
		return response;
	}
	
	public static class AddKidToFamilyResponse extends CommandResponse
	{
		private String kidId;
		
		public void setKidId(String kidId)
		{
			this.kidId = kidId;
		}
		
		public String getKidId()
		{
			return kidId;
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

	public String[] getBuckets()
	{
		return buckets;
	}

	public void setBuckets(String[] buckets)
	{
		this.buckets = buckets;
	}

	public int[] getBucketAllocations()
	{
		return bucketAllocations;
	}

	public void setBucketAllocations(int[] bucketAllocations)
	{
		this.bucketAllocations = bucketAllocations;
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
