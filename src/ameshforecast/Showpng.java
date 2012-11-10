package ameshforecast;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.*;

import com.google.appengine.api.datastore.Blob;

@SuppressWarnings("serial")
public class Showpng extends HttpServlet {
	private static final Logger log = Logger.getLogger(Fetchdata.class
			.getName());
	private static PersistenceManager pm = PMF.get().getPersistenceManager();

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		log.info("Showpng : called : " + req.getRequestURI());

		/* get_png */
		log.info("Showpng : get_png");
		String filename = req.getRequestURI().replace("/", "")
				.replace(".png", "");
		Pngdat png = null;
		Query query = pm.newQuery("select from "
				+ ameshforecast.Pngdat.class.getName()
				+ " where filename == \"" + filename + "\"");
		try {
			@SuppressWarnings("unchecked")
			List<Pngdat> results = (List<Pngdat>) query.execute();
			if (!results.isEmpty()) {
				png = results.get(0);
				log.info("Showpng : get_png : " + png.getFilename());
			} else {
				log.info("Showpng : get_png : no_png");
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.severe("Showpng : get_png : error");
		}

		/* display */
		log.info("Showpng : display");
		resp.setContentType("image/png");
		ServletOutputStream out = resp.getOutputStream();
		try {
			Blob blob = png.getContent();
			byte[] b = blob.getBytes();
			BufferedInputStream in = new BufferedInputStream(
					new ByteArrayInputStream(b));
			int len;
			while ((len = in.read(b, 0, b.length)) != -1) {
				out.write(b, 0, len);
			}
		} catch (IOException e) {
			e.printStackTrace();
			log.severe("Showpng : display : error");
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
