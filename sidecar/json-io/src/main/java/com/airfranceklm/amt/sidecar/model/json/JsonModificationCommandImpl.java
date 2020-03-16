package com.airfranceklm.amt.sidecar.model.json;

import com.airfranceklm.amt.sidecar.model.CallModificationCommand;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

import java.io.IOException;
import java.util.*;

import static com.airfranceklm.amt.sidecar.CommonExpressions.allocOrGet;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class JsonModificationCommandImpl extends JsonPayloadCarrier implements CallModificationCommand {

    @Getter
    @Setter
    protected List<String> dropHeaders;

    @Getter @Setter
    protected Map<String, Object> passFragments;
    @Getter @Setter
    protected List<String> dropFragments;

    public JsonModificationCommandImpl(@Singular TreeMap<String, String> passHeaders
            , @Singular Map<String, ?> jsonPayload
            , String payload
            , JsonNode json
            , Boolean base64Encoded
            , @Singular List<String> dropHeaders
            , @Singular Map<String, Object> passFragments
            , @Singular  List<String> dropFragments) {
        super(passHeaders, jsonPayload, payload, json, base64Encoded);
        this.dropHeaders = dropHeaders;
        this.passFragments = passFragments;
        this.dropFragments = dropFragments;
    }

    /**
     * Checks the type safety of the loaded maps.
     *
     * @throws IOException if the classes will not be compatible with the annotations of the enclosing maps.
     */
    public void checkTypeSafety() throws IOException {
        if (passHeaders != null) {
            for (Map.Entry<?, ?> entry : passHeaders.entrySet()) {
                if (entry.getValue() != null && !(entry.getValue() instanceof String)) {
                    throw new IOException(String.format("AddHeader Value for key %s (%s) is not String, but %s.",
                            entry.getKey(), entry.getValue(), entry.getValue().getClass().getName()));
                }
            }
        }

        if (dropHeaders != null) {
            for (Object entry : dropHeaders) {
                if (entry != null && !(entry instanceof String)) {
                    throw new IOException(String.format("DropHeaders Value %s is not String, but %s.",
                            entry, entry.getClass().getName()));
                }
            }
        }
    }

    public static List<String> allocOrGetDropHeaders(JsonModificationCommandImpl t) {
        return allocOrGet(t::getDropHeaders, t::setDropHeaders, ArrayList::new);
    }

    public static List<String> allocOrGetDropFragments(JsonModificationCommandImpl t) {
        return allocOrGet(t::getDropFragments, t::setDropFragments, ArrayList::new);
    }

    public static Map<String,Object> allocOrGetPassFragments(JsonModificationCommandImpl t) {
        return allocOrGet(t::getPassFragments, t::setPassFragments, HashMap::new);
    }

    @Override
    protected boolean containsOnlyNulls() {
        return super.containsOnlyNulls() &&
                (dropHeaders == null || dropHeaders.size() == 0) &&
                (passFragments == null || passFragments.size() == 0) &&
                (dropFragments == null || dropFragments.size() == 0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        JsonModificationCommandImpl that = (JsonModificationCommandImpl) o;
        return Objects.equals(this.getPassHeaders(), that.getPassHeaders()) &&
                Objects.equals(getDropHeaders(), that.getDropHeaders());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.getPassHeaders(), getDropHeaders());
    }


}
