package ch.dvbern.oss.vacme.reports.zweitBoosterMail;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import ch.dvbern.oss.vacme.entities.registration.Prioritaet;
import ch.dvbern.oss.vacme.entities.types.daterange.DateUtil;
import ch.dvbern.oss.vacme.jax.ZweitBoosterMailDataRow;
import ch.dvbern.oss.vacme.repo.ImpfungRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

@Transactional
@ApplicationScoped
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class MailZweitBoosterServiceBean {

	private static final String MAIL_STYLE = "<style>\n"
		+ "table {\n"
		+ "  font-family: arial, sans-serif;\n"
		+ "  border-collapse: collapse;\n"
		+ "  width: 50%;\n"
		+ "}\n"
		+ '\n'
		+ "td, th {\n"
		+ "  border: 1px solid #dddddd;\n"
		+ "  text-align: left;\n"
		+ "  padding: 8px;\n"
		+ "}\n"
		+ '\n'
		+ "</style>\n";

	private static final String MAIL_TEMPLATE = "<html>\n"
		+ "%s"
		+ "%s"
		+ "<h3>Impfungen nach Priorität sortiert<h3>\n"
		+ "<table>\n"
		+ "  <tr>\n"
		+ "    <th>Gruppe</th>\n"
		+ "    <th>1. Impfung</th>\n"
		+ "    <th>2. Impfung</th>\n"
		+ "    <th>1. Booster</th>\n"
		+ "    <th>2. Booster</th>\n"
		+ "  </tr>\n"
		+ "  %s"
		+ "</table>\n"
		+ "</html>";

	private static final String MAIL_KENNZAHLEN_TEMPLATE = "<h2>Reporting 2. Booster-Impfung</h2>\n"
		+ "<p>Total %d Impfungen</p>\n"
		+ "<p>Davon %d Selbstzahler Impfungen</p>\n"
		+ '\n'
		+ "<p><b>%d Bezahlte Impfungen:</b></p>\n"
		+ '\n'
		+ "<p>%d Impfungen von über 80 Jährigen</p>\n"
		+ '\n'
		+ "<p>%d Impfungen von immunsupprimierten Personen</p>\n"
		+ '\n'
		+ "<p>%d andere</p>\n";

	private static final String TABLE_ENTRY = "<td>%d</td>\n";
	private static final String TABLE_ROW = "<tr>\n%s</tr>\n";

	private final ImpfungRepo impfungRepo;

	public String generateMailContent() {
		List<ZweitBoosterMailDataRow> zweitBooster = impfungRepo.getAllZweitOderMehrBooster();

		String kennzahlenHtml = generateKennzahlenHtml(zweitBooster);

		String prioTableRowsHtml = generatePrioTableRowsHtml(zweitBooster);

		String content = String.format(
			MAIL_TEMPLATE,
			MAIL_STYLE,
			kennzahlenHtml,
			prioTableRowsHtml);

		return content;
	}

	private String generateKennzahlenHtml(List<ZweitBoosterMailDataRow> zweitBooster) {
		long selbstzahlendeZweitBoosterImpfungen = zweitBooster.stream()
			.filter(ZweitBoosterMailDataRow::isSelbstzahlerImpfung)
			.count();
		List<ZweitBoosterMailDataRow> bezahlteZweitBooster =
			zweitBooster.stream()
				.filter(dataRow -> !dataRow.isSelbstzahlerImpfung())
				.collect(Collectors.toList());

		long ueber80JaehrigeZweitBoosterImpfungen = bezahlteZweitBooster.stream()
			.filter(dataRow -> DateUtil.getAge(dataRow.getGeburtsdatum()) >= 80)
			.count();
		long immunsupprimierteZweitBoosterImpfungen =
			bezahlteZweitBooster.stream()
				.filter(dataRow -> Boolean.TRUE.equals(dataRow.getImmunsupprimiert()))
				.count();

		long andere =
			bezahlteZweitBooster.stream()
				.filter(dataRow -> !Boolean.TRUE.equals(dataRow.getImmunsupprimiert())
					&& DateUtil.getAge(dataRow.getGeburtsdatum()) < 80)
				.count();

		return String.format(
			MAIL_KENNZAHLEN_TEMPLATE,
			zweitBooster.size(),
			selbstzahlendeZweitBoosterImpfungen,
			bezahlteZweitBooster.size(),
			ueber80JaehrigeZweitBoosterImpfungen,
			immunsupprimierteZweitBoosterImpfungen,
			andere);
	}

	private String generatePrioTableRowsHtml(List<ZweitBoosterMailDataRow> zweitBooster) {

		Map<Prioritaet, Long> erstImpfungenPerPrio = impfungRepo.getCountAllErstImpfungenPerPrioritaet();
		Map<Prioritaet, Long> zweitImpfungenPerPrio = impfungRepo.getCountAllZweitImpfungenPerPrioritaet();
		Map<Prioritaet, Long> erstBoosterPerPrio = impfungRepo.getCountAllErstBoosterPerPrioritaet();
		Map<Prioritaet, Long> zweitBoosterPerPrio =
			zweitBooster.stream().collect(groupingBy(ZweitBoosterMailDataRow::getPrioritaet, counting()));

		StringBuilder tableRows = new StringBuilder();

		for (Prioritaet prioritaet : Prioritaet.values()) {

			StringBuilder sb = new StringBuilder();

			sb.append("<td>").append(prioritaet.name()).append("</td>\n");

			sb.append(String.format(
				TABLE_ENTRY,
				erstImpfungenPerPrio.containsKey(prioritaet) ? erstImpfungenPerPrio.get(prioritaet) : 0));
			sb.append(String.format(
				TABLE_ENTRY,
				zweitImpfungenPerPrio.containsKey(prioritaet) ? zweitImpfungenPerPrio.get(prioritaet) : 0));
			sb.append(String.format(
				TABLE_ENTRY,
				erstBoosterPerPrio.containsKey(prioritaet) ? erstBoosterPerPrio.get(prioritaet) : 0));
			sb.append(String.format(
				TABLE_ENTRY,
				zweitBoosterPerPrio.containsKey(prioritaet) ? zweitBoosterPerPrio.get(prioritaet) : 0));

			tableRows.append(String.format(TABLE_ROW, sb));
		}

		return tableRows.toString();
	}
}
