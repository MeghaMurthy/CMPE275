/*
 * copyright 2012, gash
 * 
 * Gash licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package poke.client;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.client.util.FileUtil;

import com.google.protobuf.ByteString;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.GeneratedMessage;

import eye.Comm.Document;
import eye.Comm.DocumentSet;
import eye.Comm.DocumentSet.Builder;
import eye.Comm.Finger;
import eye.Comm.Header;
import eye.Comm.NameSpace;
import eye.Comm.NameValueSet;
import eye.Comm.NameValueSet.NodeType;
import eye.Comm.Payload;
import eye.Comm.Request;

/**
 * provides an abstraction of the communication to the remote server.
 * 
 * @author gash
 * 
 */
public class ClientConnection {
	protected static Logger logger = LoggerFactory.getLogger("client");

	private String host;
	private int port;
	private ChannelFuture channel; // do not use directly call connect()!
	private ClientBootstrap bootstrap;
	ClientDecoderPipeline clientPipeline;
	private LinkedBlockingDeque<com.google.protobuf.GeneratedMessage> outbound;
	private OutboundWorker worker;

	protected ClientConnection(String host, int port) {
		this.host = host;
		this.port = port;

		init();
	}

	/**
	 * release all resources
	 */
	public void release() {
		bootstrap.releaseExternalResources();
	}

	public static ClientConnection initConnection(String host, int port) {

		ClientConnection rtn = new ClientConnection(host, port);
		return rtn;
	}

	/**
	 * add an application-level listener to receive messages from the server (as
	 * in replies to requests).
	 * 
	 * @param listener
	 */
	public void addListener(ClientListener listener) {
		try {
			if (clientPipeline != null)
				clientPipeline.addListener(listener);
		} catch (Exception e) {
			logger.error("failed to add listener", e);
		}
	}

	public void poke(String tag, int num) {
		// data to send
		Finger.Builder f = eye.Comm.Finger.newBuilder();
		f.setTag(tag);
		f.setNumber(num);

		// payload containing data
		Request.Builder r = Request.newBuilder();
		eye.Comm.Payload.Builder p = Payload.newBuilder();
		p.setFinger(f.build());
		r.setBody(p.build());

		// header with routing info
		eye.Comm.Header.Builder h = Header.newBuilder();
		h.setOriginator("client");
		h.setTag("test finger");
		h.setTime(System.currentTimeMillis());
		h.setRoutingId(eye.Comm.Header.Routing.FINGER);
		r.setHeader(h.build());

		eye.Comm.Request req = r.build();

		try {
			// enqueue message
			outbound.put(req);
		} catch (InterruptedException e) {
			logger.warn("Unable to deliver message, queuing");
		}
	}

