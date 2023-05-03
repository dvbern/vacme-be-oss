/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.vacme.service;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;

import ch.dvbern.oss.vacme.entities.documentqueue.IDocumentQueueJobFinishedBenachrichtigungService;
import ch.dvbern.oss.vacme.entities.documentqueue.VonBisSpracheParamJax;
import ch.dvbern.oss.vacme.entities.documentqueue.entities.DocumentQueue;
import ch.dvbern.oss.vacme.entities.registration.Sprache;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfung;
import ch.dvbern.oss.vacme.entities.terminbuchung.OrtDerImpfungTyp;
import ch.dvbern.oss.vacme.entities.types.daterange.DateUtil;
import ch.dvbern.oss.vacme.i18n.ServerMessageUtil;
import ch.dvbern.oss.vacme.i18n.TL;
import ch.dvbern.oss.vacme.jax.registration.OdiUserJax;
import ch.dvbern.oss.vacme.jax.registration.OrtDerImpfungJax;
import ch.dvbern.oss.vacme.shared.errors.AppFailureException;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import static ch.dvbern.oss.vacme.entities.types.daterange.DateUtil.DEFAULT_DATE_FORMAT;
import static java.util.Locale.GERMAN;

@ApplicationScoped
@Transactional
@Slf4j
public class MailService implements IDocumentQueueJobFinishedBenachrichtigungService {

	@Inject
	Mailer mailer;

	@Inject
	protected TL translate;

	@ConfigProperty(name = "vacme.stufe", defaultValue = "LOCAL")
	String stufe;

	@ConfigProperty(name = "vacme.mandant", defaultValue = "BE")
	String mandant;

	@ConfigProperty(name = "vacme.fachapplikation.url", defaultValue = "https://impfen-be.vacme.ch")
	String urlFachapplikation;

	@ConfigProperty(name = "vacme.fachapplikation.blog.url", defaultValue = "https://blog-impfen.vacme.ch/be/")
	String urlBlogFachapplikation;

	@ConfigProperty(name = "vacme.cc.zuwenige.zweittermine.mail")
	String ccMailZuwenigeZweittermine;

	@SuppressWarnings("CdiInjectInspection")
	@ConfigProperty(name = "vacme.cron.doccreation.cleanup.maxage.seconds")
	long maxDocumentqueueResultAgeSeconds = 60 * 60 * 4; // 4 hours

	@NonNull
	private String appendStufeAndKantonToSubject(@NonNull String subject) {
		final String prefix = String.format("VACME-%s (%s): ", mandant, stufe);
		return prefix + subject;
	}

	@NonNull
	private String appendInfoTestsystemToMessage(@NonNull String message) {
		if (Objects.equals(stufe, "PROD")) {
			return message;
		}
		return message + translate.translate("mail_info_testsystem_postfix");
	}

	public void sendTextMail(
		@Nonnull String to,
		@Nonnull String subject,
		@Nonnull String text,
		boolean abortInCaseOfException
	) throws AppFailureException {
		try {
			mailer.send(Mail.withText(to, appendStufeAndKantonToSubject(subject), appendInfoTestsystemToMessage(text)));
		} catch (final Exception e) {
			if (abortInCaseOfException) {
				throw new AppFailureException("Error while sending Mail to: '" + to + '\'', e);
			} else {
				// Bei Fehler nicht abbrechen
				LOG.warn("VACME-WARN: Fehler beim senden des Mails an {}: {}", to, subject, e);
			}
		}
	}

	public void sendHtmlMail(
		@Nonnull String to,
		@Nonnull String subject,
		@Nonnull String html,
		boolean abortInCaseOfException
	) throws AppFailureException {
		try {
			mailer.send(Mail.withHtml(to, appendStufeAndKantonToSubject(subject), appendInfoTestsystemToMessage(html)));
		} catch (final Exception e) {
			if (abortInCaseOfException) {
				throw new AppFailureException("Error while sending Mail to: '" + to + '\'', e);
			} else {
				// Bei Fehler nicht abbrechen
				LOG.warn("VACME-WARN: Fehler beim senden des Mails an {}: {}", to, subject, e);
			}
		}
	}

	/**
	 * Schickt eine "Willkommens" Mail an die Fachperson mit Angaben wie sie sich in der App Einloggen kann und wo
	 * sie Hilfe findet.
	 */
	public void sendEinladungFachapplikation(@NonNull OdiUserJax userJax, @NonNull @NotNull OrtDerImpfungJax odi) {
		String username = userJax.getUsername();
		String linkBlog = getBlogLink(odi.getTyp());
		String kontaktFachlich = translate.translate("mail_einladung_kontakt_fachlich");
		String kontaktTechnisch = translate.translate("mail_einladung_kontakt_technisch");
		String subject = translate.translate("mail_einladung_fachapplikation_subject");
		String content = translate.translate("mail_einladung_fachapplikation_content",
			username,
			urlFachapplikation,
			linkBlog,
			kontaktFachlich,
			kontaktTechnisch);
		sendHtmlMail(userJax.getEmail(), subject, content, true);
	}

