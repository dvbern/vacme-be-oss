package ch.dvbern.oss.vacme.smartdb;

import com.querydsl.core.types.Ops;
import com.querydsl.jpa.JPQLTemplates;

public class MySQLJPATemplates extends JPQLTemplates {

	public static final MySQLJPATemplates DEFAULT = new MySQLJPATemplates();

	public MySQLJPATemplates() {
		this(DEFAULT_ESCAPE);
		add(Ops.MathOps.RANDOM, "rand()");
		add(Ops.MathOps.RANDOM2, "rand({0})");
	}

	public MySQLJPATemplates(char escape) {
		super(escape);
	}
}
