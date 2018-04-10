package org.onosproject.orchestrator.dismi.primitives;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;


/**
 * This is the SD-WAN service action.
 **/

@ApiModel(description = "This is the CDN action that requests the connection from a source to a network destination " +
        "(i.e. caches).",
        parent = ServiceAction.class)
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2016-03-22T15:29:51.886Z")
public class SDWAN extends ServiceAction {

    /**
     **/
    public SDWAN() {
        super();
    }

    /**
     **/
    public SDWAN source(Subject source) {
        this.source = source;
        return this;
    }


    @ApiModelProperty(required = true, value = "")
    @JsonProperty("source")
    public Subject getSource() {
        return this.source;
    }

    public void setSource(Subject source) {
        this.source = source;
    }


    /**
     **/
    public SDWAN destination(Subject destination) {
        this.destination = destination;
        return this;
    }


    @ApiModelProperty(required = true, value = "")
    @JsonProperty("destination")
    public Subject getDestination() {
        return this.destination;
    }

    public void setDestination(Subject destination) {
        this.destination = destination;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SDWAN path = (SDWAN) o;
        return Objects.equals(this.source, path.source) &&
                Objects.equals(this.destination, path.destination) &&
                super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), source, destination);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SDWAN {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        if (null != source) {
            sb.append("    source: ").append(toIndentedString(source)).append("\n");
        }
        if (null != destination) {
            sb.append("    destination: ").append(toIndentedString(destination)).append("\n");
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

