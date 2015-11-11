package in.lubetk.allowance.db;

import java.util.Date;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "Transactions")
public class Transaction
{
	String bucketId;
	Date date;
	int amount;
	String note;
	String parentId;
	
	@DynamoDBHashKey
	public String getBucketId()
	{
		return bucketId;
	}
	public void setBucketId(String bucketId)
	{
		this.bucketId = bucketId;
	}
	
	@DynamoDBRangeKey
	public Date getDate()
	{
		return date;
	}
	public void setDate(Date date)
	{
		this.date = date;
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
	
	public String getParentId()
	{
		return parentId;
	}
	public void setParentId(String parentId)
	{
		this.parentId = parentId;
	}
}
