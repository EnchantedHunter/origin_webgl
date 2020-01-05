package com.origin.entity;

import com.origin.jpa.TableExtended;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

/**
 * игровой персонаж игрока
 */
@Entity
@Table(name = "characters")
@TableExtended(truncate = true, drop = true)
public class Character
{
	@Id
	@Column(name = "id", columnDefinition = "INT(11) NOT NULL AUTO_INCREMENT")
	private int _id;

	@Column(name = "userId", columnDefinition = "INT(11) NOT NULL", nullable = false)
	private int _userId;

	@Column(name = "name", columnDefinition = "VARCHAR(16) NOT NULL", nullable = false)
	private String _name;

	@Column(name = "createTime", columnDefinition = "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP")
	private Timestamp _createTime;

}
