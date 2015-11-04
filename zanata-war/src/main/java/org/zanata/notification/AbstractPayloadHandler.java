/*
 * Copyright 2015, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.notification;

import java.io.Serializable;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Slf4j
public abstract class AbstractPayloadHandler implements MessageListener {

    protected abstract Map<Class, JmsMessagePayloadHandler>
            getPayloadHandlers();

    @Override
    public void onMessage(Message message) {
        Serializable payload = null;
        try {
            if (message instanceof ObjectMessage) {
                payload = ((ObjectMessage) message).getObject();
            } else if (message instanceof TextMessage) {
                payload = ((TextMessage) message).getText();
            } else {
                throw new RuntimeException("Unkown JMS message type: "
                        + message.getClass().getName());
            }
            
            if(payload == null) {
                log.warn("Ignoring null payload received in a JMS message in destination: "
                        + message.getJMSDestination().toString());
            }

            Map<Class, JmsMessagePayloadHandler> payloadHandlers =
                    getPayloadHandlers();
            JmsMessagePayloadHandler handler = null;
            // The class is directly indexed
            if (payloadHandlers.containsKey(payload.getClass())) {
                handler = payloadHandlers.get(payload.getClass());
            } else {
                // try to find the right handler:
                // the first one which handles a superclass of the received
                // payload
                for (Class payloadClass : payloadHandlers.keySet()) {
                    if (payloadClass.isInstance(payload)) {
                        handler = payloadHandlers.get(payloadClass);
                        break;
                    }
                }
            }

            // Payload handler not found
            if (handler == null) {
                throw new RuntimeException(
                        "Payload handler not found for payload type: "
                                + payload.getClass().getName());
            }

            handler.handle(payload);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }
}
