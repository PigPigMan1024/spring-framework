/*
 * Copyright 2002-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.beans.factory;

import org.springframework.lang.Nullable;

/**
 * 由 {@link BeanFactory} 中使用的对象实现的接口
 * ，这些对象本身就是单个对象的工厂。如果一个 bean 实现了这个接口，
 * 它就被用作一个对象暴露的工厂，而不是直接作为一个将暴露自己的 bean 实例。
 *
 * <p><b>NB: 实现此接口的 bean 不能用作普通 bean。<b> FactoryBean
 * 以 bean 样式定义，但为 bean 引用公开的对象 ({@link getObject()}) 始终是它创建的对象。
 *
 * <p>FactoryBeans 可以支持单例和原型，并且可以根据需要懒惰地或在启动时急切地创建对象。
 * {@link SmartFactoryBean} 接口允许公开更细粒度的行为元数据。
 *
 * <p>此接口在框架本身中大量使用，例如用于 AOP {@link org.springframework.aop.framework.ProxyFactoryBean}
 * 或 {@link org.springframework.jndi.JndiObjectFactoryBean}。它也可以用于自定义组件；然而，这仅适用于基础设施代码。
 *
 * <p>{@code FactoryBean} 是一个程序化合约。实现不应该依赖于注解驱动的注入或其他反射设施。
 * <b> {@link getObjectType()} {@link getObject()} 调用可能会在引导过程的早期到达，
 * 甚至在任何后处理器设置之前.如果您需要访问其他 bean，请实现 {@link BeanFactoryAware} 并以编程方式获取它们。
 *
 * <p><b>容器只负责管理FactoryBean实例的生命周期，而不是FactoryBean创建的对象的生命周期。
 * )} 将 <i>not<i> 被自动调用。相反，FactoryBean 应该实现 {@link DisposableBean}
 * 并将任何此类关闭调用委托给底层对象。
 *
 * <p>最后，FactoryBean 对象参与包含 BeanFactory 的 bean 创建同步。
 * 除了 FactoryBean 本身（或类似的）内部的延迟初始化之外，通常不需要内部同步。
 *
 * @param <T> the bean type
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.beans.factory.BeanFactory
 * @see org.springframework.aop.framework.ProxyFactoryBean
 * @see org.springframework.jndi.JndiObjectFactoryBean
 * @since 08.03.2003
 */
public interface FactoryBean<T> {

	/**
	 * 可以在 {@link org.springframework.beans.factory.config.BeanDefinition}
	 * 上设置 {@link org.springframework.core.AttributeAccessorsetAttribute set}
	 * 的属性的名称，以便工厂 bean 可以在可以时发出其对象类型的信号' t 是从工厂 bean 类推导出来的。
	 *
	 * @since 5.2
	 */
	String OBJECT_TYPE_ATTRIBUTE = "factoryBeanObjectType";


	/**
	 * 返回此工厂管理的对象的实例（可能是共享的或独立的）。 <p>与 {@link BeanFactory} 一样，
	 * 这允许同时支持 Singleton 和 Prototype 设计模式。 <p>如果在调用时此 FactoryBean
	 * 尚未完全初始化（例如因为它涉及循环引用），则抛出相应的 {@link FactoryBeanNotInitializedException}。
	 * <p>从 Spring 2.0 开始，FactoryBeans 被允许返回 {@code null} 对象。工厂会将此视为使用的正常值；
	 * 在这种情况下它不会再抛出 FactoryBeanNotInitializedException 。
	 * 鼓励 FactoryBean 实现现在酌情抛出 FactoryBeanNotInitializedException 自己。
	 *
	 * @return an instance of the bean (can be {@code null})
	 * @throws Exception in case of creation errors
	 * @see FactoryBeanNotInitializedException
	 */
	@Nullable
	T getObject() throws Exception;

	/**
	 * 返回此 FactoryBean 创建的对象类型，如果事先未知，则返回 {@code null}。
	 * <p>这允许人们在不实例化对象的情况下检查特定类型的 bean，例如在自动装配时。
	 * <p>在创建单例对象的实现的情况下，此方法应尽量避免创建单例；它应该提前估计类型。
	 * 对于原型，也建议在此处返回有意义的类型。 <p>这个方法可以在<i>之前<i>调用这个FactoryBean已经完全初始化。
	 * 它不能依赖于初始化期间创建的状态；当然，如果可用，它仍然可以使用这种状态。
	 * <p><b>注意：<b> 自动装配将简单地忽略返回 {@code null} 的 FactoryBeans。
	 * 因此，强烈建议使用 FactoryBean 的当前状态正确实现此方法。
	 *
	 * @return the type of object that this FactoryBean creates,
	 * or {@code null} if not known at the time of the call
	 * @see ListableBeanFactory#getBeansOfType
	 */
	@Nullable
	Class<?> getObjectType();

	/**
	 * 这个工厂管理的对象是否为单例,也就是说，
	 * {@link getObject()} 是否总是返回相同的对象（可以缓存的引用）？
	 * <p><b>注意：<b>如果 FactoryBean 指示持有单例对象，则从 {@code getObject()} 返回的对象可能会被拥有的 BeanFactory 缓存。
	 * 因此，除非 FactoryBean 始终公开相同的引用，否则不要返回 {@code true}。
	 * <p>FactoryBean 本身的单例状态一般会由所属的 BeanFactory 提供；通常，它必须在那里定义为单例。
	 * <p><b>注意：<b>此方法返回 {@code false} 并不一定表示返回的对象是独立的实例。
	 * 扩展 {@link SmartFactoryBean} 接口的实现可以通过其 {@link SmartFactoryBeanisPrototype()} 方法显式指示独立实例。
	 * 如果 {@code isSingleton()} 实现返回 {@code false}，
	 * 则简单地假定未实现此扩展接口的普通 {@link FactoryBean} 实现始终返回独立实例。
	 * <p>默认实现返回 {@code true}，因为 {@code FactoryBean} 通常管理一个单例实例。
	 *
	 * @return whether the exposed object is a singleton
	 * @see #getObject()
	 * @see SmartFactoryBean#isPrototype()
	 */
	default boolean isSingleton() {
		return true;
	}

}
