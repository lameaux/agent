package com.euromoby.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.euromoby.twitter.TwitterAccount;

@Component
public class TwitterAccountDao {

	private DataSource dataSource;

	private static final TwitterAccountRowMapper ROW_MAPPER = new TwitterAccountRowMapper();

	@Autowired
	public TwitterAccountDao(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public TwitterAccount findById(String id) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		try {
			return jdbcTemplate.queryForObject("select * from twitter_account where id = ?", ROW_MAPPER, id);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public List<TwitterAccount> findAll() {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		return jdbcTemplate.query("select * from twitter_account order by screen_name", ROW_MAPPER);
	}

	public List<TwitterAccount> findByTag(String tag) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		return jdbcTemplate.query("select * from twitter_account where tags LIKE ? order by screen_name", ROW_MAPPER, "%"+tag+"%");
	}	
	
	public void save(TwitterAccount twitterAccount) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update("insert into twitter_account(id, screen_name, tags, access_token, access_token_secret) values (?,?,?,?,?)", 
				twitterAccount.getId(), twitterAccount.getScreenName(), twitterAccount.getTags(),
				twitterAccount.getAccessToken(), twitterAccount.getAccessTokenSecret());
	}

	public void update(TwitterAccount twitterAccount) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update("update twitter_account set screen_name = ?, tags = ?, access_token = ?, access_token_secret = ? where id = ?", 
				twitterAccount.getScreenName(), twitterAccount.getTags(),
				twitterAccount.getAccessToken(), twitterAccount.getAccessTokenSecret(), twitterAccount.getId());
	}

	static class TwitterAccountRowMapper implements RowMapper<TwitterAccount> {
		@Override
		public TwitterAccount mapRow(ResultSet rs, int rowNum) throws SQLException {
			TwitterAccount twitterAccount = new TwitterAccount();
			twitterAccount.setId(rs.getString("id"));
			twitterAccount.setScreenName(rs.getString("screen_name"));
			twitterAccount.setTags(rs.getString("tags"));
			twitterAccount.setAccessToken(rs.getString("access_token"));
			twitterAccount.setAccessTokenSecret(rs.getString("access_token_secret"));			
			return twitterAccount;
		}
	}
}
