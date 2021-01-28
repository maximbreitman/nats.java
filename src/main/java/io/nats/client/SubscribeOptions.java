// Copyright 2020 The NATS Authors
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at:
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package io.nats.client;

import static io.nats.client.support.Validator.*;

/**
 * The SubscribeOptions class specifies the options for subscribing with jetstream enabled servers.
 * Options are created using the constructor or a {@link SubscribeOptions.Builder Builder}.
 */
public class SubscribeOptions {

    private final String stream;
    private final String consumer;
    private final ConsumerConfiguration consumerConfiguration;
    private final boolean autoAck;
    private final long pull;

    private SubscribeOptions(String stream, String consumer, ConsumerConfiguration consumerConfiguration, boolean autoAck, long pull) {
        this.stream = stream;
        this.consumer = consumer;
        this.consumerConfiguration = consumerConfiguration;
        this.autoAck = autoAck;
        this.pull = pull;
    }

    /**
     * Gets the name of the stream.
     * @return the name of the stream.
     */
    public String getStream() {
        return stream;
    }

    /** 
     * Gets automatic message acknowedgement for this subscriber.
     * @return true is auto ack is enabled, false otherwise.
     */
    public boolean isAutoAck() {
        return autoAck;
    }

    /**
     * Gets the consumer configuration.
     * @return the consumer configuration.
     */
    public ConsumerConfiguration getConsumerConfiguration() {
        return consumerConfiguration;
    }

    /**
     * Gets the attached consumer name.
     * @return the consumer name.
     */
    public String getConsumer() {
        return consumer;
    }

    public long getPullBatchSize() {
        return pull;
    }

    /**
     * Get a default instance of SubscribeOptions
     * @return the instance
     */
    public static SubscribeOptions getDefaultInstance() {
        return new Builder().build();
    }

    /**
     * Returns a copy of the subscribe options (for thread safety) or the default instance if
     * the supplied options are null
     * @param options null or an existing options to copy.
     * @return the new options.
     */
    public static SubscribeOptions createOrCopy(SubscribeOptions options) {
        if (options == null) {
            return new Builder().build();
        }

        return new SubscribeOptions(options.stream, options.consumer,
                new ConsumerConfiguration(options.consumerConfiguration),
                options.autoAck, options.pull);
    }

    /**
     * Creates a builder for the subscribe options.
     * @return the builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    // TODO
//    public static SubscribeOptions createPullDirect(String stream, String consumer, long batchSize) {
//        SubscribeOptions so = new SubscribeOptions();
//        so.stream = validateStreamName(stream);
//        so.consumer = validateConsumer(consumer);
//        so.pull = validatePullBatchSize(batchSize);
//        return so;
//    }

    /**
     * SubscribeOptions can be created using a Builder. The builder supports chaining and will
     * create a default set of options if no methods are calls.
     */
    public static class Builder {
        private String stream;
        private String consumer;
        private ConsumerConfiguration consumerConfig;
        private boolean autoAck = true;
        private String durable;
        private String deliverSubject;
        private long pull = 0;

        /**
         * Attaches to a consumer for the subscription.
         * @param stream the name of the stream
         * @param configuration the consumer configuration.
         * @return the builder
         */
        public Builder configuration(String stream, ConsumerConfiguration configuration) {
            this.stream = validateStreamName(stream);
            this.consumerConfig = configuration;
            return this;
        }

        /**
         * Attaches to a consumer for the subscription.  Will look up the stream
         * based on the subscription.
         * @param configuration the consumer configuration.
         * @return the builder
         */
        public Builder configuration(ConsumerConfiguration configuration) {
            this.consumerConfig = configuration;
            return this;
        }

        /**
         * Sets the durable name for the subscriber.
         * @param name the durable name
         * @return the builder
         */
        public Builder durable(String name) {
            this.durable = validateDurable(name);
            return this;
        }

        /**
         * Sets the auto ack mode of this subscriber.  It is off by default.  When disabled
         * the application must manually ack using one of the message's Ack methods.
         * @param value true enables auto ack, false disables.
         * @return the builder.
         */
        public Builder autoAck(boolean value) {
            this.autoAck = value;
            return this;
        }

        /**
         * Setting this enables pull based consumers.  This sets the batch size the server
         * will send.  Default is 0 (disabled).
         * @param batchSize number of messages to request in poll. 
         * @return the builder.
         */
        public Builder pull(long batchSize) {
            this.pull = validatePullBatchSize(batchSize);
            return this;
        }

        /**
         * Setting this enables pull based consumers.  This sets the batch size the server
         * will send.  Default is 0 (disabled).
         * @param stream the name of the stream
         * @param consumer the name of the consumer
         * @param batchSize number of messages to request in poll. 
         * @return the builder.
         */
        public Builder pullDirect(String stream, String consumer, long batchSize) {
            this.stream = validateStreamName(stream);
            this.consumer = validateConsumer(consumer);
            this.pull = validatePullBatchSize(batchSize);
            return this;
        }

        /**
         * Setting this specifies the push model to a delivery subject.
         * @param deliverSubject the subject to deliver on.
         * @return the builder.
         */
        public Builder pushDirect(String deliverSubject) {
            this.deliverSubject = validateDeliverSubject(deliverSubject);
            return this;
        }          

        /**
         * Attaches to an existing consumer.
         * @param stream the stream name.
         * @param consumer the consumer name.
         * @return the builder.
         */
        public Builder attach(String stream, String consumer) {
            this.stream = validateStreamName(stream);
            this.consumer = validateConsumer(consumer);
            return this;
        }

        /**
         * Builds the subscribe options.
         * @return subscribe options
         */
        public SubscribeOptions build() {
            if (consumerConfig == null) {
                consumerConfig = ConsumerConfiguration.builder().build();
            }

            if (durable != null) {
                consumerConfig.setDurable(durable);
            }

            if (deliverSubject != null) {
                consumerConfig.setDeliverSubject(deliverSubject);
            }

            return new SubscribeOptions(stream, consumer, consumerConfig, autoAck, pull);
        }
    }

    @Override
    public String toString() {
        return "SubscribeOptions{" +
                "stream='" + stream + '\'' +
                ", consumer='" + consumer + '\'' +
                ", " + consumerConfiguration +
                ", autoAck=" + autoAck +
                ", pull=" + pull +
                '}';
    }
}
