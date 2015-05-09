package com.euromoby.download.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.euromoby.download.DownloadFile;

@Component
public class DownloadFileDao {

	private DataSource dataSource;

	private static final DownloadFileRowMapper ROW_MAPPER = new DownloadFileRowMapper();

	@Autowired
	public DownloadFileDao(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public DownloadFile findByUrl(String url) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		try {
			return jdbcTemplate.queryForObject("select * from download_file where url = ?", ROW_MAPPER, url);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}		
	}	

	public DownloadFile findByFileLocation(String fileLocation) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		try {
			return jdbcTemplate.queryForObject("select * from download_file where file_location = ?", ROW_MAPPER, fileLocation);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}		
	}	
	
	public List<DownloadFile> findAll(int limit) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		return jdbcTemplate.query("select * from download_file order by id limit ?", ROW_MAPPER, limit);
	}

	public void save(DownloadFile downloadFile) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		jdbcTemplate.update("insert into download_file (url, file_location, no_proxy) values (?,?,?)", 
			downloadFile.getUrl(), downloadFile.getFileLocation(), downloadFile.isNoProxy() ? 1 : 0);
		downloadFile.setId(jdbcTemplate.queryForObject("select scope_identity()", Integer.class));
	}

	public void deleteAll(final List<DownloadFile> downloadFiles) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		jdbcTemplate.batchUpdate("delete from download_file where id = ?", 
			new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					ps.setInt(1, downloadFiles.get(i).getId());
				}
				@Override
				public int getBatchSize() {
					return downloadFiles.size();
				}
			});
	}	
	
	static class DownloadFileRowMapper implements RowMapper<DownloadFile> {
		@Override
		public DownloadFile mapRow(ResultSet rs, int rowNum) throws SQLException {
			DownloadFile downloadFile = new DownloadFile();
			downloadFile.setId(rs.getInt("id"));
			downloadFile.setUrl(rs.getString("url"));
			downloadFile.setFileLocation(rs.getString("file_location"));
			downloadFile.setNoProxy(rs.getInt("no_proxy") == 1);
			return downloadFile;
		}
	}
}
