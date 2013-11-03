package poke.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.server.storage.Storage;

import com.google.protobuf.ByteString;

import eye.Comm.Document;
import eye.Comm.NameSpace;

public class FileOps implements Storage {

	private static FileOps obj = null;

	private FileOps() {
	}

	public static FileOps getInstance() {
		if (obj == null) {
			return new FileOps();
		} else {
			return obj;
		}
	}

	protected static Logger logger = LoggerFactory.getLogger("FileOps:Server ");
	// To make this thread safe
	// private static volatile HashMap<Long,String> nameSpaceLookUp
	// = (HashMap<Long, String>) Collections.synchronizedMap(new
	// HashMap<Long,String>());
	private static volatile HashMap<Long, NameSpace> nameSpaceLookUp = new HashMap<Long, NameSpace>();

	@Override
	public void init(Properties cfg) {
		// TODO Auto-generated method stub

	}

	@Override
	public void release() {
		// TODO Auto-generated method stub

	}

	@Override
	public NameSpace getNameSpaceInfo(long spaceId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<NameSpace> findNameSpaces(NameSpace criteria) {

		return null;
	}

	@Override
	public NameSpace createNameSpace(NameSpace space) {
		// Single level of folders.
		// Id need to be passed by client
		if (new File(space.getName()).mkdir()) {
			logger.info("Name Space created : " + space.getName());
			nameSpaceLookUp.put(space.getId(), space);
		} else {
			logger.info("Name Space exisits");
		}
		return nameSpaceLookUp.get(space.getId());

		/*
		 * space.getOwner(); space.getId(); space.getLastModified();
		 * space.getDesc(); space.getCreated(); return null;
		 */
	}

	@Override
	public boolean removeNameSpace(long spaceId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addDocument(String namespace, Document doc) {

		// Check for namespace exists
		try {
			byte[] bs = doc.getChunkContent().toByteArray();
			String docName = doc.getDocName();
			System.out.println(namespace + "<  :::> " + bs.length + " <:::  >"
					+ docName);

			File file = new File(namespace);
			if (!file.exists() || file.isFile()) {
				throw new Exception(
						"Namespace does not exist or a file exists with the same name");
			}

			FileOutputStream fos = new FileOutputStream(namespace + "/"
					+ docName);
			fos.write(bs);
			fos.close();

			PrintWriter out = new PrintWriter(new BufferedWriter(
					new FileWriter(namespace + "/.metadata", true)));
			out.println(doc.getId() + "," + doc.getDocName());
			out.close();

			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	@Override
	public boolean removeDocument(String namespace, long docId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean updateDocument(String namespace, Document doc) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Document> findDocuments(String namespace, Document criteria) {
		// Check for namespace exists
		List<Document> r = new ArrayList<Document>();

		try {
			File file = new File(namespace);
			if (!file.exists() || file.isFile()) {
				return r;
			}

			if (criteria.hasDocName() && criteria.getDocName() != null
					&& criteria.getDocName() != "") {
				// Client is finding by document name
				String filepath = namespace + "/" + criteria.getDocName();
				File f = new File(filepath);
				if (f.exists() && f.isFile()) {
					Document.Builder d = eye.Comm.Document.newBuilder();
					d.setDocName(filepath);
					d.setId(findDocId(namespace, criteria.getDocName()));
					d.setChunkContent(ByteString
							.copyFrom(getFileBytes(filepath)));
					r.add(d.build());
				}
			} else if (criteria.hasId()) {
				// Client is finding by document id
				String docName = findDocName(namespace, criteria.getId());
				String filepath = namespace + "/" + docName;
				File f = new File(filepath);
				if (f.exists() && f.isFile()) {
					Document.Builder d = eye.Comm.Document.newBuilder();
					d.setDocName(filepath);
					d.setId(criteria.getId());
					d.setChunkContent(ByteString
							.copyFrom(getFileBytes(filepath)));
					r.add(d.build());
				}
			} else {
				// Client did not supply document name or id, so looking up all
				// documents in namespace
				Path path = Paths.get(namespace);
				DirectoryStream<Path> stream = Files.newDirectoryStream(path);
				for (Path p : stream) {
					String filename = p.getFileName().toString();
					String filepath = namespace + "/" + filename;
					File f = new File(filepath);
					if (f.isFile()) {
						Document.Builder d = eye.Comm.Document.newBuilder();
						d.setDocName(filepath);
						d.setId(findDocId(namespace, criteria.getDocName()));
						d.setChunkContent(ByteString
								.copyFrom(getFileBytes(filepath)));
						r.add(d.build());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return r;
	}

	private final static byte[] getFileBytes(String filepath)
			throws IOException {
		Path path = Paths.get(filepath);
		return Files.readAllBytes(path);
	}

	// Method reads .metadata file in namespace folder and returns the document
	// id for the given docName.
	private final static long findDocId(String namespace, String docName)
			throws IOException {
		Path path = Paths.get(namespace + "/.metadata");
		List<String> lines = Files.readAllLines(path, Charset.defaultCharset());
		for (String line : lines) {
			// line will look like docId,docName
			int index = line.indexOf("," + docName);
			if (index > -1) {
				String docId = line.substring(0, index);
				return Long.parseLong(docId);
			}
		}

		throw new IOException(docName + " is not found");
	}

	// Method reads .metadata file in namespace folder and returns the document
	// name for the given docId.
	private final static String findDocName(String namespace, long docId)
			throws IOException {
		Path path = Paths.get(namespace + "/.metadata");
		List<String> lines = Files.readAllLines(path, Charset.defaultCharset());
		String pat = docId + ",";
		for (String line : lines) {
			// line will look like docId,docName
			if (line.startsWith(pat)) {
				return line.substring(pat.length());
			}
		}

		throw new IOException("Id " + docId + " is not found");
	}

	// Test main
	public static void main(String[] args) throws Exception {
		String s = findDocName("TestFolder", 10);
		System.out.println(s);
	}
}
