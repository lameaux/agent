package com.euromoby.mail.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.euromoby.mail.model.MailAccount;

@Component
public class MailAccountDao {

	private DataSource dataSource;

	private static final MailAccountRowMapper ROW_MAPPER = new MailAccountRowMapper();

	@Autowired
	public MailAccountDao(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public MailAccount findByLoginAndDomain(String login, String domain) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		try {
			return jdbcTemplate.queryForObject("select * from mail_account where login = ? and domain = ?", ROW_MAPPER, login, domain);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public List<MailAccount> findAll() {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		return jdbcTemplate.query("select * from mail_account order by domain, login", ROW_MAPPER);
	}

	public void save(MailAccount mailAccount) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update("insert into mail_account(login, domain, active) values (?,?,?)", mailAccount.getLogin(), mailAccount.getDomain(),
				mailAccount.getActive() ? 1 : 0);
		mailAccount.setId(jdbcTemplate.queryForObject("select scope_identity()", Integer.class));

	}

	public void update(MailAccount mailAccount) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update("update mail_account set active = ? where id = ?", mailAccount.getActive() ? 1 : 0, mailAccount.getId());
	}

	static class MailAccountRowMapper implements RowMapper<MailAccount> {
		@Override
		public MailAccount mapRow(ResultSet rs, int rowNum) throws SQLException {
			MailAccount mailAccount = new MailAccount();
			mailAccount.setId(rs.getInt("id"));
			mailAccount.setLogin(rs.getString("login"));
			mailAccount.setDomain(rs.getString("domain"));
			mailAccount.setActive(rs.getInt("active") == 1);
			return mailAccount;
		}
	}
}
