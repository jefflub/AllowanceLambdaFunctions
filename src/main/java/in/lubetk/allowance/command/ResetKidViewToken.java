package in.lubetk.allowance.command;

import in.lubetk.allowance.CommandBase;
import in.lubetk.allowance.CommandResponse;
import in.lubetk.allowance.db.Kid;

public class ResetKidViewToken extends CommandBase
{
	private String kidId;
	private Boolean sendEmail;
	
	@Override
	public CommandResponse handleCommandInternal()
	{
		Kid kid = Kid.loadFromId(getMapper(), kidId);
		kid.setViewToken(Kid.generateViewToken());
		getMapper().save(kid);
		
		return new ResetKidViewTokenResponse( kidId, kid.getViewToken() );
	}
	
	public static class ResetKidViewTokenResponse extends CommandResponse
	{
		private String kidId;
		private String viewToken;
		
		public ResetKidViewTokenResponse()
		{
			super();
		}
		
		public ResetKidViewTokenResponse(String kidId, String viewToken)
		{
			super();
			this.kidId = kidId;
			this.viewToken = viewToken;
		}
		
		public String getKidId()
		{
			return kidId;
		}
		public void setKidId(String kidId)
		{
			this.kidId = kidId;
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
	
	public String getKidId()
	{
		return kidId;
	}

	public void setKidId(String kidId)
	{
		this.kidId = kidId;
	}

	public Boolean getSendEmail()
	{
		return sendEmail;
	}

	public void setSendEmail(Boolean sendEmail)
	{
		this.sendEmail = sendEmail;
	}
}
