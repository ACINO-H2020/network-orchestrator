/*
 * Copyright (c) 2018 ACINO Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onosproject.orchestrator.dismi.validation;

import org.slf4j.Logger;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.slf4j.LoggerFactory.getLogger;

public class InputAssertion {

    private final Logger log = getLogger(getClass());

    public enum Type {TIME, BANDWIDTH}

    public enum IntentTime {FIXED, OPEN_ENDED, TERMINATION_TIME, SAME_START_END, BEFOR_START_END, FAIL}

    /**
     * @param intent
     * @return
     */
    public String assertIntent(String intent) {
        if (isAscii(intent)) {
            return removeEscapeChars(intent);
        }
        log.error("Invalid value " + intent + "");
        return null;
    }

    /**
     * @param intent
     * @return
     */
    private boolean isAscii(String intent) {
        Pattern p = Pattern.compile("\\A\\p{ASCII}*\\z");
        Matcher m = p.matcher(intent);
        return m.matches();
    }


    /**
     * @param intent
     * @return
     */
    private String removeEscapeChars(String intent) {
        // Remove white spaces \n, \t , add more if required
        intent = intent.replaceAll("\\s+", "").replace("\n", "").replace("\r", "");
        return intent;
    }

    public double resolveValue(String value, Type type) throws Exception {
        UnitsVocabulary unitsVocabulary = new UnitsVocabulary();
        Double factor = null;
        double numValue = 0;
        Pattern pattern = Pattern.compile("\\d+(\\.\\d+)?");
        Matcher m = null;
        m = pattern.matcher(value);
        try {
            if (m.find()) {
                numValue = Double.parseDouble(m.group(0));
                String unit = value.substring(m.group(0).length(), value.length()).toLowerCase();
                switch (type) {
                    case TIME:
                        // numValue check the upper limit
                        factor = unitsVocabulary.get(UnitsVocabulary.TIMEUNITS).get(unit);
                        break;
                    case BANDWIDTH:
                        // // numValue check the upper limit of bandwidth
                        factor = unitsVocabulary.get(UnitsVocabulary.BANDWIDTHUNITS).get(unit);
                        break;
                    default:
                        log.error("Exception: Unknown conversion type requested !");
                        throw new Exception("Unknown conversion type requested !");
                }
            }
        } catch (Exception exp) {
            log.error("*Exception: Unknown conversion type requested !");
            throw new Exception("Unknown conversion type requested !");
        }
        return factor * numValue;
    }

    public Double validatePercentage(String value) {
        if (value != null) {
            Pattern pattern = Pattern.compile("(\\d+(\\.\\d+%))");
            Matcher m = null;
            m = pattern.matcher(value);
            String temp = null;
            if (m.find()) {
                temp = m.group(0);
                return checkPercentLimit(temp, value);
            } else {
                pattern = Pattern.compile("((\\d+%))");
                m = null;
                m = pattern.matcher(value);
                if (m.find()) {
                    temp = m.group(0);
                    return checkPercentLimit(temp, value);
                } else {
                    log.error("Invalid value '" + value + "' !");
                }
            }
        }
        log.error("*Invalid value '" + value + "' !");
        return -1.0;
    }

    private Double checkPercentLimit(String temp, String value) {
        if (temp.endsWith("%") && value.endsWith("%")) {
            temp = value.substring(0, value.length() - 1);
            try {
                double percent = Double.parseDouble(temp);
                if (percent >= 0 && percent <= 100) {
                    return percent;
                } else {
                    log.error("**Invalid value '" + value + "' !");
                    return -1.0;
                }
            } catch (Exception exp) {
                log.error("***Invalid value '" + value + "' !");
                return -1.0;
            }
        } else {
            log.error("****Invalid value '" + value + "' !");
            return -1.0;
        }
    }

    public IntentTime compareDate(Date startTime, Date endTime) {

        // ii) A service with both start and end;
        if (startTime != null && endTime != null) {
            long diff = TimeUnit.MILLISECONDS.convert((endTime.getTime() - startTime.getTime()), TimeUnit.MILLISECONDS);

            if (diff == 0) {
                // both are same
                return IntentTime.SAME_START_END;
            } else if (diff < 0) {
                // end date/time is before the start time
                return IntentTime.BEFOR_START_END;
            }
            return IntentTime.FIXED;
        } else if (startTime != null) {
            // i) An open-ended service starting at a certain time but without a
            // specified ending;
            return IntentTime.OPEN_ENDED;
        } else if (endTime != null) {
            // A service terminating at a certain point
            return IntentTime.TERMINATION_TIME;
        } else {
            return IntentTime.FAIL;
        }
    }

    public boolean isValidateIP(final String ip) {
        if (null != ip) {
            Pattern pattern = Pattern.compile(
                    "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
            return pattern.matcher(ip).matches();
        } else {
            return false;
        }
    }

    public boolean isValidateMac(final String mac) {
        if (null != mac) {
            Pattern pattern = Pattern.compile("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$");
            return pattern.matcher(mac).matches();
        } else {
            return false;
        }
    }
}
