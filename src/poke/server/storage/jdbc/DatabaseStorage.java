/*
 * copyright 2012, gash
 * 
 * Gash licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package poke.server.storage.jdbc;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.server.storage.Storage;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
//import com.mysql.jdbc.Statement;



import eye.Comm.Document;
import eye.Comm.NameSpace;

public class DatabaseStorage implements Storage {
	protected static Logger logger = LoggerFactory.getLogger("database");

/*	public static final String sDriver = "com.mysql.jdbc.Driver";
	public static final String sUrl = "jdbc:mysql://localhost:3306/cmpe275";
	public static final String sUser = "root";
	public static final String sPass = "root";
*/
	protected Properties cfg;
	protected BoneCP cpool;

	protected DatabaseStorage() {
	}

	public DatabaseStorage(Properties cfg) {
		init(cfg);
	}

	@Override
	public void init(Properties cfg) {
		if (cpool != null)
			return;

		this.cfg = cfg;

		try {
	
			Class.forName(cfg.getProperty("sDriver"));
			BoneCPConfig config = new BoneCPConfig();
			config.setJdbcUrl(cfg.getProperty("sUrl"));
			config.setUsername(cfg.getProperty("sUser", "sa"));
			config.setPassword(cfg.getProperty("sPass", ""));
			config.setMinConnectionsPerPartition(5);
			config.setMaxConnectionsPerPartition(10);
			config.setPartitionCount(1);

			cpool = new BoneCP(config);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gash.jdbc.repo.Repository#release()
	 */
	@Override
	public void release() {
		if (cpool == null)
			return;

		cpool.shutdown();
		cpool = null;
	}

	@Override
	public NameSpace getNameSpaceInfo(long spaceId) {
		NameSpace space = null;

		Connection conn = null;
		try {
			conn = cpool.getConnection();
			conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
			// TODO complete code to retrieve through JDBC/SQL
			if (conn != null){
				System.out.println("Connection successful!");
				Statement stmt =  conn.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT * FROM cmpe275.tenants"); // do something with the connection.
				while(rs.next()){
					System.out.println(rs.getString(1)); // should print out "1"'
				}
			}
//			connectionPool.shutdown(); // shutdown connection pool.
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		return space;
	}
	
	

	@Override
	public List<NameSpace> findNameSpaces(NameSpace criteria) {
		List<NameSpace> list = null;

		Connection conn = null;
		try {
			conn = cpool.getConnection();
			conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
			// TODO complete code to search through JDBC/SQL
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("failed/exception on find", ex);
			try {
				conn.rollback();
			} catch (SQLException e) {
			}
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return list;
	}

	@Override
	public NameSpace createNameSpace(NameSpace space) {
		if (space == null)
			return space;

		Connection conn = null;
		try {
			conn = cpool.getConnection();
			conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
			// TODO complete code to use JDBC
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("failed/exception on creating space " + space, ex);
			try {
				conn.rollback();
			} catch (SQLException e) {
			}

			// indicate failure
			return null;
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return space;
	}

	@Override
	public boolean removeNameSpace(long spaceId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addDocument(String namespace, Document doc) {
		// TODO Auto-generated method stub
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


	public static void main(String[] args) {
		
		
		Properties cfg = new Properties();
		try {
			cfg.load(new FileInputStream("/home/mahajan/cmpe275/Project01/core-netty/config/databases.cfg"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DatabaseStorage db=new DatabaseStorage(cfg);
		db.getNameSpaceInfo(100) ;
		
/*		
		
		BoneCP connectionPool = null;
		Connection conn = null;
 
		try {
			// load the database driver (make sure this is in your classpath!)
			Class.forName(sDriver);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		try {
			// setup the connection pool
			BoneCPConfig config = new BoneCPConfig();
			config.setJdbcUrl(sUrl); // jdbc url specific to your database, eg jdbc:mysql://127.0.0.1/yourdb
			config.setUsername(sUser); 
			config.setPassword(sPass);
			config.setMinConnectionsPerPartition(5);
			config.setMaxConnectionsPerPartition(10);
			config.setPartitionCount(1);
			connectionPool = new BoneCP(config); // setup the connection pool
			
			conn = connectionPool.getConnection(); // fetch a connection
			
			if (conn != null){
				System.out.println("Connection successful!");
				Statement stmt =  conn.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT * FROM cmpe275.tenants"); // do something with the connection.
				while(rs.next()){
					System.out.println(rs.getString(1)); // should print out "1"'
				}
			}
			connectionPool.shutdown(); // shutdown connection pool.
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		*/
		
	}


}

