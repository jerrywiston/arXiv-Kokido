import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;

public class OperationManager {
	private MainWindow window;
	private boolean isSearching;
	PaperInfoManager pinfoManager;

	/*---------------------------------
	Constructor
	---------------------------------*/
	public OperationManager(PaperInfoManager pm) {
		isSearching = false;
		pinfoManager = pm;
		createFolder("paper_file");
		createFolder("kido_file");
	}

	/*---------------------------------
	Set window
	---------------------------------*/
	public void setWindow(MainWindow w) {
		window = w;
	}
	
	/*---------------------------------
	Create folder
	---------------------------------*/
	public void createFolder(String folderName) {
		File theDir = new File(folderName);

		// if the directory does not exist, create it
		if (!theDir.exists()) {
		    System.out.println("creating directory: " + theDir.getName());
		    boolean result = false;

		    try{
		        theDir.mkdir();
		        result = true;
		    } 
		    catch(SecurityException se){
		        //handle it
		    }        
		    if(result) {    
		        System.out.println("DIR created");  
		    }
		}
	}
	
	/*---------------------------------
	Save PaperInfoMap
	---------------------------------*/
	public void SaveInfo() {
		pinfoManager.SaveInfo();
	}

	/*---------------------------------
	Save PaperInfoMap
	---------------------------------*/
	public void removeInfo(String id) {
		pinfoManager.RemoveInfo(id);
		pinfoManager.SaveInfo();
		refreshNode();
		removeFile(id);
	}

	public void removeFile(String id) {
		try {
			File file = new File("paper_file/" + id + ".pdf");
			if (file.delete()) {
				window.setState(file.getName() + " is deleted!");
			} else {
				window.setState("Delete operation is failed.");
			}
		} catch (Exception e) {

			e.printStackTrace();

		}
	}

	/*---------------------------------
	Set window
	---------------------------------*/
	public PaperInfo getInfo(String id) {
		return pinfoManager.getPaperInfoMap().get(id);
	}

	/*---------------------------------
	Set profile visible
	---------------------------------*/
	public void setProfileVisible(boolean b) {
		window.setProfileVisible(false);
	}

	
	
	/*---------------------------------
	View items
	---------------------------------*/
	public void view(String id, boolean showInBrowser) {
		String str;
		if (showInBrowser)
			str = "start " + ArxivParser.BuildURL(id, "pdf");
		else
			str = "start " + "paper_file/" + id + ".pdf";

		runCommand(str);
	}
	
