package uniregistrar.driver.servlet;

import jakarta.servlet.Servlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public abstract class AbstractServlet extends HttpServlet implements Servlet {

	private static final long serialVersionUID = 4703352432772089518L;

	static void sendResponse(HttpServletResponse response, int status, String contentType, String body) throws IOException {

		response.setStatus(status);

		if (contentType != null) response.setContentType(contentType);

		response.setHeader("Access-Control-Allow-Origin", "*");

		if (body != null) {

			PrintWriter writer = response.getWriter();
			writer.write(body);
			writer.flush();
			writer.close();
		}
	}
}
