package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Account {
	private Connection connect;
	private ResultSet result;
	
	public Account(String userSQL, String passSQL) {
		String url = "jdbc:mysql://localhost:3306/distributed_sys?autoReconnect=true&useSSL=false";
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
			this.connect = DriverManager.getConnection(url, userSQL, passSQL);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public boolean userLogin(String userName, String userPass){
		String queryString = "SELECT * FROM user WHERE userName =\'"+userName+"\'";
		Statement stm;
		try {
			stm = connect.createStatement();
			this.result = stm.executeQuery(queryString);

			while (result.next()) {
				if(result.getInt(1)>0) {
					if(result.getString("userPass").equals(userPass)) {
						return true;
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				connect.close();
				result.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
}
