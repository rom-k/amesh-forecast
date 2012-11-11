package ameshforecast;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.*;

@SuppressWarnings("serial")
public class DateTimes extends HttpServlet {
	private static final Logger log = Logger.getLogger(Fetchdata.class
			.getName());
	private static PersistenceManager pm = PMF.get().getPersistenceManager();

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		log.info("DateTimes : called : " + req.getRequestURI());

		/* get_gif */
		log.info("DateTimes : get_gif");
		String out = "";
		Query query = pm.newQuery(ameshforecast.Gifdat.class);
		try {
			@SuppressWarnings("unchecked")
			List<Gifdat> results = (List<Gifdat>) query.execute();
			if (results.iterator().hasNext()) {
				for (Gifdat gif : results) {
					out += "," + gif.getFilename();
				}
			}
			if (out != "")
				out = out.substring(1);
			log.info("DateTimes : get_gif : " + out);
		} catch (Exception e) {
			e.printStackTrace();
			log.severe("DateTimes : get_gif : error");
		}

		resp.setContentType("text/plain; charset=utf-8");
		resp.getWriter().print(out);
	}
}
