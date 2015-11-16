package in.lubetk.allowance.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.lubetk.allowance.CommandBase;
import in.lubetk.allowance.CommandResponse;
import in.lubetk.allowance.db.Bucket;
import in.lubetk.allowance.db.Family;
import in.lubetk.allowance.db.Kid;

public class ViewFamily extends CommandBase
{

	@Override
	public CommandResponse handleCommandInternal()
	{
		ViewFamilyResponse response = new ViewFamilyResponse();
		
		Family family = getSessionFamily();
		response.setFamilyId(family.getFamilyId());
		response.setFamilyName(family.getName());
		List<Kid> kidList = new ArrayList<Kid>();
		for (String k: family.getKids())
		{
			Kid kid = getMapper().load(Kid.class, k);
			kidList.add(kid);
			for (String b: kid.getBuckets())
			{
				Bucket bucket = getMapper().load(Bucket.class, b);
				response.addBucketToMap(bucket);
			}
		}
		response.setKids(kidList.toArray(new Kid[1]));
		
		return response;
	}
	
	public static class ViewFamilyResponse extends CommandResponse
	{
		private String familyId;
		private String familyName;
		private Kid[] kids;
		private Map<String, Bucket> buckets;
		
		public String getFamilyId()
		{
			return familyId;
		}
		public void setFamilyId(String familyId)
		{
			this.familyId = familyId;
		}
		public String getFamilyName()
		{
			return familyName;
		}
		public void setFamilyName(String familyName)
		{
			this.familyName = familyName;
		}
		public Kid[] getKids()
		{
			return kids;
		}
		public void setKids(Kid[] kids)
		{
			this.kids = kids;
		}
		public Map<String, Bucket> getBuckets()
		{
			return buckets;
		}
		public void setBuckets(Map<String, Bucket> buckets)
		{
			this.buckets = buckets;
		}
		public void addBucketToMap(Bucket bucket)
		{
			if (buckets == null)
			{
				buckets = new HashMap<String, Bucket>();
			}
			buckets.put(bucket.getBucketId(), bucket);
		}
	}

}
