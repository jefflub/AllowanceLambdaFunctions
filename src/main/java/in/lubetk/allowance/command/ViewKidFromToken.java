package in.lubetk.allowance.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;

import in.lubetk.allowance.CommandBase;
import in.lubetk.allowance.CommandResponse;
import in.lubetk.allowance.db.Bucket;
import in.lubetk.allowance.db.Kid;
import in.lubetk.allowance.db.Transaction;

public class ViewKidFromToken extends CommandBase
{
	private String viewToken;
	
	@Override
	public CommandResponse handleCommandInternal()
	{
		if (viewToken == null || viewToken.length() != Kid.TOKEN_LENGTH)
			throw new RuntimeException("ViewKidFromToken: Missing or invalid token");
		
		ViewKidFromTokenResponse response = new ViewKidFromTokenResponse();
		Kid keyKid = new Kid();
		keyKid.setViewToken(viewToken);
		DynamoDBQueryExpression<Kid> kidQuery = new DynamoDBQueryExpression<Kid>()
				.withIndexName("ViewTokenIndex")
				.withConsistentRead(false)
				.withHashKeyValues(keyKid)
				.withLimit(10);
		List<Kid> kids = getMapper().query(Kid.class, kidQuery);
		if (kids.size() == 0)
			throw new RuntimeException("ViewKidFromToken: No kids for token.");
		Kid kid = kids.get(0);
		response.setKid(kid);
		for (String b : kid.getBuckets())
		{
			Bucket bucket = getMapper().load(Bucket.class, b);
			response.addBucket(bucket);
			
			Transaction key = new Transaction();
			key.setBucketId(b);
			DynamoDBQueryExpression<Transaction> transQuery = new DynamoDBQueryExpression<Transaction>()
					.withHashKeyValues(key)
					.withScanIndexForward(false)
					.withLimit(10);
			List<Transaction> transactions = getMapper().query(Transaction.class, transQuery);
			for (Transaction t : transactions)
				response.addTransaction(t);
		}
		
		return response;
	}
	
	public static class ViewKidFromTokenResponse extends CommandResponse
	{
		private Kid kid;
		private Map<String, Bucket> bucketInfo;
		private Map<String, List<Transaction>> transactions;
		
		public Kid getKid()
		{
			return kid;
		}
		public void setKid(Kid kid)
		{
			this.kid = kid;
		}
		public Map<String, Bucket> getBucketInfo()
		{
			return bucketInfo;
		}
		public void setBucketInfo(Map<String, Bucket> buckets)
		{
			this.bucketInfo = buckets;
		}
		public void addBucket(Bucket bucket)
		{
			if (bucketInfo == null)
				bucketInfo = new HashMap<>();
			bucketInfo.put(bucket.getBucketId(), bucket);
		}
		public Map<String, List<Transaction>> getTransactions()
		{
			return transactions;
		}
		public void setTransactions(Map<String, List<Transaction>> transactions)
		{
			this.transactions = transactions;
		}
		public void addTransaction(Transaction transaction)
		{
			if (transactions == null)
				transactions = new HashMap<>();
			if (transactions.get(transaction.getBucketId()) == null)
				transactions.put(transaction.getBucketId(), new ArrayList<>());
			transactions.get(transaction.getBucketId()).add(transaction);
		}
	}

	public void setViewToken(String viewToken)
	{
		this.viewToken = viewToken;
	}
	
	public String getViewToken()
	{
		return viewToken;
	}
}
