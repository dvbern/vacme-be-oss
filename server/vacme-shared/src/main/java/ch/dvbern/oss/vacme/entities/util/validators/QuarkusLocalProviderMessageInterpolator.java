package ch.dvbern.oss.vacme.entities.util.validators;

import java.util.Locale;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.MessageInterpolator;

import ch.dvbern.oss.vacme.i18n.LocaleProvider;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;

@ApplicationScoped
public class QuarkusLocalProviderMessageInterpolator implements MessageInterpolator {

	@Inject
	LocaleProvider currentLocaleProvider;
	private final ResourceBundleMessageInterpolator delegatedInterpolator;

	public QuarkusLocalProviderMessageInterpolator() {
		delegatedInterpolator = new ResourceBundleMessageInterpolator();
	}

	@Override
	public String interpolate(String s, Context context) {
		Locale locale = currentLocaleProvider.currentLocale();
		return delegatedInterpolator.interpolate(s, context, locale);
	}

	@Override
	public String interpolate(String s, Context context, Locale locale) {
		return delegatedInterpolator.interpolate(s, context, locale);
	}
}
