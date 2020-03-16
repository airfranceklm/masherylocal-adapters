package com.airfranceklm.amt.sidecar.elements.impl;

import com.airfranceklm.amt.sidecar.elements.*;
import com.airfranceklm.amt.sidecar.elements.DataElement;
import com.airfranceklm.amt.sidecar.SidecarInvocationData;
import com.airfranceklm.amt.sidecar.JsonHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.mashery.trafficmanager.event.processor.model.ProcessorEvent;

import static com.airfranceklm.amt.sidecar.elements.DataElementSalience.FAIL_FAST;
import static com.airfranceklm.amt.sidecar.elements.ObjectElements.UserContextField;
import static com.airfranceklm.amt.sidecar.elements.impl.TokenElementsFactory.userContextLocator;
import static com.airfranceklm.amt.sidecar.model.SidecarInput.Accessor.allocOrGetParameterGroup;

/**
 * For the cases where the user context is expressed as JSON, it's possible to express constraints to such fields.
 */
public class UserContextFieldFactory {

    private static final String INTERMEDIARY_KEY = "UserContextAsJSON";

    private static ChainableExtractor<ProcessorEvent, JsonNode> convertUserContextToJSON = (ProcessorEvent ppe, SidecarInvocationData sid) -> {
        JsonNode retVal = sid.getIntermediary(INTERMEDIARY_KEY);
        if (retVal == null) {
            String mCtx = userContextLocator.apply(ppe);
            if (mCtx != null) {
                retVal = JsonHelper.parse(mCtx);
                sid.useIntermediary(INTERMEDIARY_KEY, retVal);
            }
        }

        return retVal;
    };

    private static DataElement<ProcessorEvent, String> createUserContextStringField(String param) {
        ChainedExtractionDataElement<ProcessorEvent, String> retVal
                = new ChainedExtractionDataElement<>(UserContextField, param, FAIL_FAST);

        ChainableExtractor<ProcessorEvent, String> extractor =
                convertUserContextToJSON.andThen((node, sid) -> {
                    if (node != null) {
                        JsonNode n = node.path(param);
                        if (!n.isMissingNode() && n.isTextual()) {
                            return n.textValue();
                        }
                    }

                    // In all other cases, null will be returned
                    return null;
                });

        retVal.extractUsing(extractor);
        retVal.convertUsing((userCtxFieldValue, sid) -> allocOrGetParameterGroup(sid.getInput(), UserContextField.getElementName()).put(param, userCtxFieldValue));

        return retVal;
    }

    public static void fill(ElementsFactoryBuilder b) {
        b.addCommonElement(UserContextField, UserContextFieldFactory::createUserContextStringField);
    }
}
