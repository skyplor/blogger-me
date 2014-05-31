package com.sky.bloggerme.events;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.EBean.Scope;

import com.squareup.otto.Bus;

/**
 * Singleton that holds the app-wide eventbus
 * 
 */
@EBean(scope = Scope.Singleton)
public class BusProvider
{
	private Bus eventBus;

	/**
	 * Lazy load the event bus
	 */
	public synchronized Bus getEventBus()
	{
		if (eventBus == null)
		{
			eventBus = new Bus();
		}

		return eventBus;
	}
}
