package in.lubetk.allowance.command;

import java.util.Date;

import in.lubetk.allowance.CommandBase;
import in.lubetk.allowance.CommandResponse;
import in.lubetk.allowance.db.Bucket;
import in.lubetk.allowance.db.Transaction;

public class SpendMoney extends CommandBase
{
	private String bucketId;
	private int amount;
	private String note;
	
	@Override
	public CommandResponse handleCommandInternal()
	{
		Bucket bucket = Bucket.loadFromId(getMapper(), bucketId);
		Transaction trans = new Transaction();
		trans.setBucketId(bucketId);
		trans.setParentId(getCognitoIdentityId());
		trans.setNote(note);
		trans.setAmount((amount < 0 ? 1 : -1) * amount);
		trans.setDate(new Date());
		getMapper().save(trans);
		bucket.setCurrentTotal(bucket.getCurrentTotal() + trans.getAmount());
		getMapper().save(bucket);
		SpendMoneyResponse response = new SpendMoneyResponse();
		response.setBucketInfo(bucket);
		return response;
	}
	
	public static class SpendMoneyResponse extends CommandResponse
	{
		Bucket bucketInfo;

		public Bucket getBucketInfo()
		{
			return bucketInfo;
		}

		public void setBucketInfo(Bucket bucketInfo)
		{
			this.bucketInfo = bucketInfo;
		}
	}

	public String getBucketId()
	{
		return bucketId;
	}

	public void setBucketId(String bucketId)
	{
		this.bucketId = bucketId;
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

}
