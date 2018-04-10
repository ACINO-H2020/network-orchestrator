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

package org.onosproject.orchestrator.dismi.primitives;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Tracker.
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2016-10-12T14:35:41.641Z")
public class Tracker {
    private Boolean isValid = true;

    private List<Issue> issueList = new ArrayList<Issue>();

    public void addTracker(Tracker t) {
        if (null == t) {
            return;
        }

        if (!t.isValid) {
            isValid = false;
        }

        if (null != t.issueList) {
            issueList.addAll(t.issueList);
        }
    }

    public Tracker isValid(Boolean isValid) {
        this.isValid = isValid;
        return this;
    }

    /**
     * Whether or not the submitted DISMI service is valid.
     *
     * @return isValid
     **/
    @ApiModelProperty(value = "Whether or not the submitted DISMI service is valid")
    public Boolean getIsValid() {
        return isValid;
    }

    public void setIsValid(Boolean isValid) {
        this.isValid = isValid;
    }

    public boolean isValid() {
        return isValid.booleanValue();
    }

    public void setInvalid() {
        isValid = false;
    }

    /**
     * @param issueList
     * @return
     */

    public Tracker issueList(List<Issue> issueList) {
        this.issueList = issueList;
        return this;
    }

    public Tracker addIssueListItem(Issue issueListItem) {
        this.issueList.add(issueListItem);
        return this;
    }

    public void addIssue(Issue issue) {
        addIssueListItem(issue);
    }

    public void addIssue(String primitive, Issue.SeverityEnum severity, Issue.ErrorTypeEnum errortype, String message) {
        issueList.add(new Issue(primitive, severity, errortype, message));
        if (Issue.SeverityEnum.ERROR == severity) {
            setInvalid();
        }
    }


    /**
     * List of informations, warnings and errors about the submitted DISMI
     * service.
     *
     * @return issueList
     **/
    @ApiModelProperty(value = "List of informations, warnings and errors about the submitted DISMI service")
    @JsonProperty("issueList")
    public List<Issue> getIssueList() {
        return issueList;
    }

    public void setIssueList(List<Issue> issueList) {
        this.issueList = issueList;
    }


    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Tracker tracker = (Tracker) o;
        return Objects.equals(this.isValid, tracker.isValid) &&
                Objects.equals(this.issueList, tracker.issueList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isValid, issueList);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Tracker {\n");

        sb.append("    isValid: ").append(toIndentedString(isValid)).append("\n");
        sb.append("    issueList: ").append(toIndentedString(issueList)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

    public String toString(int t) {
        String ret;
        String tab = "";
        int i;

        for (i = 0; i < t; i++) {
            tab += " ";
        }

        ret = tab + "Tracker is ";
        if (isValid) {
            ret += "valid\n";
        } else {
            ret += "invalid\n";
        }
        tab += "  ";

        for (i = 0; i < issueList.size(); i++) {
            ret += issueList.get(i).toString(tab);
        }

        return ret;
    }
}

