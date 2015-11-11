package in.lubetk.allowance;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import in.lubetk.allowance.command.CreateFamily;
import junit.framework.TestCase;

public class JsonMarshallingTest
{
	@Test
	public void testCreateFamily() throws JsonParseException, JsonMappingException, IOException
	{
		String json = "{ \"command\":\"CreateFamily\", \"name\":\"Lubetkin\", \"parentName\":\"Dad\", \"emailAddress\":\"jefflub@example.com\", \"password\":\"foobar\" }";
		ObjectMapper mapper = new ObjectMapper();
		CommandBase command = mapper.readValue(json, CommandBase.class);
		TestCase.assertTrue(command instanceof CreateFamily);
		CreateFamily createFamily = (CreateFamily)command;
		TestCase.assertEquals("Lubetkin", createFamily.getName());
		TestCase.assertEquals("Dad", createFamily.getParentName());
		TestCase.assertEquals("jefflub@example.com", createFamily.getEmailAddress());
		TestCase.assertEquals("foobar", createFamily.getPassword());
	}
}
