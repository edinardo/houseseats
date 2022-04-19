package com.sibilante.houseseats.resource;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import com.sibilante.houseseats.model.Show;
import com.sibilante.houseseats.service.IgnoreShowRepository;

@SuppressWarnings("serial")
@WebServlet(name = "HelloAppEngine", urlPatterns = { "/checkNewShows" })
public class CheckNewShowsServlet extends HttpServlet {

	private static final String LOGIN_URL = "https://lv.houseseats.com/member/index.bv";
	private static final String SHOWS_URL = "https://lv.houseseats.com/member/ajax/upcoming-shows.bv?sortField=name";
	private static final Logger logger = LoggerFactory.getLogger(CheckNewShowsServlet.class);
	private static final String OLD_SHOWS_ATTRIBUTE = "oldShows";
	@Autowired
	private IgnoreShowRepository ignoreShow;
	@Autowired
	private Environment properties;
	private Instant lastFindShows;

	@Override
	public void init() throws ServletException {
		super.init();
		// Handle the cookies for the org.jsoup.Connection or URLConnectons
		CookieHandler.setDefault(new CookieManager());
		try {
			getServletContext().setAttribute(OLD_SHOWS_ATTRIBUTE, getHouseSeatsShows());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		@SuppressWarnings("unchecked")
		final List<Show> oldShows = (List<Show>) getServletContext().getAttribute(OLD_SHOWS_ATTRIBUTE);
		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");	
	
		List<Show> currentShows = getHouseSeatsShows();
		List<Show> newShows = currentShows.stream()
				.filter(s -> !oldShows.contains(s))
				.collect(Collectors.toList());
		if (newShows.isEmpty()) {
			try {
				Thread.sleep(30000);
				currentShows = getHouseSeatsShows();
				newShows = currentShows.stream()
						.filter(s -> !oldShows.contains(s))
						.collect(Collectors.toList());
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}
		}

		if (!newShows.isEmpty()) {
			newShows.removeAll((Collection<?>) ignoreShow.findAll());
			for (Show newShow : newShows) {
				sendTelegram(newShow);
			}
		}

		getServletContext().setAttribute(OLD_SHOWS_ATTRIBUTE, new ArrayList<>(currentShows));
		response.getWriter().print(newShows);
	}
	
	/**
	 * Retrieve the list of the currently available shows.
	 * 
	 * Since the session duration is managed on the server side and seems that end after 20 minutes of inactivity,
	 * it will authentic just at the first call, or if the last call was executed more than 20 minutes ago.
	 * 
	 * @return The list of currently available shows.
	 * @throws IOException
	 */
	private List<Show> getHouseSeatsShows() {
		ArrayList<Show> showList = new ArrayList<>();
		try {
			if (lastFindShows == null || Duration.between(lastFindShows, Instant.now()).getSeconds() > 1200) {
				Jsoup.connect(LOGIN_URL)
					.data("submit", "login")
					.data("email", properties.getProperty("houseseats.email"))
					.data("password", properties.getProperty("houseseats.password"))
					.post();
			}
			Document doc = Jsoup.connect(SHOWS_URL)
					.get();
			
			Element showArea = doc.getElementById("grid-view-info");
			Elements showElements = showArea.getElementsByClass("panel-heading");
			for (Element showElement : showElements) {
				showList.add(new Show(Long.parseLong(showElement.select("a").first().attr("href").substring(23)),
						showElement.select("a").first().text()));
	
			}
		} catch (IOException e) {
			logger.error(e.getMessage(),e );
		}
		lastFindShows = Instant.now();
		return showList;
	}
	
	private void sendTelegram(Show show) {
		String urlString = "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s&parse_mode=%s";
		try {
			String text = URLEncoder.encode(
					"[" + show.getName() + "](https://lv.houseseats.com/member/tickets/view/?showid=" + show.getId() + ")",
					"UTF-8");
			String parseMode = "markdown";
			urlString = String.format(urlString,
					properties.getProperty("telegram.api.token"),
					properties.getProperty("telegram.chat.id"), text, parseMode);
	
			URL url = new URL(urlString);
			URLConnection conn = url.openConnection();
	
			StringBuilder sb = new StringBuilder();
			InputStream is = new BufferedInputStream(conn.getInputStream());
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String inputLine = "";
			while ((inputLine = br.readLine()) != null) {
				sb.append(inputLine);
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

}