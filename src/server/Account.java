package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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

	public boolean checkUsername(String userName){
		String queryString = "SELECT * FROM user WHERE userName =\'"+userName+"\'";
		Statement stm;
		try {
			stm = connect.createStatement();
			this.result = stm.executeQuery(queryString);

			while (result.next()) {
				if(result.getInt(1)>0) {
					return false;
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
		return true;
	}

	public boolean userSignup(String userName, String userPass) {
		if(checkUsername(userName)){
			String queryString = "INSERT INTO user(userName,userPass,role) VALUES(?,?,?)";
			PreparedStatement pStatement;
			try {
				pStatement = connect.prepareStatement(queryString);
				pStatement.setString(1, userName);
				pStatement.setString(2, userPass);
				pStatement.setInt(3, 1);
				int count = pStatement.executeUpdate();
				if(count > 0) {
					return true;
				} else {
					return false;
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
		} else {
			return false;
		}
		return false;
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