	public void runCommand(String command) {
		ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", command);
		try {
			Process p = pb.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*---------------------------------
	Start search
	---------------------------------*/
	public void startSearch(String str, String type, String field, int total, int skip) {
		if (!isSearching) {
			if (skip <= 0)
				window.clearItem();

			isSearching = true;
			Thread t1 = new searchThread(str, type, field, total, skip);
			t1.start();
		}
	}
	
	/*---------------------------------
	Search items
	---------------------------------*/
	public String fieldStr(String str) {
		String fstr = null;
		
		switch(str) {
		case "CS":
			fstr = "grp_cs";
			break;
		case "Econ":
			fstr = "grp_econ";
			break;
		case "EESS":
			fstr = "grp_eess";
			break;
		case "Math":
			fstr = "grp_math";
			break;
		case "Phy":
			fstr = "grp_physics";
			break;
		case "Bio":
			fstr = "grp_q-bio";
			break;
		case "Fin":
			fstr = "grp_q-fin";
			break;
		case "Stat":
			fstr = "grp_stat";
			break;
		default:
			fstr = "all";
		}
		
		return fstr;
	}
	
	public void search(String str, String type, String field, int total, int skip) {
		if (str.length() <= 0) {
			isSearching = false;
			window.setState("");
			return;
		}

		String req = ArxivParser.BuildSearchURL(str, type, fieldStr(field), skip);
		window.setState("Searching... URL: " + req);

		String[] id_list = ArxivParser.SearchResult(req);
		if (total < id_list.length)
			id_list = Arrays.copyOfRange(id_list, 0, total);

		for (String id : id_list) {
			PaperInfo info_temp = ArxivParser.GetPaperInfo(ArxivParser.BuildURL(id, "abs"));
			if (info_temp == null)
				continue;
			ItemPanel p = window.addItem(info_temp);
			if (pinfoManager.hasKey(id))
				p.setDownloadBtn(false);
		}

		isSearching = false;
		window.setState("");
	}

	/*---------------------------------
	Search thread
	---------------------------------*/
	public class searchThread extends Thread {
		int total;
		int skip;
		String type;
		String field;
		String str;

		public searchThread(String s, String tp, String f, int t, int sk) {
			str = s;
			type = tp;
			field = f;
			total = t;
			skip = sk;
		}

		public void run() {
			search(str, type, field, total, skip);
		}
	}
	
	/*---------------------------------
	Start download
	---------------------------------*/
	public void startDownload(String id, JButton b) {
		Thread t1 = new downloadThread(id, b);
		t1.start();
	}
	
	/*---------------------------------
	Download thread
	---------------------------------*/
	public class downloadThread extends Thread {
		String id;
		JButton btn;

		public downloadThread(String id, JButton b) {
			this.id = id;
			this.btn = b;
		}

		public void run() {
			if (pinfoManager.getPaperInfoMap().containsKey(id) == false) {
				String req = ArxivParser.BuildURL(id, "pdf");
				window.setState("Downloading... URL: " + req);

				ArxivParser.Download("paper_file", req, false);
				PaperInfo info_temp = ArxivParser.GetPaperInfo(ArxivParser.BuildURL(id, "abs"));
				pinfoManager.AddInfo(info_temp);
				pinfoManager.SaveInfo();
				refreshNode();
				// System.out.println(info_temp.Out());
				window.setState("");
				btn.setEnabled(false);
			}
		}
	}

	/*---------------------------------
	Synchronize
	---------------------------------*/
	public void fileSynchronize(boolean b) {
		List<String> id_list = pinfoManager.FindMissingFile();
		if(id_list.size()==0)
			return;
		
		window.setState("Find " + id_list.size() + " missing pdf, downloading the file ...");
		
		for(String id: id_list) {
			ArxivParser.Download("paper_file", ArxivParser.BuildURL(id, "pdf"), false);
		}
		
		window.setState("");
	}

	public void infoSynchronize(boolean b) {
		List<String> id_list = pinfoManager.FindMissingInfo();
		if(id_list.size()==0)
			return;
		
		window.setState("Find " + id_list.size() + " missing info, searching the info ...");
		
		for(String id: id_list) {
			PaperInfo pinfo = ArxivParser.GetPaperInfo(ArxivParser.BuildURL(id, "abs"));
			if(pinfo != null)
				pinfoManager.AddInfo(pinfo);
		}
		
		pinfoManager.SaveInfo();
		refreshNode();
		window.setState("");
	}

	/*---------------------------------
	Tag Handle
	---------------------------------*/
	public void addTag(String id, String tid) {
		pinfoManager.AddTag(id, tid);
		refreshNode();
		pinfoManager.SaveInfo();
	}

	public void removeTag(String id, List<String> tlist) {
		for (String tid : tlist)
			pinfoManager.RemoveTag(id, tid);
		refreshNode();
		pinfoManager.SaveInfo();
	}

	/*---------------------------------
	Class Sort
	---------------------------------*/
	String classType = "Subjects";
	String sortType = "Recent";
	String orderType = "Decent";

	public void setClassSort(String c, String s, String o) {
		classType = c;
		sortType = s;
		orderType = o;
	}

	/*---------------------------------
	Refresh Node
	---------------------------------*/
	public void refresh(Map<String, PaperInfo> paperInfoMap) {
		window.ClearTreeNode();
		ClassSortList cs = new ClassSortList(paperInfoMap);

		boolean b = (orderType == "Decent") ? true : false;

		Map<String, List<String>> result = cs.Result(classType, sortType, b);
		for (Map.Entry<String, List<String>> idClass : result.entrySet()) {
			String className = idClass.getKey();
			window.addTreeNode("Root", className);
			for (String id : idClass.getValue()) {
				window.addTreeNode(className, "[" + id + "] " + paperInfoMap.get(id).title);
			}
		}
	}
	
	public void refreshNode() {
		Map<String, PaperInfo> paperInfoMap = pinfoManager.getPaperInfoMap();
		refresh(paperInfoMap);
	}
	
	/*---------------------------------
	Local Search
	---------------------------------*/
	public void localSearch(String str) {
		String [] slist = str.split(" ");
		Map<String, PaperInfo> paperInfoMap = pinfoManager.getPaperInfoMap();
		Map<String, PaperInfo> searchInfoMap = new LinkedHashMap<>();
		for (Map.Entry<String, PaperInfo> ptuple : paperInfoMap.entrySet()) {
			boolean find = false;
			for(String s: slist) {
				if(ptuple.getValue().title.toLowerCase().contains(s) == true)
					find = true;
				if(ptuple.getValue().abs.toLowerCase().contains(s) == true)
					find = true;
				if(find == true)
					break;
			}
			if(find == true)
				searchInfoMap.put(ptuple.getKey(), ptuple.getValue());
		}
		refresh(searchInfoMap);
	}
}
