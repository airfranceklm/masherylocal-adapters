package com.airfranceklm.amt.sidecar.model.json;

import com.airfranceklm.amt.sidecar.model.RequestModificationCommand;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.airfranceklm.amt.sidecar.CommonExpressions.allocOrGet;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class JsonRequestModificationCommand extends JsonModificationCommandImpl implements RequestModificationCommand {

    @Getter @Setter
    protected JsonRequestRoutingChangeBean changeRoute;

    @Builder(toBuilder = true, builderMethodName = "buildModifyRequest")
    public JsonRequestModificationCommand(@Singular Map<String, String> passHeaders
            , Map<String, ?> jsonPayload
            , String payload
            , JsonNode json
            , Boolean base64Encoded
            , @Singular List<String> dropHeaders
            , @Singular Map<String, Object> passFragments
            , @Singular List<String> dropFragments
            , JsonRequestRoutingChangeBean changeRoute) {
        super(asTreeMap(passHeaders), jsonPayload, payload, json, base64Encoded, dropHeaders, passFragments, dropFragments);
        this.changeRoute = changeRoute;
    }

    public boolean containsNullsOnly() {
        return super.containsOnlyNulls() &&
                (changeRoute == null || changeRoute.containsOnlyNulls());
    }

    public static JsonRequestRoutingChangeBean allocOrGetChangeRoute(@NonNull JsonRequestModificationCommand t) {
        return allocOrGet(t::getChangeRoute, t::setChangeRoute, JsonRequestRoutingChangeBean::new);
    }
}
