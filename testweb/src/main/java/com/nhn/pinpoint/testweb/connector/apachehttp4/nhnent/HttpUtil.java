/*
 * @(#)HttpUtil.java $version 2011. 12. 14.
 *
 * Copyright 2007 NHN Corp. All rights Reserved. 
 * NHN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.nhn.pinpoint.testweb.connector.apachehttp4.nhnent;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

/**
 * 간편하게 http데이터를 가져올 수 있는 클래스<br/>
 * <br/>
 * 사용법은, <br/>
 * HttpUtil.url(String url).call(); <br/>
 * 이 기본이다.<br/>
 * 이렇게 호출을 하면, GET방식으로 해당 url을 호출하게 되며, 다른 옵션들은 url()과 call()사이에 넣어주면 된다<br/>
 * .<br/>
 * 최대 사용할 수 있는 파라메터는 다음과 같으며<br/>
 * HttpUtil.url(String
 * url).method(Method.POST).charset("EUC-KR").passSSLError(true
 * ).connectionTimeout(1000).readTimeout(1000).call();<br/>
 * 이중에서 자신이 원하는 파라메터들만 선택해서 사용할 수 있다.<br/>
 * 반드시 url()이 맨 먼저 와야 하고, call()이 맨 뒤에 와야 하며, 그외 다른 옵션들은 순서에 관계 없다.<br/>
 * <br/>
 * 각 옵션들의 디폴트 값은 다음과 같다.<br/>
 * method : Method.GET<br/>
 * charset : 페이지 기본값<br/>
 * passSSLError : false<br/>
 * connectionTimeout : 0 (JRE기본값)<br/>
 * readTimeout : 0 (무제한)<br/>
 * 
 * @author Xenos
 */
