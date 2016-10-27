package edu.usc.iiw.crawler;

import edu.usc.iiw.storage.DBEnvWrapper;

/**
 * Daemon thread which creates multiple instances of the crawler
 * 
 * @author 
 *
 */
public class XPathCrawler {

	private String url;
	private String dbDirectory;
	private long maxDownloadSize;
	private long maxFilesToCrawl = Long.MAX_VALUE;
	DBEnvWrapper dbEnvWrapper;
	BlockingDiskQueue bdQueue;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getdbDirectory() {
		return dbDirectory;
	}

	public void setdbDirectory(String directory) {
		this.dbDirectory = directory;
	}

	public long getMaxDownloadSize() {
		return maxDownloadSize;
	}

	public void setMaxDownloadSize(long maxDownloadSize) {
		this.maxDownloadSize = maxDownloadSize;
	}

	public long getMaxFilesToCrawl() {
		return maxFilesToCrawl;
	}

	public void setMaxFilesToCrawl(long maxFilesToCrawl) {
		this.maxFilesToCrawl = maxFilesToCrawl;
	}

	void crawl() {
		// while()
	}

	public static void main(String[] args) throws Exception {

		XPathCrawler xpc = new XPathCrawler();

		if (args.length == 4) {
			xpc.setUrl(args[0]);
			xpc.setdbDirectory(args[1]);
			xpc.setMaxDownloadSize(Long.parseLong(args[2])
					* (long) Math.pow(2, 20));
			xpc.setMaxFilesToCrawl(Long.parseLong(args[3]));
		} else if (args.length == 3) {
			xpc.setUrl(args[0]);
			xpc.setdbDirectory(args[1]);
			xpc.setMaxDownloadSize(Long.parseLong(args[2]));
		} else {
			System.out
					.println("Usage: java XPathCrawler <url> <directory> <max-download-size> [<max-no-of-files-to-crawl>]");
			return;
		}

		xpc.dbEnvWrapper = new DBEnvWrapper(xpc.getdbDirectory());	
		Crawler crawlerThread = new Crawler(
				xpc.url,
				xpc.dbEnvWrapper, xpc.getMaxDownloadSize(),
				xpc.getMaxFilesToCrawl());
		crawlerThread.run();
	}

}
