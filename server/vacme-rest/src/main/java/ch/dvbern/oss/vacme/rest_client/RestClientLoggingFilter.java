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

package ch.dvbern.oss.vacme.rest_client;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Based on org.glassfish.jersey.filter.LoggingFilter
 */
@Slf4j
public class RestClientLoggingFilter implements ClientRequestFilter, ClientResponseFilter, WriterInterceptor {

	@ConfigProperty(name = "vmdl.client.logging.filter.disabled", defaultValue = "false")
	boolean disableLoggingFilter;

	private static final String NOTIFICATION_PREFIX = "* ";

	private static final String REQUEST_PREFIX = "> ";

	private static final String RESPONSE_PREFIX = "< ";

	private static final String ENTITY_LOGGER_PROPERTY = RestClientLoggingFilter.class.getName() + ".entityLogger";

	private static final String LOGGING_ID_PROPERTY = RestClientLoggingFilter.class.getName() + ".id";

	private static final Comparator<Map.Entry<String, List<String>>> COMPARATOR = (o1, o2) -> o1.getKey().compareToIgnoreCase(o2.getKey());

	private static final int DEFAULT_MAX_ENTITY_SIZE = 8 * 1024;

	private final AtomicLong _id = new AtomicLong(0);

	private final int maxEntitySize;

	public RestClientLoggingFilter() {

		this.maxEntitySize = RestClientLoggingFilter.DEFAULT_MAX_ENTITY_SIZE;
	}

	private void log(final StringBuilder b) {
		if (disableLoggingFilter) {
			return;
		}
		LOG.info(b.toString());
	}

	private StringBuilder prefixId(final StringBuilder b, final long id) {

		b.append(id).append(" ");
		return b;
	}

	private void printRequestLine(final StringBuilder b, final String note, final long id, final String method, final URI uri) {

		this.prefixId(b, id).append(RestClientLoggingFilter.NOTIFICATION_PREFIX)
			.append(note)
			.append(" on thread ").append(Thread.currentThread().getName())
			.append("\n");
		this.prefixId(b, id).append(RestClientLoggingFilter.REQUEST_PREFIX).append(method).append(" ")
			.append(uri.toASCIIString()).append("\n");
	}

	private void printResponseLine(final StringBuilder b, final String note, final long id, final int status) {

		this.prefixId(b, id).append(RestClientLoggingFilter.NOTIFICATION_PREFIX)
			.append(note)
			.append(" on thread ").append(Thread.currentThread().getName()).append("\n");
		this.prefixId(b, id).append(RestClientLoggingFilter.RESPONSE_PREFIX)
			.append(status)
			.append("\n");
	}

	private void printPrefixedHeaders(final StringBuilder b,
									  final long id,
									  final String prefix,
									  final MultivaluedMap<String, String> headers) {

		for (final Map.Entry<String, List<String>> headerEntry : this.getSortedHeaders(headers.entrySet())) {
			final List<?> val = headerEntry.getValue();
			final String header = headerEntry.getKey();

			if (val.size() == 1) {
				this.prefixId(b, id).append(prefix).append(header).append(": ").append(val.get(0)).append("\n");
			} else {
				final StringBuilder sb = new StringBuilder();
				boolean add = false;
				for (final Object s : val) {
					if (add) {
						sb.append(',');
					}
					add = true;
					sb.append(s);
				}
				this.prefixId(b, id).append(prefix).append(header).append(": ").append(sb.toString()).append("\n");
			}
		}
	}

	private Set<Map.Entry<String, List<String>>> getSortedHeaders(final Set<Map.Entry<String, List<String>>> headers) {

		final TreeSet<Map.Entry<String, List<String>>> sortedHeaders = new TreeSet<>(RestClientLoggingFilter.COMPARATOR);
		sortedHeaders.addAll(headers);
		return sortedHeaders;
	}

	private InputStream logInboundEntity(final StringBuilder b, InputStream stream, final Charset charset) throws IOException {

		if (!stream.markSupported()) {
			stream = new BufferedInputStream(stream);
		}
		stream.mark(this.maxEntitySize + 1);
		final byte[] entity = new byte[this.maxEntitySize + 1];
		final int entitySize = stream.read(entity);
		if (entitySize < 0) {
			b.append("entity size was -1. Empty answer?");
		} else {
			b.append(new String(entity, 0, Math.min(entitySize, this.maxEntitySize), charset));
		}

		if (entitySize > this.maxEntitySize) {
			b.append("...more...");
		}
		b.append('\n');
		stream.reset();
		return stream;
	}

	@Override
	public void filter(final ClientRequestContext context) throws IOException {
		final long id = this._id.incrementAndGet();
		context.setProperty(RestClientLoggingFilter.LOGGING_ID_PROPERTY, id);

		final StringBuilder b = new StringBuilder();

		this.printRequestLine(b, "Sending client request", id, context.getMethod(), context.getUri());
		this.printPrefixedHeaders(b, id, RestClientLoggingFilter.REQUEST_PREFIX, context.getStringHeaders());

		if (context.hasEntity()) {
			final OutputStream stream = new RestClientLoggingFilter.LoggingStream(b, context.getEntityStream());
			context.setEntityStream(stream);
			context.setProperty(RestClientLoggingFilter.ENTITY_LOGGER_PROPERTY, stream);
			// not calling log(b) here - it will be called by the interceptor
		} else {
			this.log(b);
		}
	}

	@Override
	public void filter(final ClientRequestContext requestContext, final ClientResponseContext responseContext) throws IOException {
		final Object requestId = requestContext.getProperty(RestClientLoggingFilter.LOGGING_ID_PROPERTY);
		final long id = requestId != null ? (Long) requestId : this._id.incrementAndGet();

		final StringBuilder b = new StringBuilder();

		this.printResponseLine(b, "Client response received", id, responseContext.getStatus());
		this.printPrefixedHeaders(b, id, RestClientLoggingFilter.RESPONSE_PREFIX, responseContext.getHeaders());

		if (responseContext.hasEntity()) {
			responseContext.setEntityStream(this.logInboundEntity(b, responseContext.getEntityStream(),
				Charset.defaultCharset()));
		}

		this.log(b);
	}

	@Override
	public void aroundWriteTo(final WriterInterceptorContext writerInterceptorContext) throws IOException, WebApplicationException {
		final RestClientLoggingFilter.LoggingStream stream = (RestClientLoggingFilter.LoggingStream) writerInterceptorContext.getProperty(RestClientLoggingFilter.ENTITY_LOGGER_PROPERTY);
		writerInterceptorContext.proceed();
		if (stream != null) {
			this.log(stream.getStringBuilder(Charset.defaultCharset()));
		}
	}

	private class LoggingStream extends FilterOutputStream {

		private final StringBuilder b;

		private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

		LoggingStream(final StringBuilder b, final OutputStream inner) {

			super(inner);

			this.b = b;
		}

		StringBuilder getStringBuilder(final Charset charset) {
			// write entity to the builder
			final byte[] entity = this.baos.toByteArray();

			this.b.append(new String(entity, 0, Math.min(entity.length, RestClientLoggingFilter.this.maxEntitySize), charset));
			if (entity.length > RestClientLoggingFilter.this.maxEntitySize) {
				this.b.append("...more...");
			}
			this.b.append('\n');

			return this.b;
		}

		@Override
		public void write(final int i) throws IOException {

			if (this.baos.size() <= RestClientLoggingFilter.this.maxEntitySize) {
				this.baos.write(i);
			}
			this.out.write(i);
		}

	}

}
