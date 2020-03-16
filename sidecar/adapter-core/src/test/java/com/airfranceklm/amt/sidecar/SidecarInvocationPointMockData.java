package com.airfranceklm.amt.sidecar;

import com.airfranceklm.amt.sidecar.model.CallModificationCommand;
import com.airfranceklm.amt.sidecar.model.SidecarInput;
import com.airfranceklm.amt.sidecar.model.SidecarOutput;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public abstract class SidecarInvocationPointMockData<U extends CallModificationCommand, T extends SidecarOutput<U>> {
    String throwException;
    SidecarInput input;
    T output;

    T get() {
        return output;
    }

    protected abstract T create();

    public SidecarInput ensureInputObject(boolean createIfMissing) {
        if (input == null && createIfMissing) {
            input = new SidecarInput();
        }

        return input;
    }

    public T ensureOutputObject(boolean createIfMissing) {
        if (output == null && createIfMissing) {
            output = create();
        }

        return output;
    }

    @JsonProperty("throw exception")
    public String getThrowException() {
        return throwException;
    }

    public void setThrowException(String throwException) {
        this.throwException = throwException;
    }

    public SidecarInput getInput() {
        return input;
    }

    public void setInput(SidecarInput input) {
        this.input = input;
    }
}
