import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;


public class ArxivParser {
	public static void Download(String path, String urlPath, boolean showProgress) {
		String[] temp = urlPath.split("/");
		String filename = temp[temp.length - 1];
		System.out.println("Start download file: " + filename);
		File file = new File(path, filename);
		try {
			int count = 0;
			int buffer_size = 8192;
			URL url = new URL(urlPath);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.connect();
			byte[] b = new byte[buffer_size];

			int l = 0;
			BufferedInputStream is = new BufferedInputStream(conn.getInputStream());
			FileOutputStream fos = new FileOutputStream(file);

			while ((l = is.read(b)) != -1) {
				fos.write(b, 0, l);
				count += buffer_size;
				if (showProgress)
					System.out.println("Download " + count + " bytes");
			}

			// Release space
			fos.close();
			is.close();
			conn.disconnect();
			System.out.println("Download Success !!");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Download Fail !!");
		}

	}

	public static PaperInfo GetPaperInfo(String urlPath){
		PaperInfo pinfo = new PaperInfo();
		String[] temp = urlPath.split("/");
		pinfo.id = temp[temp.length - 1];
		Document xmlDoc = null;
		
		URL url;

		try {
			url = new URL(urlPath);
			xmlDoc = Jsoup.parse(url, 3000);
			Elements title = xmlDoc.select("h1[class=title mathjax]");
			if(title.size()==0)
				return null;
			pinfo.title = title.get(0).text().split("Title: ")[1];

			Elements authors = xmlDoc.select("div[class=authors]");
			pinfo.authors = Arrays.asList(authors.get(0).text().split("Authors: ")[1].split(", "));

			Elements date = xmlDoc.select("div[class=dateline]");
			pinfo.date = String2Date(date.get(0).text());

			Elements abs = xmlDoc.select("blockquote[class=abstract mathjax]");
			pinfo.abs = abs.get(0).text().split("Abstract: ")[1];

			Elements subjects = xmlDoc.select("td[class=tablecell subjects]");
			String[] sub_list = subjects.get(0).text().split("; ");
			pinfo.subjects = new ArrayList<String>();
			for (int i = 0; i < sub_list.length; ++i) {
				//pinfo.subjects.add(sub_list[i].split("\\(")[1].split("\\)")[0]);
				pinfo.subjects.add(sub_list[i]);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return pinfo;
	}

	public static String[] SearchResult(String UrlPath){
		Document xmlDoc = null;
		String[] pid_list = null;
		try {
			URL url = new URL(UrlPath);
			xmlDoc = Jsoup.parse(url, 3000);
			Elements id_list = xmlDoc.select("a[title=Abstract]");
			pid_list = new String[id_list.size()];
			for (int i = 0; i < id_list.size(); ++i) {
				pid_list[i] = id_list.get(i).text().split(":")[1];
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return pid_list;
	}

	public static String BuildURL(String id, String type) {
		if (type == "pdf")
			return "https://arxiv.org/pdf/" + id + ".pdf";
		else
			return "https://arxiv.org/abs/" + id;
	}
	
	public static String KeywordGen(String keyword) {
		String [] klist = keyword.split(" ");
		String str = "";
		if(klist.length <= 1)
			str = keyword;
		else {
			for(int i=klist.length-1; i>1; --i)
				str += "AND+" + klist[i] + "+";
			str += "AND+" + klist[0] + "+" + klist[1];
		}
		return str;
	}
	
	public static String BuildSearchURL(String keyword, String type, String field, int skip) {
		String t;
		switch(type) {
		case "Titles":
			t = "ti";
			break;
		case "Authors":
			t = "au";
			break;
		case "Abstracts":
			t = "abs";
			break;
		default:
			t = "ti";
		}
		String req = "https://arxiv.org/find/" + field + "/1/" + t + ":+" + KeywordGen(keyword) + "/0/1/0/all/0/1?skip=" + skip;
		return req;
	}

	public static String BuildRecentURL(String sub, int skip, int show) {
		return "https://arxiv.org/list/" + sub + "/pastweek?skip=" + skip + "&show=" + show;
	}

	public static int [] String2Date(String dstr) {
		String[] dsplit = dstr.split("\\)")[0].split(" ");
		int year = Integer.valueOf(dsplit[4]);
		int day = Integer.valueOf(dsplit[2]);
		int month = 0;
		String month_str = dsplit[3];
		switch (month_str) {
		case "Jan":
			month = 1;
			break;
		case "Feb":
			month = 2;
			break;
		case "Mar":
			month = 3;
			break;
		case "Apr":
			month = 4;
			break;
		case "May":
			month = 5;
			break;
		case "Jun":
			month = 6;
			break;
		case "Jul":
			month = 7;
			break;
		case "Aug":
			month = 8;
			break;
		case "Sep":
			month = 9;
			break;
		case "Oct":
			month = 10;
			break;
		case "Nov":
			month = 11;
			break;
		case "Dec":
			month = 12;
			break;
		}
		int [] d = new int[3];
		d[0] = year;
		d[1] = month;
		d[2] = day;
		return d;
	}
}
