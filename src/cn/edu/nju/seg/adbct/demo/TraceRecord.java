package cn.edu.nju.seg.adbct.demo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

/**
 * @author yulei
 * @description 用于UML活动图混合执行轨迹记录。对每一条完整的执行轨迹中的节点着色，一条路径保存为一个umldi文件，在trace目录下。
 * @createTime 2013-2-26
 */
public class TraceRecord {
	public static int RED = 1;
	public static int green = 2;
	private UmldiColorVisitor visitor;
	private Document dom;
	/**
	 * 用于记录结果数目，同时也为文件命名做序号。
	 */
	private int fileCounter = 1;
	private Element root;
	private SAXReader saxReader = new SAXReader();
	private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH-mm");
	private String traceDir;
	private String refModelName;

	public TraceRecord(String filename) {
		this.refModelName = filename;
		this.dom = load(filename);
	}

	/**
	 * 探索新的执行路径前，需要重新装载原模型文件，然后对其执行路径进行着色标记。
	 * 
	 * @param filename
	 * @return
	 */
	public Document reLoad(String filename) {
		return this.load(filename);
	}

	/**
	 * 加载模型的设计文件，umldi格式。
	 * @param filename
	 * @return
	 */
	private Document load(String filename) {
		try {
			dom = saxReader.read(new File(filename + "di")); 
			root = dom.getRootElement();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return dom;
	}

	/**
	 * 输出结果文件。
	 * 
	 * @return
	 */
	public boolean saveTraceFile() {
		boolean flag = true;
		try {
			XMLWriter writer = new XMLWriter(new OutputStreamWriter(new FileOutputStream(traceDir + "trace" + fileCounter + ".umldi"), "UTF-8"));
			writer.write(dom);
			writer.close();
			fileCounter++;
		} catch (Exception ex) {
			flag = false;
			ex.printStackTrace();
		}
		System.out.println(flag);
		return flag;
	}

	/**
	 * 对给定id的节点或边进行着色标记。
	 * 
	 * @param id
	 */
	public void makeColor(String id) {
		visitor = new UmldiColorVisitor(this.refModelName + "#" + id);
		root.accept(visitor);
	}

	/**
	 * 在结果输出目录备份原uml文件，这样在查看umldi设计文件时才能正常打开。
	 * 
	 * @return
	 */
	public boolean umlBackUp() {
		boolean flag = true;
		try {
			Document uml = saxReader.read(new File(this.refModelName));
			String dateStr = df.format(new Date());
			File backupDir = new File("trace/" + dateStr);
			if (backupDir.mkdir()) {
				traceDir = backupDir.getPath() + "/";
				XMLWriter writer = new XMLWriter(new OutputStreamWriter(new FileOutputStream(traceDir + this.refModelName), "UTF-8"));
				writer.write(uml);
				writer.close();
			} else {
				flag = false;
			}
		} catch (Exception ex) {
			flag = false;
			ex.printStackTrace();
		}
		System.out.println(flag);
		return flag;
	}

	/**
	 * 获取umldi文件引用的uml文件名。 未使用。
	 * 
	 * @return
	 */
	public String getRefModel() {
		for (Iterator i = root.elementIterator(); i.hasNext();) {
			Element el = (Element) i.next();
			if (el.getName().equals("model")) {
				String umlHref = el.attribute("href").getValue();
				String[] res = umlHref.split("#");
				return res[0];
			}
		}
		System.out.println("Error: No ref model found!");
		return null;

	}
}
