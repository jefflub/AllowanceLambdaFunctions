package in.lubetk.allowance.command;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import in.lubetk.allowance.CommandBase;
import in.lubetk.allowance.CommandResponse;
import in.lubetk.allowance.db.Bucket;
import in.lubetk.allowance.db.Kid;
import in.lubetk.allowance.db.Transaction;

public class AddMoneyForKid extends CommandBase
{
	private String kidId;
	private int amount;
	private String note;
	private Map<String, Integer> allocations;
	
	@Override
	public CommandResponse handleCommandInternal()
	{
		Kid kid = Kid.loadFromId(getMapper(), kidId);
		Map<String, Bucket> bucketMap = Bucket.loadFromIds(getMapper(), kid.getBuckets());
		if (allocations != null && !allocations.isEmpty())
		{
			int allocationSum = 0;
			for (Entry<String, Integer> e : allocations.entrySet())
			{
				if (!bucketMap.containsKey(e.getKey()))
					throw new RuntimeException("AddMoneyForKid: Attempted to add money to non-existent bucket");
				allocationSum += e.getValue();
			}
			if (allocationSum != 100)
				throw new RuntimeException("AddMoneyForKid: Custom bucket allocations must add to 100");
		}
		else
		{
			allocations = new HashMap<>();
			for ( Entry<String, Bucket> e : bucketMap.entrySet() )
			{
				allocations.put(e.getKey(), e.getValue().getDefaultAllocation());
			}
		}
		
		ArrayList<Bucket> responseBuckets = new ArrayList<Bucket>();
		for (Entry<String, Integer> e : allocations.entrySet())
		{
			// Add transaction
			Transaction t = new Transaction();
			t.setDate(new Date());
			t.setBucketId(e.getKey());
			t.setNote(note);
			t.setParentId(getCognitoIdentityId());
			int bucketAmount = (amount * e.getValue()) / 100; 
			t.setAmount(bucketAmount);
			getMapper().save(t);
			// Update bucket current total
			Bucket bucket = bucketMap.get(e.getKey());
			bucket.setCurrentTotal(bucket.getCurrentTotal() + bucketAmount);
			getMapper().save(bucket);
			responseBuckets.add(bucket);
		}
		
		AddMoneyForKidResponse response = new AddMoneyForKidResponse();
		response.setBucketInfo(responseBuckets.toArray(new Bucket[1]));
		return response;
	}
	
	public static class AddMoneyForKidResponse extends CommandResponse
	{
		private Bucket[] bucketInfo;
		
		public void setBucketInfo(Bucket[] bucketInfo)
		{
			this.bucketInfo = bucketInfo;
		}
		
		public Bucket[] getBucketInfo()
		{
			return this.bucketInfo;
		}
	}
	
	public String getKidId()
	{
		return kidId;
	}

	public void setKidId(String kidId)
	{
		this.kidId = kidId;
	}

	public int getAmount()
	{
		return amount;
	}

	public void setAmount(int amount)
	{
		this.amount = amount;
	}

	public String getNote()
	{
		return note;
	}

	public void setNote(String note)
	{
		this.note = note;
	}

	public Map<String, Integer> getAllocations()
	{
		return allocations;
	}

	public void setAllocations(Map<String, Integer> allocations)
	{
		this.allocations = allocations;
	}

}
