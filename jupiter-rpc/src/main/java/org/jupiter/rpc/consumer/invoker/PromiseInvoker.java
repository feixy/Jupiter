/*
 * Copyright (c) 2015 The Jupiter Project
 *
 * Licensed under the Apache License, version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jupiter.rpc.consumer.invoker;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import org.jupiter.common.util.Reflects;
import org.jupiter.rpc.JClient;
import org.jupiter.rpc.consumer.dispatcher.Dispatcher;
import org.jupiter.rpc.consumer.promise.InvokePromise;
import org.jupiter.rpc.consumer.promise.JPromise;

import java.lang.reflect.Method;

import static org.jupiter.common.util.Preconditions.checkNotNull;

/**
 * Asynchronous call, {@link PromiseInvoker#invoke(Method, Object[])}
 * returns a default value of the corresponding method.
 *
 * jupiter
 * org.jupiter.rpc.consumer.invoker
 *
 * @author jiachun.fjc
 */
public class PromiseInvoker {

    private static final ThreadLocal<JPromise> promiseThreadLocal = new ThreadLocal<>();

    private final JClient client;
    private final Dispatcher dispatcher;

    public PromiseInvoker(JClient client, Dispatcher dispatcher) {
        this.client = client;
        this.dispatcher = dispatcher;
    }

    public static JPromise promise() {
        JPromise promise = checkNotNull(promiseThreadLocal.get(), "promise");
        promiseThreadLocal.remove();
        return promise;
    }

    @RuntimeType
    public Object invoke(@Origin Method method, @AllArguments @RuntimeType Object[] args) throws Throwable {
        InvokePromise promise = dispatcher.dispatch(client, method.getName(), args);
        promiseThreadLocal.set(promise);
        return Reflects.getTypeDefaultValue(method.getReturnType());
    }
}