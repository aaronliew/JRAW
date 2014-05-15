package net.dean.jraw;

import net.dean.jraw.models.Account;
import org.apache.http.HttpException;
import org.codehaus.jackson.JsonNode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class provides access to the most basic Reddit features such as logging in.
 */
public class RedditClient {

	/** The host that will be used in all the HTTP requests. */
	public static final String HOST = "www.reddit.com";

	/** The RestClient that will be used to execute various HTTP requests */
	private RestClient restClient;

	/**
	 * Instantiates a new RedditClient and adds the given user agent to the default headers of the RestClient
	 * @param userAgent The User-Agent header that will be sent with all the HTTP requests.
	 *                  <blockquote>Change your client's
	 *                  User-Agent string to something unique and descriptive, preferably referencing your reddit
	 *                  username. From the <a href="https://github.com/reddit/reddit/wiki/API">Reddit Wiki on Github</a>:
	 *                  <ul>
	 *                      <li>Many default User-Agents (like "Python/urllib" or "Java") are drastically limited to
	 *                          encourage unique and descriptive user-agent strings.</li>
	 *                      <li>If you're making an application for others to use, please include a version number in
	 *                          the user agent. This allows us to block buggy versions without blocking all versions of
	 *                          your app.</li>
	 *                      <li>NEVER lie about your user-agent. This includes spoofing popular browsers and spoofing
	 *                          other bots. We will ban liars with extreme prejudice.</li>
	 *                  </ul>
	 *                  </blockquote>
	 */
	public RedditClient(String userAgent) {
		this.restClient = new RestClient(HOST, userAgent);
	}

	/**
	 * Logs in to an account and returns the data associated with it
	 * @param username The username to log in to
	 * @param password The password of the username
	 * @return An Account object that has the same username as the username parameter
	 * @throws RedditException If there was an error returned in the JSON
	 */
	public Account login(String username, String password) throws RedditException {
		try {
			RestResponse loginResponse = restClient.post("/api/login/" + username,
					args("user", username, "passwd", password, "api_type", "json"));

			JsonNode errorsNode = loginResponse.getRootNode().get("json").get("errors");
			if (errorsNode.size() > 0) {
				throw new RedditException(errorsNode.get(0).asText());
			}

			return restClient.get("/api/me.json").to(Account.class);
		} catch (IOException | HttpException e) {
			e.printStackTrace();
		}

		return null;

	}

	/**
	 * Convenience method to combine a list of strings into a map. Sample usage:<br>
	 * <br>
     * <code>
	 * Map&lt;String, String&gt; mapOfArguments = args("key1", "value1", "key2", "value2");
	 * </code><br><br>
	 * would result in this:
	 * <pre>
	 * {@code
	 * {
	 *     "key1" => "value1",
	 *     "key2" => "value2"
	 * }
	 * }
	 * </pre>
	 *
	 * @param keysAndValues A list of strings to be condensed into a map. Must be of even length
	 * @throws java.lang.IllegalArgumentException If the lengths of the string array was not even
	 * @return A map of the given keys and values array
	 */
	public Map<String, String> args(String... keysAndValues) {
		if (keysAndValues.length % 2 != 0) {
			throw new IllegalArgumentException("Keys and values length must be even");
		}

		Map<String, String> args = new HashMap<>();
		for (int i = 0; i < keysAndValues.length;) {
			args.put(keysAndValues[i++], keysAndValues[i++]);
		}

		return args;
	}
}