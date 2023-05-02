/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
 */

package ch.dvbern.oss.vacme.entities.documentqueue.entities;

import javax.persistence.Entity;

import ch.dvbern.oss.vacme.entities.documentqueue.DocumentQueueType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("JpaMissingIdInspection")
@Entity
@Getter
@Setter
@Slf4j
public class ReportingOdiBuchungenDocQueue extends SpracheabhDocQueue {

	private static final long serialVersionUID = 3818379002932809199L;

	public ReportingOdiBuchungenDocQueue() {
		super();
		this.setTyp(DocumentQueueType.ODI_TERMINBUCHUNGEN);
	}
}
