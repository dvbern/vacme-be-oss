create table STAT_StatistikKennzahlEintrag
(
	id varchar(36) not null
		primary key,
	timestampErstellt datetime(6) not null,
	timestampMutiert datetime(6) not null,
	userErstellt varchar(255) not null,
	userMutiert varchar(255) not null,
	version bigint not null,
	erfassungszeitpunkt datetime(6) not null,
	statistikKennzahl varchar(50) not null,
	wert bigint not null
);

create table STAT_StatistikKennzahlEintrag_AUD
(
	id varchar(36) not null,
	REV int not null,
	REVTYPE tinyint null,
	timestampErstellt datetime(6) null,
	timestampMutiert datetime(6) null,
	userErstellt varchar(255) null,
	userMutiert varchar(255) null,
	erfassungszeitpunkt datetime(6) null,
	statistikKennzahl varchar(50) null,
	wert bigint null,
	primary key (id, REV),
	constraint FK_STAT_Kennzahl_AUD_REVINFO
		foreign key (REV) references REVINFO (REV)
);

