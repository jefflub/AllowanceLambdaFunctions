package in.lubetk.allowance.command;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;

import in.lubetk.allowance.CommandBase;
import in.lubetk.allowance.CommandResponse;
import in.lubetk.allowance.db.DbUtils;

public class SetupDBs extends CommandBase
{
	@Override
	public CommandResponse handleCommand(AmazonDynamoDB db)
	{
		String status = "SUCCESS";
		try
		{
			DbUtils.setupTables(db);
		}
		catch (Exception ex)
		{
			status = ex.getMessage();
		}
		
		SetupDBsResponse response = new SetupDBsResponse();
		response.setStatus(status);
		return response;
	}
	
	public static class SetupDBsResponse extends CommandResponse
	{
		private String status;
		
		public String getStatus()
		{
			return status;
		}
		public void setStatus(String status)
		{
			this.status = status;
		}
	}

}
