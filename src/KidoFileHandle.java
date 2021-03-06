import java.io.*;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class KidoFileHandle {

	public static Map<String, PaperInfo> LoadKido(String filename) {
		Map<String, PaperInfo> paperInfoMap = new LinkedHashMap<>();
		String fileContents = "";
		int i;
		FileReader in;
		try {
			in = new FileReader(filename);
			while ((i = in.read()) != -1) {
				char ch = (char) i;
				fileContents = fileContents + ch;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// System.out.println(fileContents);
		String[] content = fileContents.split("\\}\\{");
		for (String str : content) {
			PaperInfo ptemp = new PaperInfo();
			// Read id
			ptemp.id = str.split("ID: ")[1].split("\\\n")[0];

			// Read title
			ptemp.title = str.split("Title: ")[1].split("\\\n")[0];

			// Read Authors
			String a_temp = str.split("Authors: ")[1].split("\\\n")[0];
			ptemp.authors = Arrays.asList(a_temp.split(", "));

			// Read Date
			int[] d = new int[3];
			String[] d_temp = str.split("Date: ")[1].split("\\\n")[0].split("/");
			d[0] = Integer.valueOf(d_temp[0]);
			d[1] = Integer.valueOf(d_temp[1]);
			d[2] = Integer.valueOf(d_temp[2]);
			ptemp.date = d;

			// Read Abstract
			ptemp.abs = str.split("Abstract: ")[1].split("\\\n")[0];

			// Read Subjects
			String s_temp = str.split("Subjects: ")[1].split("\\\n")[0];
			ptemp.subjects = Arrays.asList(s_temp.split(", "));

			// Read tags
			String t_temp = str.split("Tags: ")[1].split("}")[0];

			if (t_temp.length() > 1) {
				t_temp = t_temp.split("\\\n")[0];
				//System.out.println("\"" + t_temp + "\"");
				for (String tag : t_temp.split(", "))
					ptemp.tags.add(tag);
			}

			paperInfoMap.put(ptemp.id, ptemp);
		}

		return paperInfoMap;
	}

	public static void SaveKido(String filename, Map<String, PaperInfo> paperInfoMap) {
		FileWriter out = null;
		try {
			// New file
			out = new FileWriter(filename, false);

			// Write info
			for (Map.Entry<String, PaperInfo> pinfo : paperInfoMap.entrySet()) {
				out.write("{\n");
				out.write(pinfo.getValue().Out());
				out.write("\n}");
			}

			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
