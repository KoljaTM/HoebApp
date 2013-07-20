package de.vanmar.android.hoebapp.test.mocking;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.client.methods.HttpUriRequest;

public class MockResponses {

	private static final Map<String, List<Object>> responseMapping = new HashMap<String, List<Object>>();
	private static final String BASE = "mocks/";
	private static List<Object> answerLog = new LinkedList<Object>();

	public static String forRequest(final HttpUriRequest request)
			throws IOException {
		final String requestString = request.getURI().toString();
		for (final String mappingKey : responseMapping.keySet()) {
			if (requestString.matches(mappingKey)) {
				final List<Object> answers = responseMapping.get(mappingKey);
				final Object answer;
				// remove answers from list until last one is reached
				if (answers.size() > 1) {
					answer = answers.remove(0);
				} else {
					answer = answers.get(0);
				}
				answerLog.add(answer);
				if (answer instanceof IOException) {
					throw (IOException) answer;
				} else {
					return BASE + answer.toString();
				}
			}
		}
		throw new IllegalArgumentException(
				"No mocked reply configured for request: " + requestString);
	}

	public static void forRequestDoAnswer(final String regex,
			final Object... answersToReturn) {
		List<Object> answers = responseMapping.get(regex);
		if (answers == null) {
			answers = new LinkedList<Object>();
		}
		answers.addAll(Arrays.asList(answersToReturn));
		responseMapping.put(regex, answers);
	}

	public static List<Object> getAnswerLog() {
		return answerLog;
	}

	public static void reset() {
		responseMapping.clear();
		answerLog.clear();
	}
}
