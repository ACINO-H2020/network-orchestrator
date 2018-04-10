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

/**
 * Created by stejun on 12/2/16.
 */
public interface InternalIntentStateIface {
    boolean onSubmitForValidation();

    boolean onUpdateRequest();

    boolean onValidationFailure();

    boolean onValidationSuccess();

    boolean onSubmitForCompilation();

    boolean onCompilationFailure();

    boolean onCompilationSuccess();

    boolean onSubmitForInstallation();

    boolean onInstallationSuccess();

    boolean onInstallationFailure();

    boolean onWithdrawalRequest();

    boolean onSubmitForWithdrawal();

    boolean onWithdrawalSuccess();

    boolean onNegotiation(); // Added by Abdul

    InternalIntentState doSubmitForValidation();

    InternalIntentState doUpdateRequest();

    InternalIntentState doValidationFailure();

    InternalIntentState doValidationSuccess();

    InternalIntentState doSubmitForCompilation();

    InternalIntentState doCompilationFailure();

    InternalIntentState doCompilationSuccess();

    InternalIntentState doSubmitForInstallation();

    InternalIntentState doInstallationSuccess();

    InternalIntentState doInstallationFailure();

    InternalIntentState doWithdrawalRequest();

    InternalIntentState doSubmitForWithdrawal();

    InternalIntentState doWithdrawalSuccess();

    InternalIntentState doNegotiation(); // Added by Abdul

    DismiIntentState getUserState();
}
