package listeners.messages;

import com.slack.api.app_backend.events.payload.EventsApiPayload;
import com.slack.api.bolt.App;
import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.bolt.handler.BoltEventHandler;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.event.MessageEvent;
import java.io.IOException;
import okhttp3.*;
import org.json.JSONObject;

public class SampleMessageListener implements BoltEventHandler<MessageEvent> {

    private final App app;

    public SampleMessageListener(App app) {
        this.app = app;
    }

    @Override
    public Response apply(EventsApiPayload<MessageEvent> payload, EventContext ctx)
            throws IOException, SlackApiException {
        this.app.executorService().submit(() -> {
            // Extract message text from the Slack event
            String messageText = payload.getEvent().getText();

            // Create a JIRA issue based on the message content
            String jiraIssueSummary = "TEST Slack Automation";
            String jiraIssueDescription = messageText;

            try {
                // Create the JIRA issue and get its key
                String issueKey = createJiraIssue(jiraIssueSummary, jiraIssueDescription);

                // Send a confirmation message to the Slack channel
                String confirmationMessage = "JIRA issue created with key: " + issueKey;
                sendMessageToSlack(ctx, confirmationMessage);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SlackApiException e) {
                e.printStackTrace();
            }
        });
        return Response.ok();
    }

    // Method to create a JIRA issue
    private String createJiraIssue(String summary, String description) throws IOException {
        String jiraApiEndpoint = "https://everlightsolar.atlassian.net/rest/api/2/issue";
        // ** NEED EVERLIGHT JIRA API TOKEN String jiraApiToken =

        OkHttpClient httpClient = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        String requestBody = "{"
                + "\"fields\": {"
                + "\"project\": {\"key\": \"OB_105\"},"
                + "\"summary\": \"" + summary + "\","
                + "\"description\": \"" + description + "\""
                + "}"
                + "}";

        Request request = new Request.Builder()
                .url(jiraApiEndpoint)
                .addHeader("Authorization", "Bearer " + jiraApiToken)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(mediaType, requestBody))
                .build();

        try (okhttp3.Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to create a JIRA issue. Status code: " + response.code());
            } else {
                String responseBody = response.body().string();
                return new JSONObject(responseBody).getString("key");
            }
        }
    }

    // Method to send a message to the Slack channel
    private void sendMessageToSlack(EventContext ctx, String message) throws IOException, SlackApiException {
        ctx.say(message);
    }
}
