/*
* Copyright 2015 herd contributors
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.finra.herd.dao;

import java.util.Map;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageResult;

/**
 * A service for AWS SQS operations.
 */
public interface SqsOperations
{
    /**
     * Delivers a message to the specified queue.
     *
     * @param queueName the name of the Amazon SQS queue to which a message is sent
     * @param messageText the text of the message
     * @param messageAttributes the optional SQS message attributes
     * @param amazonSQS the client for accessing AWS SQS
     *
     * @return the result the send message operation returned by the service
     */
    public SendMessageResult sendMessage(String queueName, String messageText, Map<String, MessageAttributeValue> messageAttributes, AmazonSQS amazonSQS);
}
