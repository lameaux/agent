package com.euromoby.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.euromoby.twitter.TwitterMessage;

@Component
public class TwitterMessageDao {

	private DataSource dataSource;

	private static final TwitterMessageRowMapper ROW_MAPPER = new TwitterMessageRowMapper();

	@Autowired
	public TwitterMessageDao(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public List<TwitterMessage> findAll(int limit) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		return jdbcTemplate.query("select * from twitter_message order by id limit ?", ROW_MAPPER, limit);
	}

	public void saveAll(final List<TwitterMessage> twitterMessages) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		jdbcTemplate.batchUpdate("insert into twitter_message(account_id, message_text) values (?,?)", 
			new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					ps.setString(1, twitterMessages.get(i).getAccountId());
					ps.setString(2, twitterMessages.get(i).getMessageText());
				}
	
				@Override
				public int getBatchSize() {
					return twitterMessages.size();
				}
			});
	}

	public void deleteAll(final List<TwitterMessage> twitterMessages) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		jdbcTemplate.batchUpdate("delete from twitter_message where id = ?", 
			new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					ps.setInt(1, twitterMessages.get(i).getId());
				}
	
				@Override
				public int getBatchSize() {
					return twitterMessages.size();
				}
			});
	}	
	
	static class TwitterMessageRowMapper implements RowMapper<TwitterMessage> {
		@Override
		public TwitterMessage mapRow(ResultSet rs, int rowNum) throws SQLException {
			TwitterMessage twitterMessage = new TwitterMessage();
			twitterMessage.setId(rs.getInt("id"));
			twitterMessage.setAccountId(rs.getString("account_id"));
			twitterMessage.setMessageText(rs.getString("message_text"));
			return twitterMessage;
		}
	}
}
