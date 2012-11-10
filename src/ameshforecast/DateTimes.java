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

		/* get_png */
		log.info("DateTimes : get_png");
		String out = "";
		Query query = pm.newQuery(ameshforecast.Pngdat.class);
		try {
			@SuppressWarnings("unchecked")
			List<Pngdat> results = (List<Pngdat>) query.execute();
			if (results.iterator().hasNext()) {
				for (Pngdat png : results) {
					out += "," + png.getFilename();
				}
			}
			if (out != "")
				out = out.substring(1);
			log.info("DateTimes : get_png : " + out);
		} catch (Exception e) {
			e.printStackTrace();
			log.severe("DateTimes : get_png : error");
		}

		resp.setContentType("text/plain; charset=utf-8");
		resp.getWriter().print(out);
	}
}
