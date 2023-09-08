package listeners.messages;

import com.slack.api.bolt.App;
import java.util.regex.Pattern;
import listeners.ListenerProvider;

public class MessageListeners implements ListenerProvider {
    @Override
    public void register(App app) {
        Pattern envPattern = Pattern.compile("\\b.*(Zoom|Salesforce|App).*\\b");
        app.message(envPattern, new IssueMessageListener(app));
    }
}
