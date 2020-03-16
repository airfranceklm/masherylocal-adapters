package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.identity.CounterpartKeyDescriptor;
import com.airfranceklm.amt.sidecar.identity.CounterpartKeySet;
import com.airfranceklm.amt.sidecar.identity.PartyKeyDescriptor;
import com.airfranceklm.amt.sidecar.identity.ProcessorKeySet;
import com.airfranceklm.amt.yaml.YamlHelper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.airfranceklm.amt.sidecar.JsonHelper.convert;
import static com.airfranceklm.amt.sidecar.JsonHelper.toMap;
import static com.airfranceklm.amt.yaml.YamlHelper.loadAllYamlDocuments;

public class ALCPLoaders {

    public static ProcessorKeySet loadProcessorKeySet(Class<?> owner, String resource) {
        try (InputStream is = owner.getResourceAsStream(resource)) {
            return unmarshallProcessorKeySet(loadAllYamlDocuments(owner.getResourceAsStream(resource)));
        } catch (IOException ex) {
            return null;
        }
    }

    public static ProcessorKeySet loadProcessorKeySet(File yamlFile) {
        return unmarshallProcessorKeySet(loadAllYamlDocuments(yamlFile));
    }

    private static ProcessorKeySet unmarshallProcessorKeySet(Iterator<Object> docsIter) {
        if (docsIter == null || !docsIter.hasNext()) {
            return null;
        }

        ProcessorKeySet pks = convert(docsIter.next(), ProcessorKeySet.class);

        while (docsIter.hasNext()) {
            PartyKeyDescriptor pkd = convert(docsIter.next(), PartyKeyDescriptor.class);
            pks.addKey(pkd);
        }

        return pks;
    }

    public static void saveProcessorKeySet(File into, String areaId, PartyKeyDescriptor... keys) throws IOException {
        List<Map<?, ?>> docs = new ArrayList<>();
        ProcessorKeySet firstObj = ProcessorKeySet.buildProcessorKeySet()
                .areaId(areaId)
                .build();

        docs.add(toMap(firstObj));
        for (PartyKeyDescriptor pkd : keys) {
            docs.add(toMap(pkd));
        }

        YamlHelper.saveAsMultidocYaml(docs, into);
    }

    public static void saveToYaml(ProcessorKeySet pks, File yamlFile) throws IOException {
        List<Map<?, ?>> docs = new ArrayList<>();
        ProcessorKeySet firstObj = ProcessorKeySet.buildProcessorKeySet()
                .areaId(pks.getAreaId())
                .build();

        docs.add(toMap(firstObj));
        if (pks.getKeys() != null) {
            for (PartyKeyDescriptor pkd : pks.getKeys()) {
                docs.add(JsonHelper.toMap(pkd));
            }
        }

        YamlHelper.saveAsMultidocYaml(docs, yamlFile);
    }

    public static CounterpartKeySet loadCounterpartsFromYaml(File yamlFile) {
        Iterator<Object> docsIter = loadAllYamlDocuments(yamlFile);
        if (docsIter == null || !docsIter.hasNext()) {
            return null;
        }

        CounterpartKeySet cks = new CounterpartKeySet();

        while (docsIter.hasNext()) {
            CounterpartKeyDescriptor pkd = convert(docsIter.next(), CounterpartKeyDescriptor.class);
            cks.addKey(pkd);
        }

        return cks;
    }

    public static void saveToYaml(CounterpartKeySet pks, File yamlFile) throws IOException {
        List<Map<?, ?>> docs = new ArrayList<>();

        if (pks.getKeys() != null) {
            for (CounterpartKeyDescriptor pkd : pks.getKeys()) {
                docs.add(JsonHelper.toMap(pkd));
            }
        }

        YamlHelper.saveAsMultidocYaml(docs, yamlFile);
    }

    public static void saveCounterpartKeySet(File yamlFile, CounterpartKeyDescriptor... keys) throws IOException {
        List<Map<?, ?>> docs = new ArrayList<>();

        for (CounterpartKeyDescriptor pkd : keys) {
            docs.add(JsonHelper.toMap(pkd));
        }

        YamlHelper.saveAsMultidocYaml(docs, yamlFile);
    }
}
