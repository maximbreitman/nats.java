/*******************************************************************************
 * Copyright (c) 2015-2016 Apcera Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT License (MIT)
 * which accompanies this distribution, and is available at
 * http://opensource.org/licenses/MIT
 *******************************************************************************/
/**
 * 
 */
package io.nats.client;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import static io.nats.client.Constants.*;
/*
 * This is the implementation of the AsyncSubscription interface.
 *
 */
class AsyncSubscriptionImpl extends SubscriptionImpl implements AsyncSubscription {

	private ExecutorService executor = null;
	private MessageHandler msgHandler;

	protected AsyncSubscriptionImpl(ConnectionImpl nc, String subj, String queue, MessageHandler cb, int max) {
		super(nc, subj, queue, max);
		this.msgHandler = cb;
	}

	@Override
	protected boolean processMsg(Message m) {
		Connection localConn;
		MessageHandler localHandler;
		long  localMax;

		mu.lock();
		try {
			localConn = this.getConnection();
			localHandler = this.msgHandler;
			localMax = this.max;
		} finally {
			mu.unlock();
		}

		// TODO Should this happen? And should it be handled differently?
		// the message handler has not been setup yet, drop the 
		// message.
		if (localHandler == null)
			return true;

		if (localConn == null)
			return false;

		long d = delivered.incrementAndGet();
		if (localMax <= 0 || d <= localMax) {
			try {
				localHandler.onMessage(m);
			} catch (Exception e) { }

			if (d == localMax) {
				try {
					unsubscribe();
				} catch (Exception e) {
				}
				this.conn = null;
			}
		}
		return true;
	}

	boolean isStarted()
	{
		return (executor != null);
	}


	void enable() {
		Runnable msgFeeder = new Runnable() {
			public void run(){
				try {
					conn.deliverMsgs(mch);
				} catch (Exception e) {
					e.printStackTrace();
				}
				logger.trace("msgFeeder has started for subj: {} sid: {}", subject, sid);
			}
		};

		if (!isStarted()) {
			executor = Executors.newSingleThreadExecutor(new NATSThreadFactory("msgfeeder"));
			executor.execute(msgFeeder);
			logger.trace("Started msgFeeder for subject: " + this.getSubject() + " sid: " + this.getSid());
		}
	}

	void disable() {
		if (isStarted())
		{
				executor.shutdownNow();
				executor = null;
		}			
	}

	@Override
	public void setMessageHandler(MessageHandler cb) {
		this.msgHandler = cb;
	}

	@Override
	public void start()
	{
		if (isStarted())
			return;

		if (!isValid())
			throw new IllegalStateException(ERR_BAD_SUBSCRIPTION);

		enable();

		conn.sendSubscriptionMessage(this);
	}

	@Override
	public void close() {
		super.close();
		disable();
	}}
