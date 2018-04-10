package org.onosproject.net.intent;

import org.onosproject.net.intent.Intent;
import org.onosproject.net.intent.IntentException;

import java.util.List;
import com.google.common.base.MoreObjects;

/**
 * Created by antonio on 12/06/17.
 */
public class IntentNegotiationException extends IntentException {

    private static final long serialVersionUID = 1037345731049914733L;

    private final Intent originalIntent;
    private final List<Intent> alternativeSolutions;

    public IntentNegotiationException(Intent originalIntent, List<Intent> alternativeSolutions) {

        this.originalIntent = originalIntent;
        this.alternativeSolutions = alternativeSolutions;
    }

    public List<Intent> getAlternativeSolutions() {
        return this.alternativeSolutions;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("originalIntent", originalIntent)
                .add("alternativeSolutions", alternativeSolutions)
                .toString();
    }
}
