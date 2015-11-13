package in.lubetk.allowance.command;

import com.amazonaws.services.dynamodbv2.model.ListTablesResult;

import in.lubetk.allowance.CommandBase;
import in.lubetk.allowance.CommandResponse;

public class StatusReport extends CommandBase
{

	@Override
	public CommandResponse handleCommandInternal()
	{
		ListTablesResult tables = getAmazonDynamoDB().listTables();
		StringBuilder statusMsg = new StringBuilder();
		if ( tables != null && tables.getTableNames() != null )
		{
			statusMsg.append( "SUCCESS! Tables: " );
			for ( String s : tables.getTableNames() )
			{
				statusMsg.append( s + ", " );
			}
		}
		else
			statusMsg.append("NO TABLES!");
		
		return new StatusReportResponse(statusMsg.toString());
	}

	public static class StatusReportResponse extends CommandResponse
	{
		String message;
		
		public StatusReportResponse()
		{
			super();
		}
		
		public StatusReportResponse(String statusMessage)
		{
			this.message = statusMessage;
		}
		
		public String getMessage()
		{
			return message;
		}
		public void setMessage(String message)
		{
			this.message = message;
		}
	}
}
