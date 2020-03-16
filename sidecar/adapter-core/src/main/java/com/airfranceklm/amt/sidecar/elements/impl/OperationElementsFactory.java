package com.airfranceklm.amt.sidecar.elements.impl;

import com.airfranceklm.amt.sidecar.elements.*;
import com.airfranceklm.amt.sidecar.model.SidecarInput;
import com.airfranceklm.amt.sidecar.model.SidecarInputOperation;
import com.mashery.http.ParamGroup;
import com.mashery.trafficmanager.event.processor.model.ProcessorEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static com.airfranceklm.amt.sidecar.elements.DataElementSalience.FAIL_FAST;
import static com.airfranceklm.amt.sidecar.elements.ObjectElements.FullOperation;
import static com.airfranceklm.amt.sidecar.elements.ObjectElements.Operation;
import static com.airfranceklm.amt.sidecar.elements.StringElements.HttpVerb;
import static com.airfranceklm.amt.sidecar.elements.StringElements.ResourcePath;
import static com.airfranceklm.amt.sidecar.model.SidecarInput.Accessor.allocOrGetOperation;

/**
 * Factory supplying the definition of the following data elements:
 * <ul>
 *     <li><code>operation</code>, which extracts the complete {@link SidecarInputOperation} object</li>
 *     <li><code>httpVerb</code>, which extracts only http verb being used by the client</li>
 *     <li><code>resourcePath</code>, which extracts only the resource path the client was referring to</li>
 * </ul>
 */
public class OperationElementsFactory {

    private static DataElement<ProcessorEvent, SidecarInputOperation> createOperation(String param) {
        SimpleDataElement<ProcessorEvent, SidecarInputOperation> retVal = new SimpleDataElement<>(Operation, FAIL_FAST);

        retVal.extractWith(OperationElementsFactory::extractOperationFrom);

        retVal.convertUsing((v, sid) -> sid.getInput().setOperation(v));

        return retVal;
    }

    private static DataElement<ProcessorEvent, SidecarInputOperation> createFullOperation(String param) {
        SimpleDataElement<ProcessorEvent, SidecarInputOperation> retVal = new SimpleDataElement<>(Operation, FAIL_FAST);

        retVal.extractWith(OperationElementsFactory::extractFullOperationFrom);
        retVal.convertUsing((v, sid) -> sid.getInput().setOperation(v));

        return retVal;
    }

    private static SidecarInputOperation extractFullOperationFrom(ProcessorEvent pe) {
        final SidecarInputOperation basic = extractOperationFrom(pe);
        basic.setUri(pe.getCallContext().getRequest().getHTTPRequest().getURI());
        return basic;
    }

    private static SidecarInputOperation extractOperationFrom(ProcessorEvent pe) {
        SidecarInputOperation op = new SidecarInputOperation();
        op.setHttpVerb(pe.getCallContext().getRequest().getHTTPRequest().getMethod().toLowerCase());
        op.setPath(pe.getCallContext().getRequest().getPathRemainder());

        final ParamGroup mashQueryData = pe.getCallContext().getRequest().getQueryData();
        if (mashQueryData != null) {
            Map<String, String> queryParams = new HashMap<>();
            for (String p : mashQueryData) {
                queryParams.put(p, mashQueryData.get(p));
            }
            op.setQuery(queryParams);
        }

        return op;
    }

    private static DataElement<ProcessorEvent, String> createResourcePath(String param) {
        SimpleDataElement<ProcessorEvent, String> retVal = new SimpleDataElement<>(ResourcePath, FAIL_FAST);

        retVal.extractWith((pe) -> pe.getCallContext().getRequest().getPathRemainder());
        retVal.convertUsing((v, sid) -> forNonNullElement(v, sid.getInput(), (op) -> op.setPath(v)));

        return retVal;
    }

    private static DataElement<ProcessorEvent, String> createHttpVerb(String param) {
        SimpleDataElement<ProcessorEvent, String> retVal = new SimpleDataElement<>(HttpVerb, FAIL_FAST);

        retVal.extractWith((pe) -> pe.getCallContext().getRequest().getHTTPRequest().getMethod().toLowerCase());
        retVal.convertUsing((v, sid) -> forNonNullElement(v, sid.getInput(), (op) -> op.setHttpVerb(v)));

        return retVal;
    }

    private static void forNonNullElement(String v, SidecarInput input, Consumer<SidecarInputOperation> consumer) {
        if (v != null) {
            consumer.accept(allocOrGetOperation(input));
        }
    }

    public static void fill(ElementsFactoryBuilder b) {
        b.addCommonElement(Operation, OperationElementsFactory::createOperation);
        b.addCommonElement(FullOperation, OperationElementsFactory::createFullOperation);
        b.addCommonElement(HttpVerb, OperationElementsFactory::createHttpVerb);
        b.addCommonElement(ResourcePath, OperationElementsFactory::createResourcePath);
    }
}
