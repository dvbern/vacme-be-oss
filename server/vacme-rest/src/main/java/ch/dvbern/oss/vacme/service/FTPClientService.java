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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import javax.enterprise.context.RequestScoped;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import ch.dvbern.oss.vacme.shared.errors.FTPFailureException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.util.TrustManagerUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Slf4j
@RequestScoped
@Transactional
public class FTPClientService {

	@ConfigProperty(name = "vacme.ftp.disabled", defaultValue = "false")
	boolean ftpDisabled;

	@ConfigProperty(name = "vacme.ftp.server")
	String server;

	@ConfigProperty(name = "vacme.ftp.port", defaultValue = "21")
	int port;

	@ConfigProperty(name = "vacme.ftp.username")
	String username;

	@ConfigProperty(name = "vacme.ftp.password")
	String password;

	private FTPSClient ftps;

	protected void open() {
		try {
			__open();
		} catch (IOException e) {
			LOG.error("FTP connection failed", e);

			throw new FTPFailureException("FTP connection failed");
		}
	}

	private void __open() throws IOException {

		ftps = new FTPSClient();
		ftps.addProtocolCommandListener(new VacMeFTPProtocolCommandListener());
		ftps.setTrustManager(TrustManagerUtils.getAcceptAllTrustManager());
		ftps.setConnectTimeout(6000);

		ftps.connect(server, port);
		int reply = ftps.getReplyCode();
		if (!FTPReply.isPositiveCompletion(reply)) {
			ftps.disconnect();
			throw new FTPFailureException("Exception in connecting to FTP Server");
		}

		// Set protection buffer size
		ftps.execPBSZ(0);
		// Set data channel protection to private
		ftps.execPROT("P");

		reply = ftps.sendCommand("HOST", server); //wegen virtueller Host

		if (!FTPReply.isPositiveCompletion(reply)) {
			ftps.disconnect();
			throw new FTPFailureException("HOST Command for virtuell Host failed");
		}

		boolean loggedin = ftps.login(username, password);
		if (!loggedin) {
			ftps.disconnect();
			throw new FTPFailureException("Loggin failed");

		}
		ftps.enterLocalPassiveMode();
	}

	protected void close() {

		try {
			__close();
		} catch (IOException e) {
			LOG.error("FTP closing connection failed", e);
			throw new FTPFailureException("FTP closing connection failed");
		}
	}

	private void __close() throws IOException {
		ftps.disconnect();
	}

	@NonNull
	public Collection<String> listFiles(@NonNull String path) {
		if (ftpDisabled) {
			LOG.info("FTP connection disabled");
			return Collections.emptyList();
		}
		open();
		try {
			FTPFile[] files = ftps.listFiles(path);
			return Arrays.stream(files)
				.map(FTPFile::getName)
				.collect(Collectors.toList());
		} catch (IOException e) {
			LOG.error("FTP command LIST failed", e);
			throw new FTPFailureException("FTP command LIST failed");
		} finally {
			close();
		}
	}

	public OutputStream getFile(@NonNull String source) {
		if (ftpDisabled) {
			LOG.info("FTP connection disabled");
			return OutputStream.nullOutputStream();
		}

		open();
		try {
			OutputStream outputStream = new ByteArrayOutputStream();
			ftps.retrieveFile(source, outputStream);
			IOUtils.close(outputStream);
			return outputStream;
		} catch (IOException e) {
			LOG.error("FTP command RETR failed", e);
			throw new FTPFailureException("FTP command RETR failed");
		} finally {
			close();
		}
	}

	@Transactional(value = TxType.REQUIRES_NEW, dontRollbackOn = FTPFailureException.class)
	public boolean putFileToPath(@NonNull InputStream inputStream, @NonNull String path) {
		if (ftpDisabled) {
			LOG.debug("FTP connection disabled");
			return true;
		}
		try {
			open();
			if (!ftps.storeFile(path, inputStream)) {
				LOG.error("FTP command STOR failed");
				return false;
			}

		} catch (Throwable e) {
			LOG.error("FTP command STOR failed", e);
			return false;
		} finally {
			try {
				close();
			} catch (Throwable ex) {
				LOG.warn("FTP Connection could not be closed", ex);
			}
		}
		return true;
	}

	/**
	 * Gibt die FTP Commands im Logger aus
	 */
	private static final class VacMeFTPProtocolCommandListener implements ProtocolCommandListener {
		@Override
		public void protocolCommandSent(ProtocolCommandEvent event) {

			final String cmd = event.getCommand();
			if ("PASS".equalsIgnoreCase(cmd) || "USER".equalsIgnoreCase(cmd)) {
				LOG.debug(cmd + " *******");
			} else {
				final String IMAP_LOGIN = "LOGIN";
				if (IMAP_LOGIN.equalsIgnoreCase(cmd)) { // IMAP
					String msg = event.getMessage();
					msg = msg.substring(0, msg.indexOf(IMAP_LOGIN) + IMAP_LOGIN.length());
					LOG.debug(msg + " *******");
				} else {
					LOG.debug(event.getMessage().replace("\n", "").replace("\r", ""));
				}
			}
		}

		@Override
		public void protocolReplyReceived(ProtocolCommandEvent event) {
			LOG.debug(event.getMessage().replace("\n", "").replace("\r", ""));
		}
	}
}