public class HttpUtil {
	private static Charset defaultCharset = Charset.forName("UTF-8");
	private static X509TrustManager IgnoreSLLErrorTrustManager = new X509TrustManager() {

		@Override
		public void checkClientTrusted(X509Certificate[] ax509certificate, String s) throws CertificateException {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] ax509certificate, String s) throws CertificateException {
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}

	};

	/**
	 * HttpUtil이 지원하는 Method
	 * 
	 * @author Xenos
	 */
	public enum Method {
		GET, POST
	};

	/**
	 * Static Util 클래스이므로, 생성자 막음
	 */
	private HttpUtil() {
	}

	/**
	 * HttpUtil을 사용하기 위해서 최초로 호출해야 하는 함수 이 함수는 Parameters를 반환한다.
	 * 
	 * @param theURL
	 *            호출하고자 하는 URL
	 * @return HttpUtil을 이용할 수 있는 파라메터들을 담고 있는 Parameters 클래스
	 * @throws WrongParameterException
	 *             theURL에 null이 들어왔을 경우 발생하는 Exception
	 */
	public static Parameters url(String theURL) throws HttpUtilException {
		return new Parameters(theURL);
	}

	/**
	 * theURL로 http(s)연결을 하여 응답값의 내용을 반환한다. JSON 형태의 파라미터로 요청하기 위한 method
	 * 
	 * @param theURL
	 *            연결할 http(s) 주소
	 * @param method
	 *            GET/POST
	 * @param charset
	 *            응답값의 charset(null이면 페이지 기본값)
	 * @param passSSLError
	 *            SSL오류를 무시할지 여부
	 * @param connectionTimeout
	 *            접속 제한시간 (0이면 JRE 기본값)
	 * @param readTimeout
	 *            데이터를 받아오는데 제한시간 (0이면 무제한)
	 * @param jsonEntity
	 *            넘겨주고자 하는 String 요소값
	 * @return 결과값의 InputStreamReader형태
	 * @throws HttpUtilException
	 */
	static InputStreamReader callUrl(String theURL, Method method, Charset charset, boolean passSSLError, int connectionTimeout, int readTimeout, String jsonEntity, Map<String, String> header) throws HttpUtilException {

		// 타임아웃을 설정
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, connectionTimeout);
		HttpConnectionParams.setSoTimeout(params, readTimeout);

		HttpClient httpClient;

		// SSL 오류 우회하는 설정
		if (passSSLError) {
			// 포트 번호를 추출
			int sslPort = getPortFromURL(theURL);

			try {
				SSLContext sslContext = SSLContext.getInstance("SSL");
				sslContext.init(null, new TrustManager[] { IgnoreSLLErrorTrustManager }, new SecureRandom());
				SSLSocketFactory sf = new SSLSocketFactory(sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
				Scheme httpsScheme = new Scheme("https", sslPort, sf);
				SchemeRegistry schemeRegistry = new SchemeRegistry();
				schemeRegistry.register(httpsScheme);

				ClientConnectionManager conman = new SingleClientConnManager(schemeRegistry);

				httpClient = new DefaultHttpClient(conman, params);
			} catch (NoSuchAlgorithmException en) {
				throw new HttpUtilException(en.toString(), en);
			} catch (KeyManagementException ek) {
				throw new HttpUtilException(ek.toString(), ek);
			}
		} else {
			httpClient = new DefaultHttpClient(params);
		}

		HttpRequestBase request = null;

		try {
			// Method를 결정
			switch (method) {
			case GET:
				request = new HttpGet(theURL);
				break;
			case POST:
				String[] splitURL = StringUtils.split(theURL, "?", 2);

				HttpPost requestPost = new HttpPost(splitURL[0]);

				if (jsonEntity == null) {
					requestPost.setEntity(makeFormEntity(splitURL.length < 2 ? null : splitURL[1], charset));
				} else {
					requestPost.setEntity(new StringEntity(jsonEntity, "application/json", "UTF-8"));
					requestPost.addHeader("Content-Type", "application/json");
					requestPost.addHeader("accept", "text/plain, application/json");
				}

				request = requestPost;
				break;
			}

			// 헤더를 추가
			if (header != null && header.size() > 0) {
				Set<Entry<String, String>> entrySet = header.entrySet();
				for (Entry<String, String> entry : entrySet) {
					request.addHeader(entry.getKey(), entry.getValue());
				}
			}

			HttpResponse response = httpClient.execute(request);
			int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode < 200 || statusCode >= 300) {
				throw new HttpUtilException(response.getStatusLine().getStatusCode() + ":" + response.getStatusLine().getReasonPhrase());
			}

			// HTML문서의 charset을 가져온다. 만약에 사용자가 charset을 지정 하였다면, 해당 charset으로 변경
			Charset encodingCharset = null;
			try {
				Header contentEncoding = response.getEntity().getContentEncoding();
				if (contentEncoding != null) {
					encodingCharset = Charset.forName(contentEncoding.getValue());
				}
			} catch (Exception e) {
				// 오류따윈 무시함
			}

			// 지정된 캐릭터셋이 있으면 설정
			if (charset != null) {
				encodingCharset = charset;
			}

			// 케리터셋이 결정되지 않았다면, 디폴트
			if (encodingCharset == null) {
				encodingCharset = defaultCharset;
			}

			return new InputStreamReader(response.getEntity().getContent(), encodingCharset);
		} catch (IOException e) {
			throw new HttpUtilException(e.toString(), e);
		}
	}

	/**
	 * 포트 번호 검색을 위한 urlPattern - 미리 컴파일 해둔다.
	 */
	private static Pattern urlPattern = Pattern.compile("^(https?):\\/\\/([^:\\/\\s]+)((:([^\\/]*))?(((\\/[^\\s/\\/]+)*)?\\/([^#\\s\\?]*)(\\?([^#\\s]*))?(#(\\w*))?)?)?$");

	/**
	 * url로부터 포트번호를 찾아서 반환한다.
	 * 
	 * @param url
	 *            포트번호를 찾을 url
	 * @return 포트번호
	 */
	private static int getPortFromURL(String url) {
		int returnPort = 80;

		try {
			// url처음이 http로 시작하지 않으면 http를 붙여준다.
			if (!url.toLowerCase().startsWith("http")) {
				url = "http://" + url;
			}

			// 포트번호를 검색한다.
			Matcher match = urlPattern.matcher(url);

			if (match.matches() && match.group(5) != null) {
				// 지정된 포트 번호가 있는 경우
				returnPort = Integer.parseInt(match.group(5));
			} else if (url.toLowerCase().startsWith("https://")) {
				// 지정된 포트는 없지만, https인 경우
				returnPort = 443;
			}
		} catch (Exception e) {
			/* 오류시엔 기본포트(80)으로 반환한다. */
		}

		return returnPort;
	}

	/**
	 * url의 queryString을 잘라서, UrlEncodedFormEntity 형태로 반환한다.
	 * 
	 * @param url
	 *            데이터를 잘라낼 url
	 * @return UrlEncodedFormEntity 형태로 변환된 데이터
	 */
	private static UrlEncodedFormEntity makeFormEntity(String queryString, Charset charset) {
		List<NameValuePair> postParams = new ArrayList<NameValuePair>();

		if (StringUtils.isNotEmpty(queryString)) {
			String[] data = queryString.split("&");

			for (String aData : data) {
				String[] splitData = StringUtils.split(aData, "=", 2);

				if (splitData.length < 2) {
					continue;
				}

				postParams.add(new BasicNameValuePair(splitData[0].trim(), splitData[1].trim()));
			}

		}

		try {
			if (charset != null) {
				return new UrlEncodedFormEntity(postParams, charset.toString());
			} else {
				return new UrlEncodedFormEntity(postParams);
			}
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
}