	/* Added to send a document across */
	public void addDoc(String nameSpace, long id, String owner, String docName,
			byte[] fileContents) {

		try {
			NameSpace.Builder ns = eye.Comm.NameSpace.newBuilder();
			ns.setId(10);
			ns.setOwner(owner);
			ns.setName(nameSpace);

			Document.Builder d = eye.Comm.Document.newBuilder();
			d.setChunkContent(ByteString.copyFrom(fileContents));
			d.setDocName(docName);
			d.setId(id);

			// payload containing data
			eye.Comm.Payload.Builder p = Payload.newBuilder();
			p.setSpace(ns.build());
			p.setDoc(d.build());

			// header with routing info
			eye.Comm.Header.Builder h = Header.newBuilder();
			h.setOriginator("client");
			h.setTag("document upload");
			h.setTime(System.currentTimeMillis());
			h.setRoutingId(eye.Comm.Header.Routing.DOCADD);

			Request.Builder r = Request.newBuilder();
			r.setHeader(h.build());
			r.setBody(p.build());

			eye.Comm.Request req = r.build();

			// enqueue message
			outbound.put(req);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// Ask for the get document from the server.
	public void getDoc(String namespace, String documentName) {

		try {
			NameSpace.Builder ns = eye.Comm.NameSpace.newBuilder();
			ns.setId(10L);
			ns.setOwner("Ameya");
			ns.setName(namespace);
			ns.build();

			Document.Builder d = Document.newBuilder();
			d.setDocName(documentName);
			
			// payload containing data
			eye.Comm.Payload.Builder p = Payload.newBuilder();
			p.setSpace(ns.build());
			p.setDoc(d.build());
			
			Request.Builder r = Request.newBuilder();
			r.setBody(p.build());

			// header with routing info
			eye.Comm.Header.Builder h = Header.newBuilder();
			h.setOriginator("Client 1");
			h.setTag("Test NameSpace");
			h.setTime(System.currentTimeMillis());
			h.setRoutingId(eye.Comm.Header.Routing.DOCFIND);
			r.setHeader(h.build());
			eye.Comm.Request req = r.build();

			// enqueue message
			outbound.put(req);
		} catch (InterruptedException e) {
			logger.warn("Unable to deliver message, queuing");
		}
	}

	public void addNameSpace(String nameSpace) {
		try {
			NameSpace.Builder ns = eye.Comm.NameSpace.newBuilder();
			ns.setId(10L);
			ns.setOwner("Ameya");
			ns.setName(nameSpace);
			System.out.println("Namespace  --> " + ns.getName());
			ns.build();

			// payload containing data
			Request.Builder r = Request.newBuilder();
			eye.Comm.Payload.Builder p = Payload.newBuilder();
			p.setSpace(ns.build());
			r.setBody(p.build());

			// header with routing info
			eye.Comm.Header.Builder h = Header.newBuilder();
			h.setOriginator("Client 1");
			h.setTag("Test NameSpace");
			h.setTime(System.currentTimeMillis());
			h.setRoutingId(eye.Comm.Header.Routing.NAMESPACEADD);
			r.setHeader(h.build());
			eye.Comm.Request req = r.build();

			// enqueue message
			outbound.put(req);
		} catch (InterruptedException e) {
			logger.warn("Unable to deliver message, queuing");
		}
	}

	/**/

	private void init() {
		// the queue to support client-side surging
		outbound = new LinkedBlockingDeque<com.google.protobuf.GeneratedMessage>();

		// Configure the client.
		bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool()));

		bootstrap.setOption("connectTimeoutMillis", 10000);
		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("keepAlive", true);

		// Set up the pipeline factory.
		clientPipeline = new ClientDecoderPipeline();
		bootstrap.setPipelineFactory(clientPipeline);

		// start outbound message processor
		worker = new OutboundWorker(this);
		worker.start();
	}

	/**
	 * create connection to remote server
	 * 
	 * @return
	 */
	protected Channel connect() {
		// Start the connection attempt.
		if (channel == null) {
			// System.out.println("---> connecting");
			channel = bootstrap.connect(new InetSocketAddress(host, port));

			// cleanup on lost connection

		}

		// wait for the connection to establish
		channel.awaitUninterruptibly();

		if (channel.isDone() && channel.isSuccess())
			return channel.getChannel();
		else
			throw new RuntimeException(
					"Not able to establish connection to server");
	}

	/**
	 * queues outgoing messages - this provides surge protection if the client
	 * creates large numbers of messages.
	 * 
	 * @author gash
	 * 
	 */
	protected class OutboundWorker extends Thread {
		ClientConnection conn;
		boolean forever = true;

		public OutboundWorker(ClientConnection conn) {
			this.conn = conn;

			if (conn.outbound == null)
				throw new RuntimeException(
						"connection worker detected null queue");
		}

		@Override
		public void run() {
			Channel ch = conn.connect();
			if (ch == null || !ch.isOpen()) {
				ClientConnection.logger
						.error("connection missing, no outbound communication");
				return;
			}

			while (true) {
				if (!forever && conn.outbound.size() == 0)
					break;

				try {
					// block until a message is enqueued
					GeneratedMessage msg = conn.outbound.take();
					if (ch.isWritable()) {
						ClientHandler handler = conn.connect().getPipeline()
								.get(ClientHandler.class);

						if (!handler.send(msg))
							conn.outbound.putFirst(msg);

					} else
						conn.outbound.putFirst(msg);
				} catch (InterruptedException ie) {
					break;
				} catch (Exception e) {
					ClientConnection.logger.error(
							"Unexpected communcation failure", e);
					break;
				}
			}

			if (!forever) {
				ClientConnection.logger.info("connection queue closing");
			}
		}
	}
}
