import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

/** a test servlet to make sure the servlet container is working */
public class TestServlet extends HttpServlet {

    public void doGet(HttpServletRequest request,
		      HttpServletResponse response)
	throws ServletException, IOException {

	PrintWriter out = response.getWriter();
	out.println("Hello Greenstone!");
    }
}
