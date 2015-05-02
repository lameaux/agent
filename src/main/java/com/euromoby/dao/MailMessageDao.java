package com.euromoby.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.euromoby.mail.MailMessage;

@Component
public class MailMessageDao {

	private DataSource dataSource;

	private static final MailMessageRowMapper ROW_MAPPER = new MailMessageRowMapper();

	@Autowired
	public MailMessageDao(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public MailMessage findById(Integer id) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		try {
			return jdbcTemplate.queryForObject("select * from mail_message where id = ?", ROW_MAPPER, id);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public MailMessage findByAccountIdAndId(Integer accountId, Integer id) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		try {
			return jdbcTemplate.queryForObject("select * from mail_message where account_id = ? and id = ?", ROW_MAPPER, accountId, id);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}	
	
	
	
	public List<MailMessage> findByAccountId(Integer accountId) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		return jdbcTemplate.query("select * from mail_message where account_id = ? order by id desc", ROW_MAPPER, accountId);
	}

	public void save(MailMessage mailMessage) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update("insert into mail_message(account_id, sender, size, created) values (?,?,?,?)", 
				mailMessage.getAccountId(), mailMessage.getSender(),
				mailMessage.getSize(), mailMessage.getCreated());
		mailMessage.setId(jdbcTemplate.queryForObject("select scope_identity()", Integer.class));
	}

	static class MailMessageRowMapper implements RowMapper<MailMessage> {
		@Override
		public MailMessage mapRow(ResultSet rs, int rowNum) throws SQLException {
			MailMessage mailMessage = new MailMessage();
			mailMessage.setId(rs.getInt("id"));
			mailMessage.setAccountId(rs.getInt("account_id"));
			mailMessage.setSender(rs.getString("sender"));
			mailMessage.setSize(rs.getInt("size"));
			mailMessage.setCreated(new Date(rs.getTimestamp("created").getTime()));
			return mailMessage;
		}
	}
}
