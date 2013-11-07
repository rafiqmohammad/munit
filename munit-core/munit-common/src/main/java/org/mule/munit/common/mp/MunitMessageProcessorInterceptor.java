/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.munit.common.mp;

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.processor.MessageProcessor;
import org.mule.modules.interceptor.processors.AbstractMessageProcessorInterceptor;
import org.mule.modules.interceptor.processors.MessageProcessorBehavior;
import org.mule.munit.common.MunitCore;
import org.mule.munit.common.MunitUtils;

import net.sf.cglib.proxy.MethodProxy;

/**
 * <p>
 * It intercepts the {@link MessageProcessor#process(org.mule.api.MuleEvent)}  calls
 * </p>
 *
 * @author Mulesoft Inc.
 * @since 3.3.2
 */
public class MunitMessageProcessorInterceptor extends AbstractMessageProcessorInterceptor
{
    private String fileName;
    private String lineNumber;

    public Object process(Object obj, Object[] args, MethodProxy proxy) throws Throwable
    {
        MuleEvent event = (MuleEvent) args[0];

        MockedMessageProcessorManager manager = getMockedMessageProcessorManager();

        MunitMessageProcessorCall messageProcessorCall = buildCall(event);
        runSpyAssertion(manager.getBetterMatchingBeforeSpyAssertion(messageProcessorCall), event);

        registerCall(manager, messageProcessorCall);
        MessageProcessorBehavior behavior = manager.getBetterMatchingBehavior(messageProcessorCall);
        if (behavior != null)
        {
            if (behavior.getExceptionToThrow() != null)
            {
                runSpyAssertion(manager.getBetterMatchingAfterSpyAssertion(messageProcessorCall), event);
                throw behavior.getExceptionToThrow();
            }

            MunitUtils.copyMessage((DefaultMuleMessage) behavior.getReturnMuleMessage(), (DefaultMuleMessage) event.getMessage());

            runSpyAssertion(manager.getBetterMatchingAfterSpyAssertion(messageProcessorCall), event);
            return event;
        }


        Object o = proxy.invokeSuper(obj, args);
        runSpyAssertion(manager.getBetterMatchingAfterSpyAssertion(messageProcessorCall), (MuleEvent) o);
        return o;
    }


    private void registerCall(MockedMessageProcessorManager manager, MunitMessageProcessorCall messageProcessorCall)
    {
        manager.addCall(messageProcessorCall);
    }

    private void runSpyAssertion(SpyAssertion spyAssertion, MuleEvent event)
    {
        if (spyAssertion == null)
        {
            return;
        }

        MunitUtils.verifyAssertions(event, spyAssertion.getMessageProcessors());
    }

    private MunitMessageProcessorCall buildCall(MuleEvent event)
    {
        MunitMessageProcessorCall call = new MunitMessageProcessorCall(id);
        call.setAttributes(getAttributes(event));
        call.setFlowConstruct(event.getFlowConstruct());
        call.setFileName(fileName);
        call.setLineNumber(lineNumber);
        return call;
    }


    protected MockedMessageProcessorManager getMockedMessageProcessorManager()
    {
        return ((MockedMessageProcessorManager) getMuleContext().getRegistry().lookupObject(MockedMessageProcessorManager.ID));
    }

    public MuleContext getMuleContext()
    {
        return MunitCore.getMuleContext();
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public void setLineNumber(String lineNumber)
    {
        this.lineNumber = lineNumber;
    }
}
