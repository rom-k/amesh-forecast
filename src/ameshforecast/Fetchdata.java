package ameshforecast;

import ameshforecast.Gifdat;
import ameshforecast.PMF;
import java.io.IOException;
import java.util.logging.Logger;
import javax.jdo.Query;
import javax.jdo.PersistenceManager;
import javax.servlet.ServletContext;
import javax.servlet.http.*;
import java.io.*;
import java.net.*;
import java.util.*;
import com.google.appengine.api.images.*;
import com.google.appengine.api.datastore.*;

@SuppressWarnings("serial")
public class Fetchdata extends HttpServlet {
	private static final Logger log = Logger.getLogger(Fetchdata.class.getName());
	private static PersistenceManager pm = PMF.get().getPersistenceManager();
	
	public Image getImage(String strUrl) {
		log.info("fetch : getImage : " + strUrl);
		try {
			InputStream fin = null;
			if (strUrl.substring(0,4).equals("http")) {
				URL url = new URL(strUrl);
				fin = url.openStream();
			}
			else {
				ServletContext context = getServletContext();
				fin = context.getResourceAsStream(strUrl);
			}
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			int len = 0;
			byte[] buf = new byte[1024];
			while ((len = fin.read(buf)) != -1) {
				bout.write(buf, 0, len);
			}
			byte[] bydata = bout.toByteArray();
			return ImagesServiceFactory.makeImage(bydata);
		} catch (IOException e) {
			e.printStackTrace();
			log.severe("fetch : getImage : error");
			return null;
		}
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		log.info("fetch : called : " + req.getRequestURI());

		/* mesh_index */
		log.info("fetch : mesh_index");
		String line = "";
		URL url = new URL("http://tokyo-ame.jwa.or.jp/scripts/mesh_index.js");
		HttpURLConnection urlconn = (HttpURLConnection) url.openConnection();
		urlconn.setRequestMethod("GET");
		urlconn.connect();
		if (urlconn.getResponseCode() == 200) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					urlconn.getInputStream()));
			line = reader.readLine();
			log.info("fetch : mesh_index : " + line);
			reader.close();
		} else {
			log.severe("fetch : mesh_index : access to " + url.toString()
					+ " failed with response code "
					+ String.valueOf(urlconn.getResponseCode()));
		}
		urlconn.disconnect();

		/* get_list_Web */
		log.info("fetch : get_list_Web");
		List<String> list = new ArrayList<String>();
		HashMap<String, Integer> mapWeb = new HashMap<String, Integer>();
		if (line != "") {
			line = line.substring(line.indexOf("[") + 1, line.indexOf("]"));
			log.info("fetch : get_list_Web : " + line);
			String[] ary = line.split(",");
			for (int i = 0; i < ary.length; i++) {
				list.add(ary[i].replace("\"", ""));
				mapWeb.put(ary[i].replace("\"", ""), 0);
			}
		}

		/* get_list_PMF */
		log.info("fetch : get_list_PMF");
		HashMap<String, Integer> mapPMF = new HashMap<String, Integer>();
		Query query = pm.newQuery(ameshforecast.Gifdat.class);
		try {
			@SuppressWarnings("unchecked")
			List<Gifdat> results = (List<Gifdat>) query.execute();
			String out = "";
			if (results.iterator().hasNext()) {
				for (Gifdat gif : results) {
					if (!mapWeb.containsKey(gif.getFilename())) {
						list.add(gif.getFilename());
					}
					mapPMF.put(gif.getFilename(), 0);
					out += gif.getFilename() + ",";
				}
			}
			log.info("fetch : get_list_PMF : " + out);
		} catch (Exception e) {
			e.printStackTrace();
			log.severe("fetch : get_list_PMF : error");
		}

		/* get_new */
		log.info("fetch : get_new");
		Collections.sort(list);
//		ImagesService imageSvc = ImagesServiceFactory.getImagesService();
		try {
//			Image frame = getImage("/frame.png");
			for (int i = 0; i < list.size(); i++) {
				String filename = list.get(i);
				if (mapWeb.containsKey(filename) && !mapPMF.containsKey(filename)) {
//					log.info("fetch : get_new : " + filename);
//					Image image = getImage("http://tokyo-ame.jwa.or.jp/mesh/100/" + filename + ".gif");
//					List<Composite> composites = new ArrayList<Composite>();
//					composites.add(ImagesServiceFactory.makeComposite(image, 0, 0, (float)0.5, Composite.Anchor.TOP_LEFT));
//					composites.add(ImagesServiceFactory.makeComposite(frame, 0, 0, 1, Composite.Anchor.TOP_LEFT));
//					Image mixed=imageSvc.composite(composites, image.getWidth(), image.getHeight(), 0);
//					Blob blobdata = new Blob(mixed.getImageData());
					url = new URL("http://tokyo-ame.jwa.or.jp/mesh/100/" + filename + ".gif");
					InputStream fin = url.openStream();
					ByteArrayOutputStream bout = new ByteArrayOutputStream();
					int len = 0;
					byte[] buf = new byte[1024];
					while ((len = fin.read(buf)) != -1) {
						bout.write(buf, 0, len);
					}
					byte[] bydata = bout.toByteArray();
					Blob blobdata = new Blob(bydata);
					Gifdat gifdata = new Gifdat(filename, blobdata);
					pm.makePersistent(gifdata);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.severe("fetch : get_new : error");
		}

		/* remove_old */
		log.info("fetch : remove_old");
		Integer iremove = Math.max(0, list.size() - 30);
		for (int i = 0; i < iremove; i++) {
			String filename = list.get(i);
			try {
				Gifdat gifdata = pm.getObjectById(Gifdat.class, filename);
				pm.deletePersistent(gifdata);
				log.info("fetch : remove_old : " + filename);
			} catch (Exception e) {
				e.printStackTrace();
				log.severe("fetch : remove_old : error");
			}
		}

		/* call_forecast */

	}
}
