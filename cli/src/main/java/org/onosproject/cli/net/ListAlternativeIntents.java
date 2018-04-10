package org.onosproject.cli.net;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onlab.util.StringFilter;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.intent.ConnectivityIntent;
import org.onosproject.net.intent.Intent;
import org.onosproject.net.intent.IntentService;
import org.onosproject.net.intent.IntentState;
import org.onosproject.net.intent.Key;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.StreamSupport;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;

/**
 * Created by antonio on 15/06/17.
 */

/**
 * Lists the alternative solution for the negotiation of an intent.
 */
@Command(scope = "onos", name = "alternative-intent",
        description = "Lists the alternative solution for the negotiation of an intent")
public class ListAlternativeIntents extends AbstractShellCommand {

    @Argument(index = 0, name = "app",
            description = "Application ID",
            required = true, multiValued = false)
    String applicationIdString = null;

    @Argument(index = 1, name = "key",
            description = "Intent Key",
            required = true, multiValued = false)
    String keyString = null;

    // Color codes and style
    private static final String BOLD = "\u001B[1m";
    private static final String RESET = "\u001B[0m";

    private static final String CONSTRAINTS = BOLD + "Constraints:" + RESET + " %s";
    private static final String ID = BOLD + "Id:" + RESET + " %s";
    private static final String KEY = BOLD + "Key:" + RESET + " %s";
    private static final String STATE = BOLD + "State:" + RESET + " %s";
    private static final String TYPE = BOLD + "Intent type:" + RESET + " %s";
    private static final String APP_ID = BOLD + "Application Id:" + RESET + " %s";

    private IntentService service;

    @Override
    protected void execute() {
        service = get(IntentService.class);
        CoreService coreService = get(CoreService.class);

        ApplicationId appId = appId();
        if (!isNullOrEmpty(applicationIdString)) {
            appId = coreService.getAppId(applicationIdString);
            if (appId == null) {
                print("Cannot find application Id %s", applicationIdString);
                return;
            }
        }

        final Key key;
        if (keyString.startsWith("0x")) {
            // The intent uses a LongKey
            keyString = keyString.replaceFirst("0x", "");
            key = Key.of(new BigInteger(keyString, 16).longValue(), appId);
        } else {
            // The intent uses a StringKey
            key = Key.of(keyString, appId);
        }

        Intent intent = service.getIntent(key);
        if (intent != null) {
            IntentState state = service.getIntentState(intent.key());

            if (state.equals(IntentState.NEGOTIATION_REQ)) {

                List<Intent> intents = service.getAlternativeSolutions(key);

                if (outputJson()) {
                    print("%s", json(intents));
                } else {
                    for (Intent alternativeIntent : intents) {
                        String formatted = fullFormat(alternativeIntent, state);
                        print("%s\n", formatted);
                    }
                }
            } else {
                print("The intent key %s is not in negotiation state", key);
            }
        }
    }

    /*
 * Prints information about the intent state, given an intent.
 */
    private String fullFormat(Intent intent, IntentState state) {
        StringBuilder builder = new StringBuilder();
        builder.append(format(ID, intent.id()));
        if (state != null) {
            builder.append('\n').append(format(STATE, state));
        }
        builder.append('\n').append(format(KEY, intent.key()));
        builder.append('\n').append(format(TYPE, intent.getClass().getSimpleName()));
        builder.append('\n').append(format(APP_ID, intent.appId().name()));

        if (intent instanceof ConnectivityIntent) {
            ConnectivityIntent ci = (ConnectivityIntent) intent;
            if (ci.constraints() != null && !ci.constraints().isEmpty()) {
                builder.append('\n').append(format(CONSTRAINTS, ci.constraints()));
            }
        }

        return builder.toString();
    }

    /*
     * Produces a JSON array from the intents specified.
     */
    private JsonNode json(Iterable<Intent> intents) {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode result = mapper.createArrayNode();
        StreamSupport.stream(intents.spliterator(), false)
                .forEach(intent -> result.add(jsonForEntity(intent, Intent.class)));
        return result;
    }
}
