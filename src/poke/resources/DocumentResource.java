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
package poke.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.server.resources.Resource;
import poke.server.resources.ResourceUtil;
import eye.Comm.Finger;
import eye.Comm.Header.ReplyStatus;
import eye.Comm.Payload;
import eye.Comm.PayloadReply;
import eye.Comm.Request;
import eye.Comm.Response;

public class DocumentResource implements Resource {
	protected static Logger logger = LoggerFactory.getLogger("server DOC Resource");
	@Override

		public Response process(Request request, PayloadReply result) {


			// TODO add code to process the message/event received
			//System.out.println("Was here !!!!");
			logger.info("Document: " + request.getBody().getDoc().getDocName());

			Response.Builder rb = Response.newBuilder();

			// metadata
			rb.setHeader(ResourceUtil.buildHeaderFrom(request.getHeader(), ReplyStatus.SUCCESS, null));

			// payload
			PayloadReply.Builder pb = PayloadReply.newBuilder();
			Finger.Builder fb = Finger.newBuilder();
			fb.setTag("File Recieved !!");
			fb.setNumber(100);
			pb.setFinger(fb.build());
			rb.setBody(pb.build());
			Response reply = rb.build();
			return reply;

		}
	
}
