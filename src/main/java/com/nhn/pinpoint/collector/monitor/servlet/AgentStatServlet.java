package com.nhn.pinpoint.collector.monitor.servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

import com.nhn.pinpoint.collector.StatServer;

/**
 * FIXME 에이전트 통계를 확인할 수 있는 임시 servlet. collector를 servlet container 위에서 동작시키는
 * 시점에는 spring mvc controller로 구현하는게 좋겠음.
 * 
 * @author harebox
 * 
 */
@Deprecated
public class AgentStatServlet extends HttpServlet {

	private static final long serialVersionUID = 8843232240004199263L;

	@Autowired
	private StatServer statServer;
	
	private final byte[] CALLBACK_CLOSE = new String(");").getBytes();

	void jsonpCallback(HttpServletRequest req, HttpServletResponse res,
			String json) throws IOException {
		Map<String, String[]> params = req.getParameterMap();

		if (params.containsKey("callback")) {
			res.setContentType("text/javascript;charset=UTF-8");

			ServletOutputStream out = res.getOutputStream();
			out.write(new String(params.get("callback")[0] + "(").getBytes());
			out.write(json.getBytes());
			out.write(CALLBACK_CLOSE);
			out.close();
		} else {
			res.setContentType("text/json;charset=UTF-8");
			ServletOutputStream out = res.getOutputStream();
			out.write(json.getBytes());
			out.close();
		}
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
//		Map<String, String[]> params = req.getParameterMap();
//		
//		if (params.containsKey("agentId")) {
//			String agentId = params.get("agentId")[0];
//			String json = statServer.getStore().getInJson(agentId);
//			if (json != null) {
//				jsonpCallback(req, res, json);
//			} else {
//				jsonpCallback(req, res, "{\"error\": \"not found : " + agentId + "\"}");
//			}
//		} else {
//			jsonpCallback(req, res, statServer.getStore().getInJson());
//		}
	}

}
