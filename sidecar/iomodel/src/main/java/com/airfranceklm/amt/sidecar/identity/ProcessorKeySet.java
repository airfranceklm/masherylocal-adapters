package com.airfranceklm.amt.sidecar.identity;

import com.airfranceklm.amt.sidecar.model.alcp.ALCPProcessorIdentity;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.IOException;
import java.net.NetworkInterface;
import java.util.List;

/**
 * Processor key set that is used to identify itself
 */
@NoArgsConstructor
public class ProcessorKeySet extends KeySet<PartyKeyDescriptor> {

    public static final KeyType CLUSTER_IDENTITY_TYPE = new KeyType("#identity");

    @JsonProperty("aid")
    @Getter
    @Setter
    private String areaId;

    public ProcessorKeySet(String areaId) {
        this.areaId = areaId;
    }

    @Builder(builderMethodName = "buildProcessorKeySet")
    public ProcessorKeySet(@Singular List<PartyKeyDescriptor> keys, String areaId) {
        super();
        this.areaId = areaId;

        this.setKeys(keys);
    }

    public void rehydrateKeys() {
        rehydrateKeys(passForMachine());
    }

    /**
     * Re-hydrates the private and public key by reading the content of the keys
     */
    public void rehydrateKeys(String pwd) {
        if (getKeys() != null) getKeys().forEach((key) -> {
            if (key != null) {
                key.rehydrate(pwd);
            }
        });
    }

    public PartyKeyDescriptor identity() {
        return getKeyByType(CLUSTER_IDENTITY_TYPE);
    }

    public ALCPProcessorIdentity alcpIdentity() {
        PartyKeyDescriptor pkd = getKeyByType(CLUSTER_IDENTITY_TYPE);
        if (pkd != null) {
            return new ALCPProcessorIdentity(areaId, pkd);
        } else {
            return null;
        }
    }

    public String passForMachine() {
        byte[] mac;
        try {
            mac = NetworkInterface.getByIndex(0).getHardwareAddress();
        } catch (IOException ex) {
            throw new IllegalStateException("Hardware address cannot be established");
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mac.length; i++) {
            sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
        }

        return sb.toString();
    }
}
