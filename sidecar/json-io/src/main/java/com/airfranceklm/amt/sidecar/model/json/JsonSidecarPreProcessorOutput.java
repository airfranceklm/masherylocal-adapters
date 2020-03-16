package com.airfranceklm.amt.sidecar.model.json;

import com.airfranceklm.amt.sidecar.model.SidecarPreProcessorOutput;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static com.airfranceklm.amt.sidecar.CommonExpressions.allocOrGet;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonSidecarPreProcessorOutput extends JsonAbstractSidecarOutput implements SidecarPreProcessorOutput {

    @Getter @Setter
    protected JsonRequestModificationCommand modify;
    @Getter @Setter @JsonIgnore
    protected Map<String,?> relayParams;
    @Getter
    protected JsonNode relay;
    @Getter @Setter
    protected JsonReplyCommand reply;

    public void setRelay(JsonNode relay) {
        this.relay = relay;

        if (relay != null) {
            this.relayParams = jsonToMap(relay);
        } else {
            this.relayParams = null;
        }
    }

    public JsonSidecarPreProcessorOutput modify(Consumer<JsonRequestModificationCommand.JsonRequestModificationCommandBuilder> cfg) {
        JsonRequestModificationCommand.JsonRequestModificationCommandBuilder builder =
                modify != null ? modify.toBuilder() : JsonRequestModificationCommand.buildModifyRequest();
        cfg.accept(builder);
        modify = builder.build();

        return this;
    }

    public JsonSidecarPreProcessorOutput reply(Consumer<JsonReplyCommand.JsonReplyCommandBuilder> cfg) {
        JsonReplyCommand.JsonReplyCommandBuilder builder =
                reply != null ? reply.asReplyBuilder() : JsonReplyCommand.buildReply();
        cfg.accept(builder);
        reply = builder.build();

        return this;
    }

    @Override
    public Serializable createSerializableRelayParameters() {
        return jsonToString(relay);
    }

    @Override
    public boolean relaysMessageToPostprocessor() {
        return relay != null;
    }

    @Override
    public SidecarPreProcessorOutput toSerializableForm() {
        this.relay = null;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        JsonSidecarPreProcessorOutput that = (JsonSidecarPreProcessorOutput) o;
        return Objects.equals(modify, that.modify) &&
                Objects.equals(relayParams, that.relayParams) &&
                Objects.equals(relay, that.relay) &&
                Objects.equals(reply, that.reply);
    }



    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), modify, relayParams, relay, reply);
    }

    public static JsonRequestModificationCommand allocOrGetModify(JsonSidecarPreProcessorOutput t) {
        return allocOrGet(t::getModify, t::setModify, JsonRequestModificationCommand::new);
    }

    public static JsonReplyCommand allocOrGetReply(JsonSidecarPreProcessorOutput t) {
        return allocOrGet(t::getReply, t::setReply, JsonReplyCommand::new);
    }

    public static JsonSidecarPreProcessorOutput preOut() {
        return new JsonSidecarPreProcessorOutput();
    }
}
