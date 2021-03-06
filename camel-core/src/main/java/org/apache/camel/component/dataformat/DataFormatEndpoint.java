/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.dataformat;

import org.apache.camel.AsyncCallback;
import org.apache.camel.Component;
import org.apache.camel.Consumer;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultAsyncProducer;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.processor.MarshalProcessor;
import org.apache.camel.processor.UnmarshalProcessor;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.util.ServiceHelper;

@UriEndpoint(scheme = "dataformat")
public class DataFormatEndpoint extends DefaultEndpoint {

    private MarshalProcessor marshal;
    private UnmarshalProcessor unmarshal;
    @UriParam
    private DataFormat dataFormat;
    @UriParam
    private String operation;

    public DataFormatEndpoint() {
    }

    public DataFormatEndpoint(String endpointUri, Component component, DataFormat dataFormat) {
        super(endpointUri, component);
        this.dataFormat = dataFormat;
    }

    public DataFormat getDataFormat() {
        return dataFormat;
    }

    public void setDataFormat(DataFormat dataFormat) {
        this.dataFormat = dataFormat;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    @Override
    public Producer createProducer() throws Exception {
        return new DefaultAsyncProducer(this) {
            @Override
            public boolean process(Exchange exchange, AsyncCallback callback) {
                if (marshal != null) {
                    return marshal.process(exchange, callback);
                } else {
                    return unmarshal.process(exchange, callback);
                }
            }

            @Override
            public String toString() {
                return "DataFormatProducer[" + dataFormat + "]";
            }
        };
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        throw new UnsupportedOperationException("Cannot consume from data format");
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    protected void doStart() throws Exception {
        if (operation.equals("marshal")) {
            marshal = new MarshalProcessor(dataFormat);
            marshal.setCamelContext(getCamelContext());
        } else {
            unmarshal = new UnmarshalProcessor(dataFormat);
            unmarshal.setCamelContext(getCamelContext());
        }

        ServiceHelper.startServices(dataFormat, marshal, unmarshal);
        super.doStart();
    }

    @Override
    protected void doStop() throws Exception {
        ServiceHelper.stopServices(marshal, unmarshal, dataFormat);
        super.doStop();
    }
}