	@NonNull
	private String getBlogLink(@NonNull OrtDerImpfungTyp typ) {
		String link = urlBlogFachapplikation;
		if (!link.endsWith("/")) {
			link = link + '/';
		}
		switch (typ) {
		case HAUSARZT:
			return link + "arztpraxen/";
		case ALTERSHEIM:
			return link + "heime/";
		case SPITAL:
			return link + "listenspitaeler/";
		default:
			return link;
		}
	}

	public void sendCheckFreieZweittermineMail(
		@NonNull Set<String> mailEmpfaenger,
		@NonNull OrtDerImpfung odi,
		@NonNull Set<LocalDate> daten
	) {
		String subject = translate.translate(
			"mail_check_freie_zweittermine_subject",
			odi.getName());
		StringBuilder content = new StringBuilder();
		for (LocalDate datum : daten) {
			String contentText = translate.translate(
			"mail_check_freie_zweittermine_content",
			DEFAULT_DATE_FORMAT.apply(Locale.getDefault()).format(datum));
			content.append(contentText).append("\n");
		}

		// Evtl. zusaetzliche Kopie an Verteiler
		if (StringUtils.isNotEmpty(ccMailZuwenigeZweittermine)) {
			mailEmpfaenger.add(ccMailZuwenigeZweittermine);
		}

		for (String empfaenger : mailEmpfaenger) {
			this.sendTextMail(empfaenger, subject, content.toString(), false);
		}
	}

	@Override
	public void sendFinishedDocumentQueueSuccessJobMail(
		@NonNull VonBisSpracheParamJax params,
		@NonNull DocumentQueue abrechnungDocQueue,
		@NonNull String processingTimeSeconds
	) {
		Sprache sprache = params.getSprache();

		String von = DateUtil.formatDate(params.getVon());
		String bis = DateUtil.formatDate(params.getBis());
		String receiverMail = abrechnungDocQueue.getBenutzer().getEmail();
		UUID resultId = Optional.ofNullable(abrechnungDocQueue.getDocumentQueueResult())
			.orElseThrow(() ->  new AppFailureException("Document Result war nicht verfuegbar "
				+ "fuer AbrechnungDocumentQueue#" + abrechnungDocQueue.getId()))
			.getId();

		String fullLink = getFullLink(this.urlFachapplikation, resultId);
		String expiryDate = calculateExpiryTime(abrechnungDocQueue);

		final String subject = ServerMessageUtil.getMessage("mail_document_job_finished_subject",
			sprache.getLocale(), abrechnungDocQueue.getId());

		final String content = ServerMessageUtil.getMessage("mail_abrechnung_document_job_finished_content",
			sprache.getLocale(), von, bis, processingTimeSeconds, fullLink, expiryDate);

		this.sendTextMail(receiverMail, subject, content, true);
	}

	private String calculateExpiryTime(@NonNull DocumentQueue abrechnungDocQueue) {
		LocalDateTime expiryDateTime = abrechnungDocQueue.getResultTimestamp().plus(maxDocumentqueueResultAgeSeconds,
			ChronoUnit.SECONDS);
		String expiryDate = DateUtil.DEFAULT_DATE_TIME_FORMAT.apply(GERMAN).format(expiryDateTime);
		return expiryDate;
	}

	@NonNull
	private String getFullLink(@NonNull String link, @NonNull UUID resultId) {
		try {
			URIBuilder builder = new URIBuilder(link);
			builder.setPath("/reports");
			builder.addParameter("resultid", resultId.toString());
			return builder.build().toURL().toString();
		} catch (URISyntaxException | MalformedURLException e) {
			LOG.error("Problem creating url from {} and {}", link, resultId, e);
		}
		return link;
	}

	@Override
	public void sendFinishedDocumentQueueSuccessJobMail(
		@NonNull Sprache sprache,
		@NonNull DocumentQueue docQueue,
		@NonNull String processingTimeSeconds
	) {
		String receiverMail = docQueue.getBenutzer().getEmail();
		UUID resultId = Optional.ofNullable(docQueue.getDocumentQueueResult())
			.orElseThrow(() ->  new AppFailureException("Document Result war nicht verfuegbar "
				+ "fuer AbrechnungDocumentQueue#" + docQueue.getId()))
			.getId();

		final String subject = ServerMessageUtil.getMessage("mail_document_job_finished_subject",
			sprache.getLocale(), docQueue.getId());

		final String content = ServerMessageUtil.getMessage("mail_document_job_finished_content",
			sprache.getLocale(), processingTimeSeconds, getFullLink(urlFachapplikation, resultId), calculateExpiryTime(docQueue));

		this.sendTextMail(receiverMail, subject, content, true);
	}

	@Override
	public void sendFinishedDocumentQueueJobFailureMail(
		@NonNull Sprache sprache,
		@Nullable String rawJsonParamString,
		@NonNull DocumentQueue abrechnungDocQueue,
		@NonNull String errormessage
	) {
		final String subject = ServerMessageUtil.getMessage("mail_document_job_error_subject",
			sprache.getLocale(), abrechnungDocQueue.getId());

		final String content = ServerMessageUtil.getMessage("mail_document_job_error_content",
			sprache.getLocale(), abrechnungDocQueue.getId(), rawJsonParamString, errormessage);
		String receiverMail = abrechnungDocQueue.getBenutzer().getEmail();
		this.sendTextMail(receiverMail, subject, content, true);
	}
}
