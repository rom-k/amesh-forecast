package ameshforecast;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.*;

@SuppressWarnings("serial")
public class MainServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(Fetchdata.class.getName());
	private static PersistenceManager pm = PMF.get().getPersistenceManager();

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		log.info("main : called : " + req.getRequestURI());

		/* get_png */
		log.info("main : get_png");
		String filename = null;
		Query query = pm.newQuery("select from " + ameshforecast.Pngdat.class.getName() + " order by filename desc range 0,1");
		try {
			@SuppressWarnings("unchecked")
			List<Pngdat> results = (List<Pngdat>) query.execute();
			if (!results.isEmpty()) {
				filename = results.get(0).getFilename();
				log.info("main : get_png : " + filename);
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.severe("main : get_png : error");
		}

		/* display */
		log.info("main : display");
		resp.setContentType("text/html; charset=utf-8");
		PrintWriter out = resp.getWriter();
		out.println("<!DOCTYPE html>");
		out.println("<html>");

		out.println("<head>");
		out.println("<title>amesh-forecast</title>");

		out.println("<style type=\"text/css\">");
		out.println("  html { height: 100% }");
		out.println("  body { height: 100%; margin: 0px; padding: 0px }");
		out.println("  #map_canvas { height: 100% }");
		out.println("</style>");

		out.println("<meta name=\"viewport\" content=\"initial-scale=1.0, user-scalable=no\" />");
		out.println("<script type=\"text/javascript\"");
		out.println("  src=\"http://maps.google.com/maps/api/js?sensor=false\">");
		out.println("</script>");

		out.println("<script type=\"text/javascript\">");
		out.println("//<![CDATA[");

		out.println("  function btnReload() {}");
		out.println("  btnReload.prototype = new GControl();");
		out.println("  myButton.prototype.initialize = function(map) {");
		out.println("    var myBtn = document.createElement(\"div\");");
		out.println("    myBtn.style.textDecoration = \"underline\";");
		out.println("    myBtn.style.color = \"#0000cc\";");
		out.println("    myBtn.style.backgroundColor = \"white\";");
		out.println("    myBtn.style.font = \"small Arial\";");
		out.println("    myBtn.style.border = \"1px solid black\";");
		out.println("    myBtn.style.padding = \"2px\";");
		out.println("    myBtn.style.marginBottom = \"3px\";");
		out.println("    myBtn.style.textAlign = \"center\";");
		out.println("    myBtn.style.width = \"140px\";");
		out.println("    myBtn.style.cursor = \"pointer\";");
		out.println("    myBtn.appendChild(document.createTextNode(\"Reload\"));");
		out.println("    GEvent.addDomListener(myBtn, \"click\", function() {");
    	out.println("      alert(\"これは自作ボタンです\");");
		out.println("    });");
		out.println("    var container = document.createElement(\"div\");");
  		out.println("    container.appendChild(myBtn);");
		out.println("    map.getContainer().appendChild(container);");
		out.println("    return container;");
		out.println("  }");
		out.println("  myButton.prototype.getDefaultPosition = function() {");
		out.println("    return new GControlPosition(G_ANCHOR_TOP_LEFT, new GSize(10, 15));");
		out.println("  }");

//		out.println("  function initialize() {");
		out.println("  function load() {");
		out.println("  var tokyoSt = new google.maps.LatLng(35.681382, 139.766084);");
		out.println("  var mopt = { zoom: 9, center: tokyoSt, mapTypeId: google.maps.MapTypeId.ROADMAP };");
		out.println("  var map = new google.maps.Map(document.getElementById(\"map_canvas\"), mopt);");

		if (filename != null) {
			out.println("  var bounds = new google.maps.LatLngBounds(");
			out.println("    new google.maps.LatLng(35.115556, 138.39928),");
			out.println("    new google.maps.LatLng(36.225442, 140.54820));");
			out.println("  var png = new google.maps.GroundOverlay(\"./" + filename + ".png\", bounds);");
			out.println("  png.setMap(map);");
		}

		out.println("  map.addControl(new btnReload());");

		out.println("  }");
		out.println("//]]>");
		out.println("</script>");

		out.println("</head>");

//		out.println("<body onload=\"initialize()\">");
		out.println("<body onload=\"load()\" onunload=\"GUnload()\">");
		out.println("<div id=\"map_canvas\" style=\"width:100%; height:100%\"></div>");
		out.println("</body>");

		out.println("</html>");
	}
}
