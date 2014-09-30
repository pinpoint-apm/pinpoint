package com.nhn.pinpoint.web.controller;

import org.apache.commons.lang.StringUtils;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.nhn.pinpoint.rpc.Future;
import com.nhn.pinpoint.rpc.ResponseMessage;
import com.nhn.pinpoint.rpc.server.ChannelContext;
import com.nhn.pinpoint.thrift.dto.TResult;
import com.nhn.pinpoint.thrift.dto.command.TCommandEcho;
import com.nhn.pinpoint.thrift.dto.command.TCommandTransfer;
import com.nhn.pinpoint.thrift.io.DeserializerFactory;
import com.nhn.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.nhn.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.nhn.pinpoint.thrift.io.SerializerFactory;
import com.nhn.pinpoint.web.server.PinpointSocketManager;

@Controller
@RequestMapping("/command")
public class CommandController {

	// FIX ME: 단순히 연동 테스트를 위해서 만든것 
	// 나중에 api같은게 정해지면 그때 이를 이용해서 정상적으로 api를 만들면 될듯
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private SerializerFactory commandSerializerFactory;

	@Autowired
	private DeserializerFactory commandDeserializerFactory;

	@Autowired
	private PinpointSocketManager socketManager;

	@RequestMapping(value = "/echo", method = RequestMethod.GET)
	public ModelAndView echo(@RequestParam("application") String applicationName, @RequestParam("agent") String agentId,
			@RequestParam("startTimeStamp") long startTimeStamp, @RequestParam("message") String message) throws TException {

		ChannelContext context = socketManager.getCollectorChannelContext(applicationName, agentId, startTimeStamp);

		if (context == null) {
			return createResponse(false, String.format("Can't find suitable ChannelContext(%s/%s/%d).", applicationName, agentId, startTimeStamp));
		}

		TCommandEcho echo = new TCommandEcho();
		echo.setMessage(message);

		HeaderTBaseSerializer serializer = commandSerializerFactory.createSerializer();
		byte[] payload = serializer.serialize(echo);

		TCommandTransfer transfer = new TCommandTransfer();
		transfer.setApplicationName(applicationName);
		transfer.setAgentId(agentId);
		transfer.setPayload(payload);

		Future<ResponseMessage> future = context.getSocketChannel().sendRequestMessage(serializer.serialize(transfer));
		future.await();

		String exceptionMessage = StringUtils.EMPTY;
		
		ResponseMessage responseMessage = future.getResult();
		try {
			HeaderTBaseDeserializer deserializer = commandDeserializerFactory.createDeserializer();

			TBase result = deserializer.deserialize(responseMessage.getMessage());

			if (result == null) {
				return createResponse(false, String.format("Can't get message from %s.", context));
			} else if (result instanceof TCommandEcho) {
				String messagee = ((TCommandEcho) result).getMessage();
				return createResponse(true, message);
			} else if (result instanceof TResult) {
				String messagee = ((TResult) result).getMessage();
				return createResponse(false, message);
			}

		} catch (TException e) {
			exceptionMessage = e.getMessage();
		}

		return createResponse(false, exceptionMessage);
	}

	
	private ModelAndView createResponse(boolean success, String message) {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("jsonView");

        if (success) {
        	mv.addObject("code", 0);
        } else {
        	mv.addObject("code", -1);
        }
        
        mv.addObject("message", message);
        
        return mv;
	}
	
}
