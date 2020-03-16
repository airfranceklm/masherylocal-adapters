package com.airfranceklm.amt.sidecar.model.json;

import com.airfranceklm.amt.sidecar.model.ResponseModificationCommand;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonResponseModificationCommand extends JsonModificationCommandImpl implements ResponseModificationCommand {
    @Getter
    @Setter
    protected Integer statusCode;

    @Builder(toBuilder = true, builderMethodName = "buildModifyResponse")
    public JsonResponseModificationCommand(@Singular Map<String, String> passHeaders
            , Map<String, ?> jsonPayload
            , String payload
            , JsonNode json
            , Boolean base64Encoded
            , @Singular List<String> dropHeaders
            , @Singular Map<String, Object> passFragments
            , @Singular List<String> dropFragments
            , Integer statusCode) {
        super(asTreeMap(passHeaders), jsonPayload, payload, json, base64Encoded, dropHeaders, passFragments, dropFragments);
        this.statusCode = statusCode;
    }

    @Override
    public boolean containsOnlyNulls() {
        return super.containsOnlyNulls() && statusCode == null;
    }

    /**
     * Backward compatibility with Mashsery's Phase-1 dialect. Use {@link #setStatusCode(Integer)}
     * instead
     * @param code code to return to the application
     */
    @Deprecated
    public void setCode(int code) {
        setStatusCode(code);
    }
}
