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
public class ShowGif extends HttpServlet {
	private static final Logger log = Logger.getLogger(Fetchdata.class
			.getName());
	private static PersistenceManager pm = PMF.get().getPersistenceManager();

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		log.info("ShowGif : called : " + req.getRequestURI());

		/* get_gif */
		log.info("ShowGif : get_gif");
		String filename = req.getRequestURI().replace("/", "")
				.replace(".gif", "");
		Gifdat gif = null;
		Query query = pm.newQuery("select from "
				+ ameshforecast.Gifdat.class.getName()
				+ " where filename == \"" + filename + "\"");
		try {
			@SuppressWarnings("unchecked")
			List<Gifdat> results = (List<Gifdat>) query.execute();
			if (!results.isEmpty()) {
				gif = results.get(0);
				log.info("ShowGif : get_gif : " + gif.getFilename());
			} else {
				log.info("ShowGif : get_gif : no_gif");
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.severe("ShowGif : get_gif : error");
		}

		/* display */
		log.info("ShowGif : display");
		resp.setContentType("image/gif");
		ServletOutputStream out = resp.getOutputStream();
		try {
			Blob blob = gif.getContent();
			byte[] b = blob.getBytes();
			BufferedInputStream in = new BufferedInputStream(
					new ByteArrayInputStream(b));
			int len;
			while ((len = in.read(b, 0, b.length)) != -1) {
				out.write(b, 0, len);
			}
		} catch (IOException e) {
			e.printStackTrace();
			log.severe("ShowGif : display : error");
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
