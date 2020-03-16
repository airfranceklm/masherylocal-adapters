package com.airfranceklm.amt.testsupport;

import com.mashery.http.client.HTTPClientRequest;
import com.mashery.http.io.ContentSource;
import com.mashery.http.server.HTTPServerRequest;
import com.mashery.http.server.HTTPServerResponse;
import lombok.*;
import org.easymock.EasyMockSupport;

import java.util.function.Supplier;

import static com.airfranceklm.amt.testsupport.Mocks.allocOrGet;

public class TestContext<T extends MasheryProcessorTestCase> {

    @Getter
    private EasyMockSupport owner;
    @Getter
    private T testCase;

    @Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PRIVATE)
    private ContentSource clientContentSource;
    @Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PRIVATE)
    private ContentSource originContentSource;

    @Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PRIVATE)
    private HTTPServerRequest serverRequest;

    @Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PRIVATE)
    private HTTPClientRequest apiOriginRequest;

    @Getter @Setter
    private HTTPServerResponse httpServerResponse;


    public TestContext(@NonNull EasyMockSupport owner, @NonNull T testCase) {
        this.owner = owner;
        this.testCase = testCase;
    }

    public HTTPServerRequest allocOrGetServerRequest(Supplier<HTTPServerRequest> creator) {
        return allocOrGet(this::getServerRequest, this::setServerRequest, creator);
    }

    public HTTPClientRequest allocOrGetAPIOriginRequest(Supplier<HTTPClientRequest> creator) {
        return allocOrGet(this::getApiOriginRequest, this::setApiOriginRequest, creator);
    }

    public ContentSource allocOrGetClientContentSource(Supplier<ContentSource> creator) {
        return allocOrGet(this::getClientContentSource, this::setClientContentSource, creator);
    }

    public ContentSource allocOrGetOriginContentSource(Supplier<ContentSource> creator) {
        return allocOrGet(this::getOriginContentSource, this::setOriginContentSource, creator);
    }


}
