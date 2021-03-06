package com.laststar.fileRepository.dao;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.laststar.fileRepository.Paging;
import com.laststar.fileRepository.entity.FILES;
import com.laststar.fileRepository.entity.USERS;

public class FileDao extends EntityDao {

	public int getTotalFileCount() throws SQLException {
		PreparedStatement pstmt = connection.prepareStatement("SELECT COUNT(*) FROM FILES");
		ResultSet rs = pstmt.executeQuery();
		rs.next();
		return rs.getInt("COUNT(*)");
	}
	
	public int getPrefixMatchFileCount(String prefix) throws SQLException {
		PreparedStatement pstmt = connection.prepareStatement("SELECT COUNT(*) FROM FILES WHERE NAME LIKE ? || '%'");
		pstmt.setString(1, prefix);
		ResultSet rs = pstmt.executeQuery();
		rs.next();
		return rs.getInt("COUNT(*)");
	}

	public FILES[] getFileList(Paging paging) throws SQLException {
		FILES[] files = new FILES[paging.getEndRow() - paging.getStartRow() + 1];
		PreparedStatement pstmt = connection
				.prepareStatement(
						"SELECT B.RNUM, B.NAME, B.VERSION, B.SINCE, B.NICKNAME " + 
						"FROM (SELECT ROWNUM RNUM, A.NAME NAME, A.VERSION VERSION, A.SINCE SINCE, A.NICKNAME NICKNAME " + 
						"	FROM (SELECT F.NAME NAME, F.VERSION VERSION, F.SINCE SINCE, U.NICKNAME NICKNAME " + 
						"		FROM FILES F, USERS U " + 
						"		WHERE F.USER_ID = U.ID " + 
						"	         ) A " + 
						"	WHERE ROWNUM <= ? " + 
						"         ) B " + 
						"WHERE B.RNUM >=?");
		
		pstmt.setInt(1, paging.getEndRow());
		pstmt.setInt(2, paging.getStartRow());

		ResultSet rs = pstmt.executeQuery();
		for (int i = 0; rs.next(); i++) {
			String name = rs.getString("NAME");
			String version = rs.getString("VERSION");
			String since = rs.getString("SINCE");
			String user_name = rs.getString("NICKNAME");

			USERS author = new USERS(null, null, user_name, null, null);
			files[i] = new FILES(name, version, author, null, since, null, 0);
		}

		return files;
	}

	public int getFileCount(String name, String version) throws SQLException{
		PreparedStatement pstmt = connection.prepareStatement(
				"SELECT COUNT(*) FROM FILES WHERE NAME = ? AND VERSION = ?");
		pstmt.setString(1, name);
		pstmt.setString(2, version);
		
		ResultSet rs = pstmt.executeQuery();
		rs.next();
		
		return rs.getInt("COUNT(*)");
	}
	
	public FILES getFile(String name, String version) throws SQLException {
		PreparedStatement pstmt = connection.prepareStatement(
				"SELECT F.NAME, F.VERSION, F.CONTENT, F.SINCE,  F.REPOSITORY_PATH, F.DOWNLOAD_COUNT, U.ID, U.NICKNAME, U.EMAIL"
						+ " FROM FILES F, USERS U" + " WHERE F.USER_ID = U.ID" + " AND F.NAME = ?"
						+ " AND F.VERSION = ?");
		pstmt.setString(1, name);
		pstmt.setString(2, version);

		ResultSet rs = pstmt.executeQuery();
		if (rs.next()) {
			USERS author = new USERS(rs.getString("ID"), null, rs.getString("NICKNAME"), rs.getString("EMAIL"),
					null);
			return new FILES(rs.getString("NAME"), rs.getString("VERSION"), author, rs.getString("CONTENT"),
					rs.getString("SINCE"), rs.getString("REPOSITORY_PATH"), rs.getInt("DOWNLOAD_COUNT"));
		} else {
			return null;
		}
		
	}
	
	public void deleteFile(String name, String version) throws SQLException {
		PreparedStatement pstmt = connection.prepareStatement("DELETE FROM FILES WHERE NAME = ? AND VERSION = ? ");
		pstmt.setString(1, name);
		pstmt.setString(2, version);
		
		pstmt.executeUpdate();
	}
		
	public void insertFile(FILES file) throws SQLException {
		PreparedStatement pstmt = connection.prepareStatement("INSERT INTO FILES VALUES(?,?,?,?, ?, ?,?)");
		pstmt.setString(1, file.getNAME());
		pstmt.setString(2, file.getVERSION());
		pstmt.setString(3, file.getAUTHOR().getID());
		pstmt.setString(4, file.getCONTENT());
		pstmt.setString(5, file.getSINCE());
		pstmt.setString(6, file.getREPOSITORY_PATH());
		pstmt.setInt(7, 0);
		
		pstmt.executeUpdate();
	}

	public void modifyFile(String name, String version, String modifyName, String  modifyVersion, String  modifyContent, String modifyRepositoryPath) throws SQLException {
		PreparedStatement pstmt = connection.prepareStatement("UPDATE FILES SET NAME = ?, VERSION = ?, CONTENT = ?, REPOSITORY_PATH = ?  WHERE NAME = ? AND VERSION = ?");
		pstmt.setString(1, modifyName);
		pstmt.setString(2, modifyVersion);
		pstmt.setString(3, modifyContent);
		pstmt.setString(4, modifyRepositoryPath);
		pstmt.setString(5, name);
		pstmt.setString(6, version);
		
		pstmt.executeUpdate();
	}



}
