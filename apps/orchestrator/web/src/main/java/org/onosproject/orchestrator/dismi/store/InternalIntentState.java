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

package org.onosproject.orchestrator.dismi.store;

import org.onosproject.orchestrator.dismi.primitives.DismiIntentState;
import org.slf4j.Logger;

import java.lang.reflect.Method;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by stejun on 12/2/16.
 */
public enum InternalIntentState implements InternalIntentStateIface {

    Submit {
        @Override
        public DismiIntentState getUserState() {
            return DismiIntentState.PROCESSING;
        }

        @Override
        public boolean onSubmitForValidation() {
            return true;
        }

        @Override
        public InternalIntentState doSubmitForValidation() {
            return InternalIntentState.Validating;
        }
    },

    Update {
        @Override
        public DismiIntentState getUserState() {
            return DismiIntentState.PROCESSING;
        }

        @Override
        public boolean onSubmitForValidation() {
            return true;
        }

        @Override
        public InternalIntentState doSubmitForValidation() {
            return InternalIntentState.Validating;
        }
    },

    Validating {
        @Override
        public DismiIntentState getUserState() {
            return DismiIntentState.PROCESSING;
        }

        @Override
        public boolean onValidationSuccess() {
            return true;
        }

        @Override
        public boolean onValidationFailure() {
            return true;
        }

        @Override
        public InternalIntentState doValidationSuccess() {
            return InternalIntentState.Validated;
        }

        @Override
        public InternalIntentState doValidationFailure() {
            return InternalIntentState.ValidationFailure;
        }
    },

    ValidationFailure {
        @Override
        public DismiIntentState getUserState() {
            return DismiIntentState.PROCESSING_FAILED;
        }

        @Override
        public boolean onUpdateRequest() {
            return true;
        }

    },

    Validated {
        @Override
        public DismiIntentState getUserState() {
            return DismiIntentState.PROCESSING;
        }

        @Override
        public boolean onSubmitForCompilation() {
            return true;
        }

        @Override
        public InternalIntentState doSubmitForCompilation() {
            return InternalIntentState.Compiling;
        }

    },

    Compiling {
        @Override
        public DismiIntentState getUserState() {
            return DismiIntentState.PROCESSING;
        }

        @Override
        public boolean onCompilationSuccess() {
            return true;
        }

        @Override
        public boolean onCompilationFailure() {
            return true;
        }

        @Override
        public InternalIntentState doCompilationSuccess() {
            return InternalIntentState.Compiled;
        }

        @Override
        public InternalIntentState doCompilationFailure() {
            return InternalIntentState.CompilationFailure;
        }
    },

    CompilationFailure {
        @Override
        public DismiIntentState getUserState() {
            return DismiIntentState.PROCESSING_FAILED;
        }

        @Override
        public boolean onUpdateRequest() {
            return true;
        }

    },

    Compiled {
        @Override
        public DismiIntentState getUserState() {
            return DismiIntentState.PROCESSING;
        }

        @Override
        public boolean onSubmitForInstallation() {
            return true;
        }

        @Override
        public InternalIntentState doSubmitForInstallation() {
            return InternalIntentState.Installing;
        }

    },

    Installing {
        @Override
        public DismiIntentState getUserState() {
            return DismiIntentState.INSTALLING;
        }

        @Override
        public boolean onInstallationSuccess() {
            return true;
        }

        @Override
        public boolean onInstallationFailure() {
            return true;
        }

        @Override
        public boolean onNegotiation() {
            return true;
        }

        @Override
        public InternalIntentState doInstallationSuccess() {
            return InternalIntentState.Installed;
        }

        @Override
        public InternalIntentState doInstallationFailure() {
            return InternalIntentState.InstallationFailed;
        }

        @Override
        public InternalIntentState doNegotiation() {
            return InternalIntentState.Negotiation;
        }
    },

    Negotiation {
        @Override
        public DismiIntentState getUserState() {
            return DismiIntentState.NEGOTIATION;
        }

        @Override
        public boolean onUpdateRequest() {
            return true;
        }

        @Override
        public InternalIntentState doUpdateRequest() {
            return InternalIntentState.Update;
        }

        // Following added by Abdul. We need to move state from Negotiation to Withdrawing
        @Override
        public boolean onSubmitForWithdrawal() {
            return true;
        }

        @Override
        public InternalIntentState doSubmitForWithdrawal() {
            return InternalIntentState.Withdrawing;
        }
    },


    InstallationFailed {
        @Override
        public DismiIntentState getUserState() {
            return DismiIntentState.FAILED;
        }

        @Override
        public boolean onSubmitForInstallation() {
            return true;
        }

        @Override
        public boolean onWithdrawalRequest() {
            return true;
        }

        @Override
        public InternalIntentState doSubmitForWithdrawal() {
            return InternalIntentState.WithdrawalRequest;
        }

        @Override
        public InternalIntentState doSubmitForInstallation() {
            return InternalIntentState.Installing;
        }
    },

