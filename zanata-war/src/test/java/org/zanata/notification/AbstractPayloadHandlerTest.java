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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class AbstractPayloadHandlerTest {

    @Mock
    private AbstractPayloadHandler handler;

    @Mock
    private JmsMessagePayloadHandler jmsPayloadHandler;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    private <M extends Message> M mockMessage(Class<M> messageType) {
        Destination mockDestination = mock(Destination.class);
        when(mockDestination.toString()).thenReturn("Mock Destination");
        M mockMessage = mock(messageType);
        try {
            when(mockMessage.getJMSDestination()).thenReturn(mockDestination);
        } catch (JMSException e) {
            // This should not happen in mocked environment
            throw new RuntimeException(e);
        }
        return mockMessage;
    }

    @Test
    public void willSkipNullPayloads() {
        Mockito.reset(handler, jmsPayloadHandler);
        Map<Class, JmsMessagePayloadHandler> handlerMap =
                ImmutableMap.of(String.class, jmsPayloadHandler);
        when(handler.getPayloadHandlers()).thenReturn(handlerMap);

        for (Message message : Lists.newArrayList(
                mockMessage(TextMessage.class),
                mockMessage(Message.class),
                mockMessage(BytesMessage.class),
                mockMessage(MapMessage.class))) {
            handler.onMessage(message);
        }
        verifyZeroInteractions(jmsPayloadHandler);
    }

    @Test
    public void willSkipUnrecognizedPayloads() throws Exception {
        Mockito.reset(handler, jmsPayloadHandler);
        Map<Class, JmsMessagePayloadHandler> handlerMap =
                ImmutableMap.of(String.class, jmsPayloadHandler);
        when(handler.getPayloadHandlers()).thenReturn(handlerMap);

        ObjectMessage mockMessage = mock(ObjectMessage.class);
        when(mockMessage.getObject()).thenReturn(new Integer(1));

        handler.onMessage(mockMessage);
        verify(jmsPayloadHandler).handle(new Integer(1));
    }
}
