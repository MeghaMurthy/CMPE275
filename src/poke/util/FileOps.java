package poke.util;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eye.Comm.Document;
import eye.Comm.NameSpace;
import poke.server.storage.Storage;

public class FileOps implements Storage {

	private static  FileOps obj=null;
	private FileOps(){}
	
	public static FileOps getInstance(){
		if (obj == null){
			return new FileOps();
		}
		else {
			return obj;
		}
	}
	
	protected static Logger logger = LoggerFactory.getLogger("FileOps:Server ");
//  To make this thread safe
//	private static volatile HashMap<Long,String> nameSpaceLookUp 
//								= (HashMap<Long, String>) Collections.synchronizedMap(new HashMap<Long,String>()); 
	private static volatile HashMap<Long,NameSpace> nameSpaceLookUp = new HashMap<Long,NameSpace>();

	
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
		//Single level of folders.
		//Id need to be passed by client 
		if (new File(space.getName()).mkdir()){
			logger.info("Name Space created : "+space.getName());
			nameSpaceLookUp.put(space.getId(),space);
		}
		else {
			logger.info("Name Space exisits");
		}
		return nameSpaceLookUp.get(space.getId());
		
		/*
		space.getOwner();
		space.getId();
		space.getLastModified();
		space.getDesc();
		space.getCreated();
		return null;
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
			System.out.println(namespace+"<  :::> "+  bs.length      +" <:::  >"+docName);
			File file = new File(namespace);
			file.mkdirs();
			FileOutputStream fos = new FileOutputStream(namespace+"/"+docName);
			fos.write(bs);
			fos.close();
		}
		catch(Exception e){
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
		// TODO Auto-generated method stub
		return null;
	}

}