    Installed {
        @Override
        public DismiIntentState getUserState() {
            return DismiIntentState.INSTALLED;
        }

        @Override
        public boolean onInstallationFailure() {
            return true;
        }

        @Override
        public boolean onWithdrawalRequest() {
            return true;
        }

        @Override
        public InternalIntentState doInstallationFailure() {
            return InternalIntentState.InstallationFailed;
        }

        @Override
        public InternalIntentState doWithdrawalRequest() {
            return InternalIntentState.WithdrawalRequest;
        }

        // ToDo Following added by Abdul. Rechek this code 
        @Override
        public boolean onSubmitForWithdrawal() {
            return true;
        }

        @Override
        public InternalIntentState doSubmitForWithdrawal() {
            return InternalIntentState.Withdrawing;
        }
    },

    WithdrawalRequest {
        @Override
        public DismiIntentState getUserState() {
            return DismiIntentState.WITHDRAWING;
        }

        @Override
        public boolean onSubmitForWithdrawal() {
            return true;
        }

        @Override
        public InternalIntentState doSubmitForWithdrawal() {
            return InternalIntentState.Withdrawing;
        }
    },

    Withdrawing {
        @Override
        public DismiIntentState getUserState() {
            return DismiIntentState.WITHDRAWING;
        }

        @Override
        public boolean onWithdrawalSuccess() {
            return true;
        }

        @Override
        public InternalIntentState doWithdrawalSuccess() {
            return InternalIntentState.Withdrawn;
        }
    },

    Withdrawn {
        @Override
        public DismiIntentState getUserState() {
            return DismiIntentState.WITHDRAWN;
        }
    };

    public boolean canChangeState(IntentFsmEvent event) {
        final Logger log = getLogger(getClass());
        try {
            // Google Guava is used here
            //log.info("====>on" + event.name());
            Method listener = this.getClass().getMethod("on" + event.name());
            return (Boolean) listener.invoke(this);
        } catch (Exception ex) {
            // Missing event handling or something went wrong
            throw new IllegalArgumentException("Event " + event.name() + " is not handled in the state machine", ex);
        }
    }

    public InternalIntentState getNextState(IntentFsmEvent event) {
        try {
            // Google Guava is used here
            Method listener = this.getClass().getMethod("do" + event.name());
            return (InternalIntentState) listener.invoke(this);
        } catch (Exception ex) {
            // Missing event handling or something went wrong
            throw new IllegalArgumentException("Event " + event.name() + " is not handled in the state machine", ex);
        }
    }


    public boolean onUpdateRequest() {
        return false;
    }

    public boolean onSubmitForValidation() {
        return false;
    }

    public boolean onValidationSuccess() {
        return false;
    }

    public boolean onValidationFailure() {
        return false;
    }

    public boolean onSubmitForCompilation() {
        return false;
    }

    public boolean onCompilationSuccess() {
        return false;
    }

    public boolean onCompilationFailure() {
        return false;
    }

    public boolean onSubmitForInstallation() {
        return false;
    }

    public boolean onInstallationSuccess() {
        return false;
    }

    public boolean onInstallationFailure() {
        return false;
    }

    public boolean onNegotiation() {
        return false;
    }

    public boolean onWithdrawalRequest() {
        return false;
    }

    public boolean onSubmitForWithdrawal() {
        return false;
    }

    public boolean onWithdrawalSuccess() {
        return false;
    }

    public InternalIntentState doSubmitForValidation() {
        throw new IllegalArgumentException();
    }

    public InternalIntentState doUpdateRequest() {
        throw new IllegalArgumentException();
    }

    public InternalIntentState doValidationFailure() {
        throw new IllegalArgumentException();
    }

    public InternalIntentState doValidationSuccess() {
        throw new IllegalArgumentException();
    }

    public InternalIntentState doSubmitForCompilation() {
        throw new IllegalArgumentException();
    }

    public InternalIntentState doCompilationFailure() {
        throw new IllegalArgumentException();
    }

    public InternalIntentState doCompilationSuccess() {
        throw new IllegalArgumentException();
    }

    public InternalIntentState doSubmitForInstallation() {
        throw new IllegalArgumentException();
    }

    public InternalIntentState doInstallationSuccess() {
        throw new IllegalArgumentException();
    }

    public InternalIntentState doInstallationFailure() {
        throw new IllegalArgumentException();
    }

    public InternalIntentState doNegotiation() {
        throw new IllegalArgumentException();
    }

    public InternalIntentState doWithdrawalRequest() {
        throw new IllegalArgumentException();
    }

    public InternalIntentState doSubmitForWithdrawal() {
        throw new IllegalArgumentException();
    }

    public InternalIntentState doWithdrawalSuccess() {
        throw new IllegalArgumentException();
    }

    public DismiIntentState getUserState() {
        return DismiIntentState.UNKNOWN;
    }

}
